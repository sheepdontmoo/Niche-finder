#!/usr/bin/env python3
"""
Post remote1stjobs content to LinkedIn via the official REST API.
No third-party services, no pip installs — Python standard library only.

Reads linkedin-poster/queue.json, finds the next item that is due, uploads its
image, publishes the post, and marks it posted (writing the state back to queue.json).

Auth (set as environment variables / GitHub Actions secrets):
  LINKEDIN_ACCESS_TOKEN   OAuth token with w_member_social (personal profile)
                          or w_organization_social (company page).
  LINKEDIN_AUTHOR_URN     e.g. urn:li:person:XXXXXXXX  or  urn:li:organization:12345678

Usage:
  python post_to_linkedin.py            # post the next due item (drip/scheduled)
  python post_to_linkedin.py --all-due  # post every item that is due right now
  python post_to_linkedin.py --dry-run  # show what would post, change nothing
"""

import json
import os
import sys
import ssl
import time
import urllib.request
import urllib.error
from datetime import datetime, timezone

HERE = os.path.dirname(os.path.abspath(__file__))
QUEUE_PATH = os.path.join(HERE, "queue.json")
API_VERSION = "202405"
CTX = ssl.create_default_context()


def now_utc():
    return datetime.now(timezone.utc)


def parse_iso(s):
    if not s:
        return None
    return datetime.fromisoformat(s.replace("Z", "+00:00"))


def load_queue():
    with open(QUEUE_PATH, "r", encoding="utf-8") as f:
        return json.load(f)


def save_queue(q):
    with open(QUEUE_PATH, "w", encoding="utf-8") as f:
        json.dump(q, f, indent=2, ensure_ascii=False)
        f.write("\n")


def http(method, url, token, data=None, headers=None, raw=False):
    h = {"Authorization": f"Bearer {token}"}
    if headers:
        h.update(headers)
    body = None
    if data is not None and not raw:
        body = json.dumps(data).encode("utf-8")
        h.setdefault("Content-Type", "application/json")
    elif raw:
        body = data
    req = urllib.request.Request(url, data=body, headers=h, method=method)
    try:
        with urllib.request.urlopen(req, context=CTX) as resp:
            payload = resp.read()
            return resp.status, dict(resp.headers), payload
    except urllib.error.HTTPError as e:
        return e.code, dict(e.headers), e.read()


def read_image_bytes(item):
    """Prefer a local file in images/; fall back to image_url."""
    local = item.get("image")
    if local:
        p = local if os.path.isabs(local) else os.path.join(HERE, local)
        if os.path.exists(p):
            with open(p, "rb") as f:
                return f.read()
    url = item.get("image_url")
    if url and url.lower().endswith((".png", ".jpg", ".jpeg")):
        with urllib.request.urlopen(url, context=CTX) as r:
            return r.read()
    return None  # no usable image -> text-only post


def upload_image(token, author_urn, img_bytes):
    status, _, payload = http(
        "POST",
        "https://api.linkedin.com/rest/images?action=initializeUpload",
        token,
        data={"initializeUploadRequest": {"owner": author_urn}},
        headers={"LinkedIn-Version": API_VERSION, "X-Restli-Protocol-Version": "2.0.0"},
    )
    if status not in (200, 201):
        raise RuntimeError(f"initializeUpload failed [{status}]: {payload[:400]}")
    value = json.loads(payload)["value"]
    upload_url = value["uploadUrl"]
    image_urn = value["image"]
    up_status, _, up_payload = http(
        "PUT", upload_url, token, data=img_bytes, headers={"Content-Type": "application/octet-stream"}, raw=True
    )
    if up_status not in (200, 201):
        raise RuntimeError(f"image upload failed [{up_status}]: {up_payload[:400]}")
    return image_urn


def publish_post(token, author_urn, commentary, image_urn=None):
    body = {
        "author": author_urn,
        "commentary": commentary,
        "visibility": "PUBLIC",
        "distribution": {
            "feedDistribution": "MAIN_FEED",
            "targetEntities": [],
            "thirdPartyDistributionChannels": [],
        },
        "lifecycleState": "PUBLISHED",
        "isReshareDisabledByAuthor": False,
    }
    if image_urn:
        body["content"] = {"media": {"id": image_urn}}
    status, headers, payload = http(
        "POST",
        "https://api.linkedin.com/rest/posts",
        token,
        data=body,
        headers={"LinkedIn-Version": API_VERSION, "X-Restli-Protocol-Version": "2.0.0"},
    )
    if status not in (200, 201):
        raise RuntimeError(f"post failed [{status}]: {payload[:600]}")
    return headers.get("x-restli-id") or headers.get("X-RestLi-Id") or "posted"


def is_due(item, q, now):
    if item.get("posted"):
        return False
    sched = parse_iso(item.get("scheduled_at"))
    if sched:
        return now >= sched
    # drip mode: respect the interval since last post
    if q["meta"].get("mode") == "drip":
        last = parse_iso(q["meta"].get("last_posted_at"))
        if last is None:
            return True
        gap = (now - last).total_seconds() / 60.0
        return gap >= float(q["meta"].get("drip_interval_minutes", 90))
    return True


def main():
    args = set(sys.argv[1:])
    dry = "--dry-run" in args
    all_due = "--all-due" in args

    token = os.environ.get("LINKEDIN_ACCESS_TOKEN")
    author = os.environ.get("LINKEDIN_AUTHOR_URN")
    if not dry and (not token or not author):
        print("ERROR: set LINKEDIN_ACCESS_TOKEN and LINKEDIN_AUTHOR_URN", file=sys.stderr)
        sys.exit(2)

    q = load_queue()
    now = now_utc()
    due = [p for p in q["posts"] if is_due(p, q, now)]
    if not due:
        print("Nothing due right now.")
        return
    if not all_due:
        due = due[:1]  # drip: one per run

    for item in due:
        print(f"[{item['id']}] {item['pillar']} — {'DRY RUN' if dry else 'posting'}")
        if dry:
            continue
        img = read_image_bytes(item)
        image_urn = upload_image(token, author, img) if img else None
        if image_urn is None:
            print(f"  (no local/usable image — posting text-only for {item['id']})")
        posted_id = publish_post(token, author, item["commentary"], image_urn)
        item["posted"] = True
        item["posted_id"] = posted_id
        q["meta"]["last_posted_at"] = now.replace(microsecond=0).isoformat().replace("+00:00", "Z")
        save_queue(q)
        print(f"  ✓ posted ({posted_id})")
        time.sleep(2)

    remaining = sum(1 for p in q["posts"] if not p.get("posted"))
    print(f"Done. {remaining} post(s) still queued.")


if __name__ == "__main__":
    main()
