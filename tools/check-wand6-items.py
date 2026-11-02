#!/usr/bin/env python3
"""Run items with two extracted production wand6 clients and capture live Swing windows."""
import argparse
import os
from pathlib import Path
import shutil
import subprocess
import tarfile
import tempfile
import time

ROOT = Path(__file__).resolve().parents[1]


def await_condition(condition, description, processes=(), timeout=20):
    deadline = time.monotonic() + timeout
    while time.monotonic() < deadline:
        if condition():
            return
        if any(p.poll() is not None for p in processes):
            raise AssertionError(f'Process exited awaiting {description}')
        time.sleep(.04)
    raise AssertionError(f'Timeout: {description}')


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--assets', type=Path, required=True)
    parser.add_argument('--version', default='4.2.0-rc1')
    parser.add_argument('--first', choices=('one', 'two'), default='one', help='Which named client logs in first')
    args = parser.parse_args()
    if not (os.environ.get('DISPLAY') or os.environ.get('WAYLAND_DISPLAY')):
        raise AssertionError('A real graphical session is required')
    output = ROOT / 'client/build/item-gui'
    output.mkdir(parents=True, exist_ok=True)
    run = Path(tempfile.mkdtemp(prefix='run-', dir=output))
    print(f'Output: {run}', flush=True)
    env = os.environ.copy()
    for key in ('CLASSPATH', 'JAVA_OPTS', 'JAVA_TOOL_OPTIONS', 'JDK_JAVA_OPTIONS'):
        env.pop(key, None)
    for module in ('client', 'server'):
        with tarfile.open(ROOT / module / 'build/distributions' / f'wand6-{module}-{args.version}.tar') as archive:
            archive.extractall(run, filter='data')
    client = run / f'wand6-client-{args.version}'
    for line in (ROOT / 'client/required-assets.txt').read_text().splitlines():
        destination = client / 'assets' / line
        destination.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(args.assets.resolve() / line, destination)
    agent = ROOT / f'client/build/libs/client-{args.version}-item-gui-check-agent.jar'
    processes, streams = [], []

    def launch(command, name, environment):
        stream = (run / (name + '.log')).open('w')
        streams.append(stream)
        p = subprocess.Popen(command, cwd=run, env=environment, stdout=stream, stderr=subprocess.STDOUT)
        processes.append(p)
        return p

    def launch_client(role, nick):
        opts = f'-Dwalkingdesu.testMode=true -Dwalkingdesu.testNick={nick} -Dwand6.checkRole={role} "-javaagent:{agent}={run}"'
        return launch([str(client / 'bin/wand6-client'), 'localhost', str(port)], role, dict(env, JAVA_OPTS=opts))

    def successful(p, role):
        code = p.wait(timeout=60)
        log = (run / (role + '.log')).read_text()
        if code != 0 or f'PASS: items GUI role={role}' not in log or 'Exception' in log or 'Error' in log:
            raise AssertionError(f'{role} exit={code}\n{log}')

    try:
        server_cp = str(ROOT / 'server/build/classes/java/checks') + os.pathsep + str(run / f'wand6-server-{args.version}/lib/*')
        server = launch(['java', '-cp', server_cp, 'wand6.server.ItemCheck', 'fixture'], 'server', env)
        def get_port():
            for line in (run / 'server.log').read_text().splitlines():
                if line.startswith('WAND6_SERVER_PORT='):
                    return int(line.split('=')[1])
            return None
        await_condition(get_port, 'server startup', [server])
        port = get_port()
        first = launch_client(args.first, 'st12-' + args.first)
        await_condition(lambda: 'WelcomeMessage' in (run / (args.first + '.log')).read_text(),
                        'first client WELCOME', [first, server])
        second_role = 'two' if args.first == 'one' else 'one'
        second = launch_client(second_role, 'st12-' + second_role)
        one, two = (first, second) if args.first == 'one' else (second, first)
        successful(one, 'one')
        successful(two, 'two')
        pngs = list(run.glob('*.png'))
        if len(pngs) != 14:
            raise AssertionError(f'Expected 14 live-window PNGs, got {len(pngs)}')
        print(f'PASS: Items production clients; fixture-seeded gold/NPC, pickup/merge/resize, PvP death/drop/repick, tower reward, disconnect conservation; port={port}; clients exit=0; fourteen live-window PNGs in {run}', flush=True)
    finally:
        for p in reversed(processes):
            if p.poll() is None:
                p.terminate()
                try:
                    p.wait(timeout=5)
                except subprocess.TimeoutExpired:
                    p.kill()
                    p.wait()
        for stream in streams:
            stream.close()


if __name__ == '__main__':
    main()
