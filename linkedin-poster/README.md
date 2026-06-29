# LinkedIn auto-poster (no third-party tools)

Posts the queued remote1stjobs content to LinkedIn straight from this repo via the
**official LinkedIn REST API**, scheduled by **GitHub Actions**. No Buffer, no GoHighLevel,
no SaaS — just the repo + the API. Python standard library only (no `pip install`).

```
linkedin-poster/
  queue.json            # the posts: copy + image + schedule + posted state
  post_to_linkedin.py   # uploads image + publishes the next due post
  get_token.py          # one-time: OAuth code -> access token + your author URN
  images/               # drop the post images here (slot1-data.png, ...)
.github/workflows/linkedin-post.yml   # cron that drips the queue
```

## What Claude can and can't do
- ✅ Claude wrote all the code, the queue, and the workflow, and maintains the content.
- ⚠️ **One-time, human-only:** create the LinkedIn developer app and complete the OAuth
  login (it needs *your* LinkedIn credentials — Claude can't log in as you). ~10 minutes.
  After that it runs itself.

## One-time setup (~10 min)

### 1. Create a LinkedIn app
- Go to https://www.linkedin.com/developers/apps → **Create app**. Associate it with the
  remote1stjobs Company Page.
- **Products** → add **"Share on LinkedIn"** and **"Sign In with LinkedIn using OpenID Connect"**.
  (These grant `w_member_social`, `openid`, `profile` — enough to post to *your personal profile*.)
- To post to the **Company Page** instead, add the **"Community Management API"** product
  (approval required) for the `w_organization_social` scope, and use the org URN in step 3.
- **Auth** tab → add an **Authorized redirect URL**: `http://localhost:8000/callback`.
  Note your **Client ID** and **Client Secret**.

### 2. Get an access token + your author URN
Run locally (needs Python 3):
```bash
export LINKEDIN_CLIENT_ID=xxxx
export LINKEDIN_CLIENT_SECRET=xxxx
export LINKEDIN_REDIRECT_URI=http://localhost:8000/callback
python linkedin-poster/get_token.py
```
Open the printed URL, authorise, paste the `code` back. It prints:
- `LINKEDIN_ACCESS_TOKEN` (valid ~60 days)
- `LINKEDIN_AUTHOR_URN` (e.g. `urn:li:person:AbC123`). For a Company Page use
  `urn:li:organization:<your-org-id>` instead.

### 3. Store them as GitHub secrets
Repo → **Settings → Secrets and variables → Actions → New repository secret**:
- `LINKEDIN_ACCESS_TOKEN`
- `LINKEDIN_AUTHOR_URN`

### 4. Add the images
Download each graphic from Magnific and save into `linkedin-poster/images/` with the
filenames referenced in `queue.json` (`slot1-data.png` … `slot5-realjobs.png`), then commit.
(If an image file is missing, the script falls back to `image_url`, or posts text-only.)

## Posting

- **Automatic:** the workflow runs every 30 min and posts the **next due** item.
  - **Drip mode** (default): one post every `drip_interval_minutes` (90) — so the 5 posts
    roll out over the next several hours once enabled.
  - **Pinned times:** set an item's `scheduled_at` to a UTC ISO time
    (e.g. `"2026-06-29T09:07:00Z"`) to post at an exact moment instead.
- **Manual:** repo → **Actions → LinkedIn auto-poster → Run workflow**
  (tick *all_due* to flush everything at once).
- **Local test:** `LINKEDIN_ACCESS_TOKEN=… LINKEDIN_AUTHOR_URN=… python linkedin-poster/post_to_linkedin.py --dry-run`

The workflow commits `queue.json` back after each post so items aren't re-posted.

## Token refresh
Member tokens last ~60 days. Re-run `get_token.py` and update the
`LINKEDIN_ACCESS_TOKEN` secret before expiry. (Refresh tokens are available on request
from LinkedIn; until then, a 60-day manual refresh is the simplest path.)

## How the daily engine plugs in
`/linkedin` generates the posts + graphics; add each finished post to `queue.json`
(copy + image path), and this poster ships them. Same repo, no extra tools.
