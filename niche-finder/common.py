"""Shared helpers: config loading, paths, slugs, JSON IO."""
from __future__ import annotations

import json
import re
import unicodedata
from pathlib import Path

import yaml

ROOT = Path(__file__).resolve().parent.parent
PKG = ROOT / "niche-finder"
DATA = ROOT / "data"
DEMOS = ROOT / "demos"
OUTREACH = ROOT / "outreach"

PROSPECTS_FILE = DATA / "prospects.json"
NICHES_FILE = DATA / "niches_ranked.json"
CRM_FILE = DATA / "crm.json"


def load_config(path: str | Path | None = None) -> dict:
    path = Path(path) if path else PKG / "config.yaml"
    with open(path, "r", encoding="utf-8") as fh:
        return yaml.safe_load(fh)


def ensure_dirs() -> None:
    for d in (DATA, DEMOS, OUTREACH):
        d.mkdir(parents=True, exist_ok=True)


def slugify(value: str) -> str:
    value = unicodedata.normalize("NFKD", value).encode("ascii", "ignore").decode()
    value = re.sub(r"[^\w\s-]", "", value).strip().lower()
    return re.sub(r"[-\s]+", "-", value) or "business"


def read_json(path: Path, default):
    if not Path(path).exists():
        return default
    with open(path, "r", encoding="utf-8") as fh:
        return json.load(fh)


def write_json(path: Path, obj) -> None:
    Path(path).parent.mkdir(parents=True, exist_ok=True)
    with open(path, "w", encoding="utf-8") as fh:
        json.dump(obj, fh, indent=2, ensure_ascii=False)
