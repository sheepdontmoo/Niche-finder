import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import test from "node:test";

const routesUrl = new URL("../routes/", import.meta.url);

test("robots, sitemap, and llms endpoints expose only public guidance routes", async () => {
  const [robots, sitemap, llms] = await Promise.all([
    readFile(new URL("robots[.]txt.ts", routesUrl), "utf8"),
    readFile(new URL("sitemap[.]xml.ts", routesUrl), "utf8"),
    readFile(new URL("llms[.]txt.ts", routesUrl), "utf8"),
  ]);

  assert.match(robots, /Disallow: \/app/);
  assert.match(robots, /Disallow: \/auth/);
  assert.match(robots, /Disallow: \/webhooks/);
  assert.match(sitemap, /\["\/", "\/support", "\/privacy"\]/);
  assert.doesNotMatch(sitemap, /"\/app"|"\/auth"|"\/webhooks"/);
  assert.match(llms, /Shopify App Store listing/);
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
