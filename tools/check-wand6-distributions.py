#!/usr/bin/env python3
"""Verify extracted production launchers. Images and live-window PNGs stay local."""
import argparse
import io
import os
from pathlib import Path
import shutil
import subprocess
import tarfile
import time
import zipfile

ROOT = Path(__file__).resolve().parents[1]
IMAGE_SUFFIXES = {'.png', '.jpg', '.jpeg', '.gif', '.webp', '.bmp', '.svg', '.ico'}


def require(value, message):
    if not value:
        raise AssertionError(message)


def verify_jar(data, name):
    with zipfile.ZipFile(io.BytesIO(data)) as archive:
        entries = archive.namelist()
        require(not any(Path(p).suffix.lower() in IMAGE_SUFFIXES for p in entries), f'Image in {name}')
        classes = [p for p in entries if p.endswith('.class')]
        require(classes and all(p.startswith('wand6/') for p in classes), f'Non-wand6 class in {name}')
        require(not any('Check' in p for p in classes), f'Check class in production {name}')


def verify_archives(version):
    for module in ('client', 'server'):
        for suffix in ('zip', 'tar'):
            artifact = ROOT / module / 'build/distributions' / f'wand6-{module}-{version}.{suffix}'
            if suffix == 'zip':
                with zipfile.ZipFile(artifact) as archive:
                    entries = [(p, archive.read(p)) for p in archive.namelist() if not p.endswith('/')]
            else:
                with tarfile.open(artifact) as archive:
                    entries = [(p.name, archive.extractfile(p).read()) for p in archive if p.isfile()]
            for name, data in entries:
                require(Path(name).suffix.lower() not in IMAGE_SUFFIXES, f'Image in {artifact}: {name}')
                if name.endswith('.jar'):
                    verify_jar(data, name)
    standalone = ROOT / 'server/build/libs' / f'wand6-server-{version}-standalone.jar'
    verify_jar(standalone.read_bytes(), str(standalone))
    print('PASS: ZIP/TAR and standalone contain only wand6 production classes; no images/checks.', flush=True)


def run_logged(command, cwd, env, log, timeout=35):
    with log.open('w') as stream:
        result = subprocess.run(command, cwd=cwd, env=env, stdout=stream, stderr=subprocess.STDOUT, timeout=timeout)
    return result.returncode, log.read_text()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--assets', type=Path, required=True, help='Directory containing images listed in client/required-assets.txt')
    parser.add_argument('--output', type=Path, default=ROOT / 'client/build/distribution-check')
    parser.add_argument('--version', default='4.2.0-rc1')
    args = parser.parse_args()
    require(os.environ.get('DISPLAY') or os.environ.get('WAYLAND_DISPLAY'), 'A real graphical session is required')
    output = args.output.resolve()
    output.mkdir(parents=True, exist_ok=True)
    # Fresh unique extraction avoids stale assets and PNG evidence from earlier runs.
    import tempfile
    run = Path(tempfile.mkdtemp(prefix='run-', dir=output))
    print(f'Output: {run}', flush=True)
    verify_archives(args.version)
    env = os.environ.copy()
    for variable in ('CLASSPATH', 'JAVA_OPTS', 'JDK_JAVA_OPTIONS', 'JAVA_TOOL_OPTIONS'):
        env.pop(variable, None)
    for module in ('client', 'server'):
        with tarfile.open(ROOT / module / 'build/distributions' / f'wand6-{module}-{args.version}.tar') as archive:
            archive.extractall(run, filter='data')
    client = run / f'wand6-client-{args.version}'
    server = run / f'wand6-server-{args.version}'
    client_script = str(client / 'bin/wand6-client')
    server_script = str(server / 'bin/wand6-server')
    code, log = run_logged([client_script, 'localhost', '1'], run, env, run / 'missing-assets.log')
    require(code == 1 and 'Missing wand6 asset img/summer.png' in log, 'Missing asset diagnostic failed')
    (client / 'assets/img').mkdir(parents=True)
    for asset in (ROOT / 'client/required-assets.txt').read_text().splitlines():
        destination = client / 'assets' / asset
        destination.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(args.assets.resolve() / asset, destination)
    missing_sprite = client / 'assets/img/peasant_walk/e_01.png'
    sprite_bytes = missing_sprite.read_bytes()
    missing_sprite.unlink()
    code, log = run_logged([client_script, 'localhost', '1'], run, env, run / 'missing-sprite.log')
    require(code == 1 and 'Missing wand6 asset img/peasant_walk/e_01.png' in log, 'Missing sprite diagnostic failed')
    missing_sprite.write_bytes(sprite_bytes)
    headless = dict(env, JAVA_OPTS='-Djava.awt.headless=true')
    code, log = run_logged([client_script, 'localhost', '1'], run, headless, run / 'headless.log')
    require(code == 1 and 'needs a graphical desktop' in log, 'Headless diagnostic failed')
    invalid_name = dict(env, JAVA_OPTS='-Dwalkingdesu.testMode=true -Dwalkingdesu.testNick=x')
    code, log = run_logged([client_script, 'localhost', '1'], run, invalid_name, run / 'invalid-nick.log')
    require(code == 1 and 'Nick must be' in log, 'Test nickname validation failed')
    print('PASS: missing assets, headless desktop and invalid test nickname diagnostics.', flush=True)
    # Real object-stream smoke through the extracted production server script.
    common_jar = next((client / 'lib').glob('common-*.jar'))
    check_cp = os.pathsep.join([str(ROOT / 'server/build/classes/java/checks'), str(common_jar)])
    code, log = run_logged(['java', '-cp', check_cp, 'wand6.server.NetworkSmokeCheck', server_script], run, env,
                           run / 'network-smoke.log')
    require(code == 0 and 'PASS:' in log, log)
    print(log.strip(), flush=True)
    server_log = run / 'gui-server.log'
    with server_log.open('w') as stream:
        process = subprocess.Popen([server_script, '0'], cwd=run, env=env, stdout=stream, stderr=subprocess.STDOUT)
        try:
            deadline = time.monotonic() + 10
            port = None
            while time.monotonic() < deadline:
                for line in server_log.read_text().splitlines():
                    if line.startswith('WAND6_SERVER_PORT='):
                        port = int(line.split('=')[1])
                if port:
                    break
                require(process.poll() is None, server_log.read_text())
                time.sleep(0.04)
            require(port, 'Server startup timeout')
            agent = ROOT / 'client/build/libs' / f'client-{args.version}-gui-check-agent.jar'
            require(agent.is_file(), f'Build :client:guiCheckAgent first: {agent}')
            gui_env = dict(env, JAVA_OPTS=f'-Dwalkingdesu.testMode=true -Dwalkingdesu.testNick=stage8-gui "-javaagent:{agent}={run}"')
            code, log = run_logged([client_script, 'localhost', str(port)], run, gui_env, run / 'gui-client.log', timeout=60)
            require(code == 0 and 'PASS: distribution GUI' in log, log)
            require('Exception' not in log and 'Error' not in log, log)
            require((run / '01-login-map.png').is_file() and (run / '04-map-resize.png').is_file(), 'Missing live-window PNGs')
            print(f'PASS: production client script, GUI login/map/resize, port={port}, exit={code}.', flush=True)
        finally:
            process.terminate()
            try:
                process.wait(timeout=5)
            except subprocess.TimeoutExpired:
                process.kill()
                process.wait()
            print(f'GUI server stopped; exit={process.returncode}', flush=True)
    print(f'PASS: distribution checks complete. Inspect fresh PNGs in {run}', flush=True)


if __name__ == '__main__':
    main()
