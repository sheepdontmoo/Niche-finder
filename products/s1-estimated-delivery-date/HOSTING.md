# Production hosting runbook (Fly.io)

The storefront block is hosted by Shopify, but the **admin app (settings +
billing) needs a permanent host**. The dev tunnel only exists while
`npm run dev` runs. This deploys the backend to Fly.io with a persistent
SQLite volume, always-on.

Do this after recording the screencast. Run one command at a time.

## 1. Install the Fly CLI (PowerShell)
```
pwsh -Command "iwr https://fly.io/install.ps1 -useb | iex"
```
Close and reopen PowerShell, then `cd` back to the app folder. Verify:
```
fly version
```

## 2. Sign in / sign up
```
fly auth signup      # or: fly auth login
```
(A card is required by Fly, but this app is a tiny always-on machine — a few
dollars a month.)

## 3. Get your two Shopify credentials
In the Dev Dashboard → your app → **API credentials** (a.k.a. Client
credentials): copy the **Client ID** (this is `SHOPIFY_API_KEY`) and the
**Client secret** (this is `SHOPIFY_API_SECRET`).

## 4. Create the app + volume, set secrets, deploy
`fly.toml` is already in the repo. `app` is set to `edd-supadesign` — if that
name is taken, change it in `fly.toml` first (must be globally unique). Then:
```
fly launch --copy-config --no-deploy
```
Accept the detected settings (it reads fly.toml + the Dockerfile). If it didn't
create the volume, make it:
```
fly volumes create data --region lhr --size 1
```
Set the secrets (replace the two values, and the app name if you changed it):
```
fly secrets set SHOPIFY_API_KEY=<Client ID> SHOPIFY_API_SECRET=<Client secret> SHOPIFY_APP_URL=https://edd-supadesign.fly.dev SCOPES=write_metafields
```
Deploy:
```
fly deploy
```
When it finishes, your app is live at `https://edd-supadesign.fly.dev`.

## 5. Point Shopify at the permanent URL
Edit `shopify.app.toml`:
- `application_url = "https://edd-supadesign.fly.dev"`
- under `[auth]`, set every redirect URL to that domain, e.g.
  `redirect_urls = ["https://edd-supadesign.fly.dev/auth/callback", "https://edd-supadesign.fly.dev/auth/shopify/callback", "https://edd-supadesign.fly.dev/api/auth/callback"]`

Then push the config to Shopify:
```
npm run deploy
```

## 6. Verify
- Reinstall/open the app in the admin — it should load from the Fly URL (no
  `npm run dev` needed).
- Save settings, confirm the storefront block still shows.

## Local dev after this change
`schema.prisma` now uses `env("DATABASE_URL")`. For local `npm run dev`, add
this line to your `.env`:
```
DATABASE_URL="file:dev.sqlite"
```

## Note on cost
A single shared-cpu-1x/512MB always-on machine + a 1GB volume is a few dollars
a month — the price of the app being reliably up for the reviewer and
merchants. (Per the venture rules this is the product's own cost to carry.)
