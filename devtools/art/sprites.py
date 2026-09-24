# Copyright (C) 2026 Rusty Shackleford and nfx. SPDX-License-Identifier: AGPL-3.0-or-later
"""The bar's pixel art, drawn here so it is ours and reproducible.

Run: uv run --no-project --with pillow python devtools/art/sprites.py
Writes src/main/resources/assets/azimuth/textures/gui/sprites/*.png (and the bar's nine-slice
mcmeta) and, with --preview <file>, a 6x mock of the bar with every sprite on it to judge by eye.

Every size is in GUI pixels. The bar is 10 tall: an outline row, a highlight row, six body rows,
a shadow row, an outline row, with the corner pixel cut so the ends read as rounded. Shapes that
are tinted in the game (dots, chevrons) are white with a black outline: the client multiplies
the sprite by the marker's colour, so white takes the colour and black stays black.
"""
from __future__ import annotations

import json
import sys
from pathlib import Path

from PIL import Image

HERE = Path(__file__).resolve().parent
SPRITES = HERE.parent.parent / "src/main/resources/assets/azimuth/textures/gui/sprites"

BLACK = (0, 0, 0, 255)
WHITE = (255, 255, 255, 255)
CLEAR = (0, 0, 0, 0)
HIGHLIGHT = (61, 67, 76, 255)
BODY = (42, 46, 52, 255)
SHADOW = (28, 31, 36, 255)
NOTCH = (201, 206, 214, 255)
NOTCH_EDGE = (107, 114, 126, 255)

# The badge colours players already know from the bar this one replaces: red north, yellow
# east, blue south, green west. Each badge is a 1 px ring in the darker shade around the fill.
BADGES = {
    "n": ((216, 58, 58, 255), (110, 20, 20, 255)),
    "e": ((224, 192, 48, 255), (120, 96, 12, 255)),
    "s": ((58, 106, 216, 255), (20, 46, 120, 255)),
    "w": ((58, 176, 74, 255), (20, 90, 34, 255)),
}
LETTERS = {
    "n": ["X...X", "XX..X", "X.X.X", "X..XX", "X...X"],
    "e": ["XXXXX", "X....", "XXXX.", "X....", "XXXXX"],
    "s": [".XXXX", "X....", ".XXX.", "....X", "XXXX."],
    "w": ["X...X", "X...X", "X.X.X", "XX.XX", "X...X"],
}


def canvas(w: int, h: int) -> Image.Image:
    return Image.new("RGBA", (w, h), CLEAR)


def outlined(w: int, h: int, fill: set[tuple[int, int]], colour=WHITE) -> Image.Image:
    """A shape of `fill` pixels in `colour` with every 8-neighbour outside it painted black."""
    img = canvas(w, h)
    px = img.load()
    for x, y in fill:
        px[x, y] = colour
    for x, y in list(fill):
        for dx in (-1, 0, 1):
            for dy in (-1, 0, 1):
                nx, ny = x + dx, y + dy
                if 0 <= nx < w and 0 <= ny < h and (nx, ny) not in fill:
                    px[nx, ny] = BLACK
    return img


def bar() -> Image.Image:
    """16 by 10; nine-sliced at 3 px caps so any width tiles the middle."""
    img = canvas(16, 10)
    px = img.load()
    for x in range(16):
        for y in range(10):
            if y in (0, 9) or x in (0, 15):
                px[x, y] = BLACK
            elif y == 1:
                px[x, y] = HIGHLIGHT
            elif y == 8:
                px[x, y] = SHADOW
            else:
                px[x, y] = BODY
    for x, y in ((0, 0), (15, 0), (0, 9), (15, 9)):
        px[x, y] = CLEAR
    return img


def notch() -> Image.Image:
    """3 by 10: the viewer's own heading, a light tick down from the top edge and up from the
    bottom, with a shoulder either side where it meets the outline."""
    img = canvas(3, 10)
    px = img.load()
    for y in (1, 2, 7, 8):
        px[1, y] = NOTCH
    for x in (0, 2):
        px[x, 1] = NOTCH_EDGE
        px[x, 8] = NOTCH_EDGE
    return img


def badge(letter: str) -> Image.Image:
    fill, ring = BADGES[letter]
    img = canvas(7, 7)
    px = img.load()
    for x in range(7):
        for y in range(7):
            px[x, y] = ring if x in (0, 6) or y in (0, 6) else fill
    for y, row in enumerate(LETTERS[letter]):
        for x, c in enumerate(row):
            if c == "X":
                px[x + 1, y + 1] = WHITE
    return img


def frame() -> Image.Image:
    """10 by 10 black ring around an 8 by 8 head."""
    img = canvas(10, 10)
    px = img.load()
    for x in range(10):
        for y in range(10):
            if x in (0, 9) or y in (0, 9):
                px[x, y] = BLACK
    return img


def disc(size: int) -> Image.Image:
    """An outlined disc: white inside, black rim, the rim's corners cut. From 7 px up the fill's
    own corners are cut too, which rounds it; below that a cut fill is a plus sign (a 5 px far
    dot once showed one), so a small fill stays whole."""
    fill = {(x, y) for x in range(1, size - 1) for y in range(1, size - 1)
            if size < 7 or not ((x in (1, size - 2)) and (y in (1, size - 2)))}
    return outlined(size, size, fill)


def chevron(left: bool) -> Image.Image:
    """6 by 7, two pixels thick, pointing off the bar's end."""
    fill = {(3, 1), (4, 1), (2, 2), (3, 2), (1, 3), (2, 3), (2, 4), (3, 4), (3, 5), (4, 5)}
    if not left:
        fill = {(5 - x, y) for x, y in fill}
    return outlined(6, 7, fill)


def write_all() -> dict[str, Image.Image]:
    SPRITES.mkdir(parents=True, exist_ok=True)
    sprites = {
        "bar": bar(), "notch": notch(), "frame": frame(), "dot": disc(10), "player_dot": disc(7),
        "chevron_left": chevron(True), "chevron_right": chevron(False),
    }
    for letter in BADGES:
        sprites["badge_" + letter] = badge(letter)
    for name, img in sprites.items():
        img.save(SPRITES / f"{name}.png")
    (SPRITES / "bar.png.mcmeta").write_text(json.dumps(
        {"gui": {"scaling": {"type": "nine_slice", "width": 16, "height": 10,
                             "border": {"left": 3, "top": 2, "right": 3, "bottom": 2}}}}, indent=2) + "\n")
    return sprites


def preview(sprites: dict[str, Image.Image], out: Path) -> None:
    """A 6x mock of the 102 by 10 bar with every sprite placed as the client places it."""
    img = Image.new("RGBA", (112, 16), (120, 170, 230, 255))
    b = sprites["bar"]
    # nine-slice by hand: caps and the tiled middle
    for x in range(102):
        sx = x if x < 3 else (x - 102 + 16 if x >= 99 else 3 + (x - 3) % 10)
        for y in range(10):
            img.putpixel((5 + x, 3 + y), b.getpixel((sx, y)))
    def put(name, x, y, tint=None):
        s = sprites[name]
        if tint:
            s = Image.merge("RGBA", [s.getchannel(i).point(lambda v, t=tint[i]: v * t // 255) for i in range(3)] + [s.getchannel("A")])
        img.alpha_composite(s, (5 + x, 3 + y))
    put("notch", 50, 0)
    put("badge_n", 10, 2); put("badge_e", 30, 2); put("badge_s", 56, 2); put("badge_w", 84, 2)
    put("dot", 66, 0, (229, 184, 91)); put("frame", 18, 0); put("player_dot", 40, 2, (255, 255, 255))
    put("chevron_left", 1, 2, (229, 184, 91)); put("chevron_right", 95, 2, (134, 182, 138))
    img = img.resize((img.width * 6, img.height * 6), Image.NEAREST)
    img.save(out)


if __name__ == "__main__":
    written = write_all()
    print(f"{len(written)} sprites in {SPRITES}")
    if len(sys.argv) > 2 and sys.argv[1] == "--preview":
        preview(written, Path(sys.argv[2]))
        print("preview", sys.argv[2])
