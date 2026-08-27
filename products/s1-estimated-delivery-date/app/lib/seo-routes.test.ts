import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import test from "node:test";

import {
  ADSENSE_SELLER_RECORD,
  shouldLoadAdsense,
} from "./adsense.ts";

const routesUrl = new URL("../routes/", import.meta.url);

test("robots, sitemap, and llms endpoints expose only public guidance routes", async () => {
  const [robots, sitemap, llms, ads] = await Promise.all([
    readFile(new URL("robots[.]txt.ts", routesUrl), "utf8"),
    readFile(new URL("sitemap[.]xml.ts", routesUrl), "utf8"),
    readFile(new URL("llms[.]txt.ts", routesUrl), "utf8"),
    readFile(new URL("ads[.]txt.ts", routesUrl), "utf8"),
  ]);

  assert.match(robots, /Disallow: \/app/);
  assert.match(robots, /Disallow: \/auth/);
  assert.match(robots, /Disallow: \/webhooks/);
  assert.match(sitemap, /\["\/", "\/support", "\/privacy"\]/);
  assert.doesNotMatch(sitemap, /"\/app"|"\/auth"|"\/webhooks"/);
  assert.match(llms, /Shopify App Store listing/);
  assert.match(ads, /ADSENSE_SELLER_RECORD/);
  assert.equal(
    ADSENSE_SELLER_RECORD,
    "google.com, pub-6271772629726247, DIRECT, f08c47fec0942fa0"
  );
});

test("AdSense is limited to public guidance pages", async () => {
  const [root, privacy] = await Promise.all([
    readFile(new URL("../root.tsx", import.meta.url), "utf8"),
    readFile(new URL("privacy/route.tsx", routesUrl), "utf8"),
  ]);

  assert.match(root, /google-adsense-account/);
  assert.match(root, /pagead2\.googlesyndication\.com\/pagead\/js\/adsbygoogle\.js/);
  assert.match(root, /CONSENT_DEFAULT/);

  for (const publicPath of ["/", "/support", "/support/", "/privacy"]) {
    assert.equal(shouldLoadAdsense(publicPath), true, publicPath);
  }

  for (const protectedPath of [
    "/app",
    "/app/settings",
    "/auth/login",
    "/auth/callback",
    "/webhooks/app/uninstalled",
    "/ads.txt",
    "/robots.txt",
    "/unknown",
  ]) {
    assert.equal(shouldLoadAdsense(protectedPath), false, protectedPath);
  }

  assert.match(privacy, /AdSense is not loaded in/);
  assert.match(privacy, /embedded Shopify app/);
});

test("public pages use canonical URLs and the not-found route remains canonical-free", async () => {
  const [root, support, privacy, notFound] = await Promise.all([
    readFile(new URL("_index/route.tsx", routesUrl), "utf8"),
    readFile(new URL("support/route.tsx", routesUrl), "utf8"),
    readFile(new URL("privacy/route.tsx", routesUrl), "utf8"),
    readFile(new URL("$.tsx", routesUrl), "utf8"),
  ]);

  assert.match(root, /rel: "canonical", href: SITE_URL/);
  assert.match(support, /rel: "canonical", href: `\$\{SITE_URL\}\/support`/);
  assert.match(privacy, /rel: "canonical", href: `\$\{SITE_URL\}\/privacy`/);
  assert.match(notFound, /status: 404/);
  assert.match(notFound, /X-Robots-Tag/);
  assert.match(notFound, /noindex, nofollow/);
  assert.doesNotMatch(notFound, /canonical/);
});
