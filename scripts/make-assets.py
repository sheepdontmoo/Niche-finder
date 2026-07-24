#!/usr/bin/env python3
"""Litmas brand mark — the source art for app icons and splash screens.

The logo is a single test strip: one vertical bar, solid top third, in the one
brand colour. Nothing else. It has to read at favicon size, so there is no
detail smaller than the bar itself.

Outputs resources/icon.png (1024) and resources/splash{,-dark}.png (2732),
which @capacitor/assets expands into the per-density Android and iOS sets.
"""
from PIL import Image, ImageDraw
import os

GROUND = (11, 13, 16)      # --bg
BRAND = (255, 106, 0)      # --brand  #FF6A00
UNREAD = 0.20              # opacity of the un-reacted part of the strip


def draw_strip(img, cx, cy, bar_w, bar_h):
    """One test strip, centred on (cx, cy), with the top third solid."""
    left, top = cx - bar_w / 2, cy - bar_h / 2
    box = [left, top, left + bar_w, top + bar_h]
    radius = bar_w / 2

    # Full strip, un-reacted.
    mask = Image.new("L", img.size, 0)
    ImageDraw.Draw(mask).rounded_rectangle(box, radius=radius, fill=255)
    dim = Image.new("L", img.size, 0)
    dim.paste(mask.point(lambda v: int(v * UNREAD)), (0, 0))
    img.paste(Image.new("RGB", img.size, BRAND), (0, 0), dim)

    # The reading: top third, solid, clipped to the strip's rounded shape.
    top_mask = mask.copy()
    ImageDraw.Draw(top_mask).rectangle(
        [0, top + bar_h / 3, img.size[0], img.size[1]], fill=0
    )
    img.paste(Image.new("RGB", img.size, BRAND), (0, 0), top_mask)


def make_icon(path, px=1024):
    img = Image.new("RGB", (px, px), GROUND)
    draw_strip(img, px / 2, px / 2, bar_w=px * 0.21, bar_h=px * 0.58)
    img.save(path)


def make_splash(path, px=2732):
    img = Image.new("RGB", (px, px), GROUND)
    draw_strip(img, px / 2, px / 2, bar_w=px * 0.055, bar_h=px * 0.15)
    img.save(path)


os.makedirs("resources", exist_ok=True)
make_icon("resources/icon.png")
make_splash("resources/splash.png")
make_splash("resources/splash-dark.png")

# Favicon-size legibility check — the mark must survive this.
Image.open("resources/icon.png").resize((16, 16), Image.LANCZOS).resize(
    (128, 128), Image.NEAREST
).save("resources/icon-16px-check.png")

print("Wrote resources/icon.png, splash.png, splash-dark.png (+ 16px check)")
