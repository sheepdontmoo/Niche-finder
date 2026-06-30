#!/usr/bin/env python3
"""
One-time helper to turn a LinkedIn OAuth `code` into an access token,
and to fetch your member URN. Standard library only.

Run locally once (see linkedin-poster/README.md for the full walkthrough):

  1. Get an authorization code by visiting the auth URL this script prints.
  2. Paste the `code` back in.

Env required:
  LINKEDIN_CLIENT_ID, LINKEDIN_CLIENT_SECRET, LINKEDIN_REDIRECT_URI
"""

import json
import os
import ssl
import sys
import urllib.parse
import urllib.request

CTX = ssl.create_default_context()
# Scopes: w_member_social = post as you; openid+profile = read your member id.
# For a company PAGE instead, request w_organization_social (needs the
# Community Management API product approved on your app) and use the org URN.
SCOPES = "openid profile w_member_social"


def main():
    cid = os.environ.get("LINKEDIN_CLIENT_ID")
    secret = os.environ.get("LINKEDIN_CLIENT_SECRET")
    redirect = os.environ.get("LINKEDIN_REDIRECT_URI", "http://localhost:8000/callback")
    if not cid or not secret:
        print("Set LINKEDIN_CLIENT_ID and LINKEDIN_CLIENT_SECRET first.", file=sys.stderr)
        sys.exit(2)

    auth_url = "https://www.linkedin.com/oauth/v2/authorization?" + urllib.parse.urlencode(
        {
            "response_type": "code",
            "client_id": cid,
            "redirect_uri": redirect,
            "scope": SCOPES,
        }
    )
    print("\n1) Open this URL, authorise, then copy the `code` from the redirected URL:\n")
    print(auth_url + "\n")
    code = input("2) Paste the code here: ").strip()

    _, _, payload = (lambda r: (r.status, r.headers, r.read()))(
        urllib.request.urlopen(
            urllib.request.Request(
                "https://www.linkedin.com/oauth/v2/accessToken",
                data=urllib.parse.urlencode(
                    {
                        "grant_type": "authorization_code",
                        "code": code,
                        "client_id": cid,
                        "client_secret": secret,
                        "redirect_uri": redirect,
                    }
                ).encode(),
                headers={"Content-Type": "application/x-www-form-urlencoded"},
            ),
            context=CTX,
        )
    )
    tok = json.loads(payload)
    access = tok.get("access_token")
    print("\nACCESS TOKEN (set as secret LINKEDIN_ACCESS_TOKEN):\n")
    print(access + "\n")

    # Fetch member id -> author URN
    with urllib.request.urlopen(
        urllib.request.Request("https://api.linkedin.com/v2/userinfo", headers={"Authorization": f"Bearer {access}"}),
        context=CTX,
    ) as r:
        me = json.loads(r.read())
    sub = me.get("sub")
    print("AUTHOR URN (set as secret LINKEDIN_AUTHOR_URN):\n")
    print(f"urn:li:person:{sub}\n")
    print("Token expires in ~", tok.get("expires_in"), "seconds. Refresh before then (see README).")


if __name__ == "__main__":
    main()
