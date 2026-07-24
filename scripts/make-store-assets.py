#!/usr/bin/env python3
"""Generate Play Store marketing assets for ChartDetector.

Inputs:  raw app screenshots (1170x2532) passed via --shots-dir, and a display
         font via --font (falls back to DejaVu Sans Bold).
Outputs: store-assets/feature-graphic.png (1024x500) and four branded
         1080x1920 listing screenshots with bold headline captions.
"""
import argparse
import os
from PIL import Image, ImageDraw, ImageFont, ImageFilter

BG = (11, 15, 20)
SURFACE = (18, 25, 36)
GREEN = (34, 211, 167)
RED = (244, 88, 107)
TEXT = (232, 238, 246)
MUTED = (139, 155, 176)

p = argparse.ArgumentParser()
p.add_argument("--shots-dir", required=True)
p.add_argument("--font", default="/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf")
p.add_argument("--out", default="store-assets")
args = p.parse_args()
os.makedirs(args.out, exist_ok=True)

DISPLAY = args.font
BODY = "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"


def font(path, size):
    return ImageFont.truetype(path, size)


def draw_candles(d, x0, y0, size):
    candles = [
        (0.10, 0.60, 0.95, 0.68, 0.88, False),
        (0.30, 0.45, 0.82, 0.52, 0.74, True),
        (0.50, 0.34, 0.70, 0.40, 0.62, True),
        (0.70, 0.20, 0.58, 0.28, 0.50, False),
        (0.90, 0.05, 0.46, 0.12, 0.36, True),
    ]
    body_w = size * 0.11
    wick_w = max(2, int(size * 0.025))
    for xf, tf, bf, btf, bbf, bull in candles:
        cx = x0 + xf * size
        color = GREEN if bull else RED
        d.rectangle([cx - wick_w / 2, y0 + tf * size, cx + wick_w / 2, y0 + bf * size], fill=color)
        d.rounded_rectangle(
            [cx - body_w / 2, y0 + btf * size, cx + body_w / 2, y0 + bbf * size],
            radius=size * 0.02, fill=color,
        )


def gradient_bar(width, height):
    bar = Image.new("RGB", (width, height))
    for x in range(width):
        t = x / max(1, width - 1)
        c = tuple(int(GREEN[i] * (1 - t) + (20, 165, 131)[i] * t) for i in range(3))
        for y in range(height):
            bar.putpixel((x, y), c)
    return bar


def feature_graphic():
    W, H = 1024, 500
    img = Image.new("RGB", (W, H), BG)
    d = ImageDraw.Draw(img)
    # faint oversized candles on the right for depth
    draw_candles(d, W * 0.60, H * 0.10, H * 0.80)
    overlay = Image.new("RGBA", (W, H), (11, 15, 20, 140))
    img = Image.alpha_composite(img.convert("RGBA"), overlay).convert("RGB")
    d = ImageDraw.Draw(img)
    # crisp brand candles chip on the left
    chip = H * 0.30
    d.rounded_rectangle([60, 70, 60 + chip, 70 + chip], radius=chip * 0.22, fill=SURFACE)
    draw_candles(d, 60 + chip * 0.15, 70 + chip * 0.15, chip * 0.70)
    # wordmark
    f_big = font(DISPLAY, 92)
    d.text((60 + chip + 34, 70 + chip / 2), "CHART", font=f_big, fill=TEXT, anchor="lm")
    w = d.textlength("CHART", font=f_big)
    d.text((60 + chip + 34 + w, 70 + chip / 2), "DETECTOR", font=f_big, fill=GREEN, anchor="lm")
    # tagline
    f_tag = font(BODY, 34)
    d.text((60, 300), "Snap any trading chart.", font=f_tag, fill=TEXT)
    d.text((60, 352), "Get an instant AI read.", font=f_tag, fill=TEXT)
    f_sub = font(BODY, 24)
    d.text((60, 424), "Trend  ·  Patterns  ·  Key levels  ·  Trade ideas", font=f_sub, fill=MUTED)
    img.save(f"{args.out}/feature-graphic.png")


def branded_shot(src, headline, sub, out_name, crop=None):
    W, H = 1080, 1920
    img = Image.new("RGB", (W, H), BG)
    d = ImageDraw.Draw(img)
    # header
    f_head = font(DISPLAY, 108)
    f_sub = font(BODY, 38)
    d.text((W / 2, 130), headline, font=f_head, fill=TEXT, anchor="mm")
    bar = gradient_bar(int(d.textlength(headline, font=f_head) * 0.5), 10)
    img.paste(bar, (int(W / 2 - bar.width / 2), 208))
    d.text((W / 2, 272), sub, font=f_sub, fill=MUTED, anchor="mm")

    shot = Image.open(src)
    if crop:
        shot = shot.crop(crop)
    # fit into remaining area
    avail_h = H - 380 - 60
    scale = min((W - 200) / shot.width, avail_h / shot.height)
    shot = shot.resize((int(shot.width * scale), int(shot.height * scale)), Image.LANCZOS)
    x = (W - shot.width) // 2
    y = 380

    # soft shadow + rounded frame
    radius = 44
    shadow = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    sd = ImageDraw.Draw(shadow)
    sd.rounded_rectangle([x - 6, y + 14, x + shot.width + 6, y + shot.height + 26], radius=radius, fill=(0, 0, 0, 160))
    shadow = shadow.filter(ImageFilter.GaussianBlur(18))
    img = Image.alpha_composite(img.convert("RGBA"), shadow).convert("RGB")

    mask = Image.new("L", shot.size, 0)
    ImageDraw.Draw(mask).rounded_rectangle([0, 0, shot.width, shot.height], radius=radius, fill=255)
    img.paste(shot, (x, y), mask)
    d = ImageDraw.Draw(img)
    d.rounded_rectangle([x, y, x + shot.width, y + shot.height], radius=radius, outline=(35, 50, 71), width=4)
    img.save(f"{args.out}/{out_name}")


feature_graphic()
S = args.shots_dir
branded_shot(f"{S}/1-home.png", "SNAP ANY CHART", "Stocks · Crypto · Forex — camera or screenshot", "screenshot-1.png")
branded_shot(f"{S}/3-results-top.png", "INSTANT AI VERDICT", "Trend, bias & confidence in seconds", "screenshot-2.png")
branded_shot(f"{S}/3b-results-full.png", "PATTERNS & LEVELS", "What's forming — and the prices that matter", "screenshot-3.png", crop=(0, 1880, 1170, 3770))
branded_shot(f"{S}/3b-results-full.png", "IDEAS, WITH RISK", "Entry, invalidation, targets & honest caveats", "screenshot-4.png", crop=(0, 3230, 1170, 5120))
print("Wrote feature-graphic.png + screenshot-1..4.png to", args.out)
