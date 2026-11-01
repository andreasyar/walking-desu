#!/usr/bin/env python3
"""Recover peon attack/death from the local 5×13 sheet. Requires Pillow.

Preview: python tools/cut_peon_sprites.py
Install after reviewing the preview: python tools/cut_peon_sprites.py --install
Source sheets and generated PNGs remain outside Git. Never scales or smooths pixels.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import shutil
import tempfile
from pathlib import Path

from PIL import Image, ImageDraw, ImageOps

CELL = 72
SIZE = (5 * CELL, 13 * CELL)
BACKGROUND = (252, 252, 252)
ROOT = Path(__file__).resolve().parents[1]
ATTACK = {"n": (0, False), "ne": (1, False), "e": (2, False),
          "se": (3, False), "s": (4, False), "sw": (3, True),
          "w": (2, True), "nw": (1, True)}
# The first three death columns are identical, as are the last two.
DEATH = {"ne": (0, False), "nw": (0, True), "se": (3, False), "sw": (3, True)}


def digest(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def normalized(image: Image.Image) -> Image.Image:
    """Ignore invisible RGB when matching historical slices to the transparent sheet."""
    image = image.convert("RGBA")
    pixels = bytearray(image.tobytes())
    for offset in range(0, len(pixels), 4):
        if pixels[offset + 3] == 0:
            pixels[offset:offset + 3] = b"\0\0\0"
    return Image.frombytes("RGBA", image.size, bytes(pixels))


def remove_background(image: Image.Image) -> tuple[Image.Image, int]:
    image = image.convert("RGBA")
    # Exact color key, including enclosed holes. Keep black outlines and gray highlights.
    pixels = bytearray(image.tobytes())
    removed = 0
    for offset in range(0, len(pixels), 4):
        if tuple(pixels[offset:offset + 3]) == BACKGROUND:
            pixels[offset:offset + 4] = b"\0\0\0\0"
            removed += 1
    return Image.frombytes("RGBA", image.size, bytes(pixels)), removed


def crop(sheet: Image.Image, row: int, column: int, mirror: bool) -> Image.Image:
    frame = sheet.crop((column * CELL, row * CELL, (column + 1) * CELL, (row + 1) * CELL))
    return ImageOps.mirror(frame) if mirror else frame


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--assets", type=Path, default=ROOT / ".devcontainer/assets/client/img")
    parser.add_argument("--output", type=Path, default=ROOT / "client/build/peon-sprites")
    parser.add_argument("--install", action="store_true", help="Back up existing frames, then install attack/death")
    args = parser.parse_args()
    assets, output = args.assets.resolve(), args.output.resolve()
    if output == assets or assets in output.parents:
        parser.error("Preview output must be outside the source image directory")
    if output in assets.parents:
        parser.error("Preview output must not contain the source image directory")
    original = Image.open(assets / "peon.png").convert("RGBA")
    reference = Image.open(assets / "peasant.png").convert("RGBA")
    if original.size != SIZE or reference.size != SIZE:
        parser.error(f"Both sheets must be {SIZE}; found {original.size} and {reference.size}")
    if original.getpixel((0, 0))[:3] != BACKGROUND:
        parser.error(f"Unexpected peon background; expected {BACKGROUND}")
    sheet, removed = remove_background(original)
    mappings, frames, mismatches = [], [], []
    for state, rows, directions in [("attack", range(5, 10), ATTACK), ("death", range(10, 13), DEATH)]:
        target = output / f"peon_{state}"
        target.mkdir(parents=True, exist_ok=True)
        for direction, (column, mirror) in directions.items():
            for frame_number, row in enumerate(rows, 1):
                name = f"{direction}_{frame_number:02}.png"
                recovered = crop(sheet, row, column, mirror)
                expected = normalized(crop(reference, row, column, mirror))
                old_reference = assets / f"peasant_{state}" / name
                match = normalized(Image.open(old_reference)).tobytes() == expected.tobytes()
                # Known historical error: SW attack frame 5 was never mirrored.
                if not match:
                    if state == "attack" and name == "sw_05.png":
                        if normalized(Image.open(old_reference)).tobytes() != normalized(crop(reference, row, column, False)).tobytes():
                            raise ValueError(f"Unexpected historical frame: {old_reference}")
                        mismatches.append("peasant_attack/sw_05.png: historical slice lacks SW mirror; recovered peon frame is mirrored")
                    else:
                        raise ValueError(f"Sheet coordinates do not match historical frame: {old_reference}")
                if recovered.getbbox() is None:
                    raise ValueError(f"Empty recovered frame: {state}/{name}")
                recovered.save(target / name)
                frames.append((f"{state} {direction} {frame_number}", recovered))
                mappings.append({"file": f"peon_{state}/{name}", "row": row, "column": column,
                                 "mirror": mirror, "sha256": digest(target / name), "reference_match": match})

    # Checkerboard preview at native pixel size, labels outside the original 72×72 frames.
    preview = Image.new("RGB", (8 * 128, 7 * 100), "#22262c")
    drawing = ImageDraw.Draw(preview)
    for index, (label, frame) in enumerate(frames):
        x, y = (index % 8) * 128, (index // 8) * 100
        for cy in range(0, CELL, 8):
            for cx in range(0, CELL, 8):
                drawing.rectangle((x + cx, y + cy, x + cx + 7, y + cy + 7),
                                  fill="#89939d" if (cx // 8 + cy // 8) % 2 else "#bfc6cc")
        preview.paste(frame, (x, y), frame)
        drawing.text((x, y + 76), label, fill="white")
    preview.save(output / "preview.png")
    directions = list(ATTACK)
    death_direction = {"n": "ne", "ne": "ne", "e": "ne", "nw": "nw", "w": "nw",
                       "se": "se", "s": "sw", "sw": "sw"}
    animation = []
    for step in range(10):
        canvas = Image.new("RGB", (8 * 96, 2 * 100), "#aeb8c2")
        labels = ImageDraw.Draw(canvas)
        for index, direction in enumerate(directions):
            attack_frame = Image.open(output / "peon_attack" / f"{direction}_{step % 5 + 1:02}.png").convert("RGBA")
            death_frame = Image.open(output / "peon_death" / f"{death_direction[direction]}_{min(step, 2) + 1:02}.png").convert("RGBA")
            for row, frame, label in [(0, attack_frame, "attack"), (1, death_frame, "death")]:
                canvas.paste(frame, (index * 96 + 12, row * 100), frame)
                labels.text((index * 96 + 8, row * 100 + 77), f"{label} {direction}", fill="black")
        animation.append(canvas)
    animation[0].save(output / "preview.gif", save_all=True, append_images=animation[1:],
                      duration=150, loop=0, disposal=2, optimize=False)
    manifest = {"sheet_size": SIZE, "cell_size": CELL, "background_rgb": BACKGROUND,
                "removed_background_pixels": removed,
                "sources": {name: digest(assets / name) for name in ["peasant.png", "peon.png"]},
                "notes": mismatches, "frames": mappings}
    (output / "manifest.json").write_text(json.dumps(manifest, indent=2) + "\n", encoding="utf-8")
    if args.install:
        backup_root = assets.parent / "sprite-backups"
        backup_root.mkdir(parents=True, exist_ok=True)
        backup = Path(tempfile.mkdtemp(prefix="peon-", dir=backup_root))
        for state in ["attack", "death"]:
            old = assets / f"peon_{state}"
            if old.exists():
                shutil.copytree(old, backup / old.name)
        shutil.copy2(output / "manifest.json", backup / "replacement-manifest.json")
        for mapping in mappings:
            source = output / mapping["file"]
            destination = assets / mapping["file"]
            destination.parent.mkdir(parents=True, exist_ok=True)
            with tempfile.NamedTemporaryFile(dir=destination.parent, suffix=".png", delete=False) as temporary:
                temporary.write(source.read_bytes())
                temporary_path = Path(temporary.name)
            temporary_path.replace(destination)
        print(f"Installed {len(mappings)} frames; previous frames backed up in {backup}")
    print(f"Generated {len(mappings)} frames; removed {removed} background pixels; preview: {output / 'preview.png'}")
    for note in mismatches:
        print(note)


if __name__ == "__main__":
    main()
