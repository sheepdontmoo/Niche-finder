#!/usr/bin/env python3
"""Generate the source icon/splash art used by @capacitor/assets.

Produces resources/icon.png (1024x1024), resources/splash.png and
resources/splash-dark.png (2732x2732): a dark chart backdrop with green/red
candlesticks and a rising accent trendline.
"""
from PIL import Image, ImageDraw
import os

BG = (11, 15, 20)          # --bg
SURFACE = (26, 36, 50)     # --surface-2
GREEN = (34, 211, 167)     # --accent
RED = (244, 88, 107)       # --red


def draw_chart(d: ImageDraw.ImageDraw, x0, y0, size):
    """Draw candlesticks + trendline inside a square region."""
    # (x_frac, top_frac, bottom_frac, body_top_frac, body_bot_frac, bullish)
    candles = [
        (0.14, 0.62, 0.92, 0.68, 0.86, False),
        (0.30, 0.50, 0.84, 0.56, 0.78, True),
        (0.46, 0.40, 0.74, 0.44, 0.68, True),
        (0.62, 0.28, 0.66, 0.34, 0.60, False),
        (0.78, 0.14, 0.56, 0.20, 0.46, True),
    ]
    body_w = size * 0.085
    wick_w = max(3, int(size * 0.018))
    for xf, tf, bf, btf, bbf, bull in candles:
        cx = x0 + xf * size
        color = GREEN if bull else RED
        d.rectangle(
            [cx - wick_w / 2, y0 + tf * size, cx + wick_w / 2, y0 + bf * size],
            fill=color,
        )
        d.rounded_rectangle(
            [cx - body_w / 2, y0 + btf * size, cx + body_w / 2, y0 + bbf * size],
            radius=size * 0.015,
            fill=color,
        )
    # rising trendline with an arrowhead
    lw = max(6, int(size * 0.030))
    pts = [
        (x0 + 0.06 * size, y0 + 0.80 * size),
        (x0 + 0.38 * size, y0 + 0.60 * size),
        (x0 + 0.56 * size, y0 + 0.66 * size),
        (x0 + 0.92 * size, y0 + 0.22 * size),
    ]
    d.line(pts, fill=(232, 238, 246), width=lw, joint="curve")
    ax, ay = pts[-1]
    ah = size * 0.085
    d.polygon(
        [(ax + lw * 0.4, ay - lw * 0.6), (ax - ah * 0.55, ay - lw * 0.2),
         (ax + lw * 0.1, ay + ah * 0.55)],
        fill=(232, 238, 246),
    )


def make_icon(path, px=1024):
    img = Image.new("RGB", (px, px), BG)
    d = ImageDraw.Draw(img)
    # subtle rounded inner panel for depth
    m = px * 0.10
    d.rounded_rectangle([m, m, px - m, px - m], radius=px * 0.12, fill=SURFACE)
    inner = px * 0.16
    draw_chart(d, inner, inner, px - 2 * inner)
    img.save(path)


def make_splash(path, px=2732):
    img = Image.new("RGB", (px, px), BG)
    d = ImageDraw.Draw(img)
    size = px * 0.28
    x0 = (px - size) / 2
    y0 = (px - size) / 2 - px * 0.02
    draw_chart(d, x0, y0, size)
    img.save(path)


os.makedirs("resources", exist_ok=True)
make_icon("resources/icon.png")
make_splash("resources/splash.png")
make_splash("resources/splash-dark.png")
print("Wrote resources/icon.png, splash.png, splash-dark.png")
