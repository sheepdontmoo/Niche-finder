#!/usr/bin/env python3
"""Litmas — Play Store / App Store marketing assets.

One brand colour (orange), the test-strip mark, and real app captures. No
secondary palette, no invented stats, no scarcity copy.

Inputs:  raw app screenshots (1170x2532) via --shots-dir.
Outputs: store-assets/feature-graphic.png (1024x500) and four branded
         1080x1920 listing screenshots.

Display face: Anton (SIL Open Font License 1.1), vendored at
scripts/fonts/Anton-Regular.ttf.
"""
import argparse
import os
from PIL import Image, ImageDraw, ImageFont, ImageFilter

GROUND = (11, 13, 16)
SURFACE = (20, 23, 28)
LINE = (38, 44, 53)
BRAND = (255, 106, 0)
INK = (236, 239, 243)
INK_DIM = (138, 147, 160)
UNREAD = 0.20

HERE = os.path.dirname(os.path.abspath(__file__))
DEFAULT_DISPLAY = os.path.join(HERE, "fonts", "Anton-Regular.ttf")
BODY = "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"

p = argparse.ArgumentParser()
p.add_argument("--shots-dir", required=True)
p.add_argument("--font", default=DEFAULT_DISPLAY)
p.add_argument("--out", default="store-assets")
args = p.parse_args()
os.makedirs(args.out, exist_ok=True)


def font(path, size):
    return ImageFont.truetype(path, size)


def draw_strip(img, cx, cy, bar_w, bar_h, level="top"):
    """The logo: one vertical bar with a single solid band."""
    left, top = cx - bar_w / 2, cy - bar_h / 2
    box = [left, top, left + bar_w, top + bar_h]
    mask = Image.new("L", img.size, 0)
    ImageDraw.Draw(mask).rounded_rectangle(box, radius=bar_w / 2, fill=255)

    img.paste(
        Image.new("RGB", img.size, BRAND),
        (0, 0),
        mask.point(lambda v: int(v * UNREAD)),
    )

    band = bar_h / 3
    offset = {"top": 0, "middle": band, "bottom": 2 * band}[level]
    solid = mask.copy()
    d = ImageDraw.Draw(solid)
    d.rectangle([0, 0, img.size[0], top + offset], fill=0)
    d.rectangle([0, top + offset + band, img.size[0], img.size[1]], fill=0)
    img.paste(Image.new("RGB", img.size, BRAND), (0, 0), solid)


def feature_graphic():
    W, H = 1024, 500
    img = Image.new("RGB", (W, H), GROUND)

    draw_strip(img, 148, H / 2, bar_w=76, bar_h=210)

    d = ImageDraw.Draw(img)
    x = 246
    f_name = font(args.font, 104)
    d.text((x, 176), "LITMAS", font=f_name, fill=INK, anchor="ls")

    f_slogan = font(BODY, 40)
    d.text((x, 244), "Check every trade.", font=f_slogan, fill=BRAND, anchor="ls")

    f_sub = font(BODY, 25)
    d.text((x, 306), "AI candlestick & chart detector", font=f_sub, fill=INK, anchor="ls")
    d.text(
        (x, 352),
        "Buy, hold or sell — with a confidence score",
        font=f_sub,
        fill=INK_DIM,
        anchor="ls",
    )
    img.save(f"{args.out}/feature-graphic.png")


def branded_shot(src, headline, sub, out_name, crop=None, level="top"):
    W, H = 1080, 1920
    img = Image.new("RGB", (W, H), GROUND)
    d = ImageDraw.Draw(img)

    f_head = font(args.font, 104)
    f_sub = font(BODY, 36)
    d.text((W / 2, 132), headline, font=f_head, fill=INK, anchor="mm")
    d.text((W / 2, 250), sub, font=f_sub, fill=INK_DIM, anchor="mm")

    # brand rule under the headline — the strip, laid flat
    rule_w = 132
    d.rounded_rectangle(
        [W / 2 - rule_w / 2, 196, W / 2 + rule_w / 2, 206], radius=5, fill=BRAND
    )

    shot = Image.open(src)
    if crop:
        shot = shot.crop(crop)
    avail_h = H - 360 - 70
    scale = min((W - 210) / shot.width, avail_h / shot.height)
    shot = shot.resize(
        (int(shot.width * scale), int(shot.height * scale)), Image.LANCZOS
    )
    x, y = (W - shot.width) // 2, 360

    radius = 42
    shadow = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    ImageDraw.Draw(shadow).rounded_rectangle(
        [x - 6, y + 16, x + shot.width + 6, y + shot.height + 28],
        radius=radius,
        fill=(0, 0, 0, 170),
    )
    img = Image.alpha_composite(
        img.convert("RGBA"), shadow.filter(ImageFilter.GaussianBlur(20))
    ).convert("RGB")

    mask = Image.new("L", shot.size, 0)
    ImageDraw.Draw(mask).rounded_rectangle(
        [0, 0, shot.width, shot.height], radius=radius, fill=255
    )
    img.paste(shot, (x, y), mask)
    ImageDraw.Draw(img).rounded_rectangle(
        [x, y, x + shot.width, y + shot.height], radius=radius, outline=LINE, width=4
    )
    img.save(f"{args.out}/{out_name}")


feature_graphic()
S = args.shots_dir
branded_shot(
    f"{S}/1-home.png",
    "CHECK EVERY TRADE",
    "Snap any chart — stocks, crypto, forex",
    "screenshot-1.png",
)
branded_shot(
    f"{S}/3-results-top.png",
    "BUY, HOLD OR SELL",
    "A plain-language call, with a confidence score",
    "screenshot-2.png",
)
branded_shot(
    f"{S}/3b-results-full.png",
    "PATTERNS & LEVELS",
    "What's forming, and the prices that matter",
    "screenshot-3.png",
    crop=(0, 2530, 1170, 4430),
)
branded_shot(
    f"{S}/6-history.png",
    "EVERY SCAN, SAVED",
    "Look back at what you were seeing at the time",
    "screenshot-4.png",
)
print("Wrote feature-graphic.png + screenshot-1..4.png to", args.out)
