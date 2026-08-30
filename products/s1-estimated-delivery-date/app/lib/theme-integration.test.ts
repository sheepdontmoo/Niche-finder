import { describe, it } from "node:test";
import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";

const blockUrl = new URL(
  "../../extensions/delivery-date/blocks/delivery_date.liquid",
  import.meta.url,
);

describe("theme app block contract", () => {
  it("reads the app-owned shop metafield using Shopify's reserved namespace syntax", async () => {
    const liquid = await readFile(blockUrl, "utf8");
    assert.match(liquid, /shop\.metafields\["\$app"\]\.settings/);
    assert.doesNotMatch(liquid, /shop\.metafields\.app\.settings/);
  });

  it("lets the extension schema load the storefront script exactly once", async () => {
    const liquid = await readFile(blockUrl, "utf8");
    assert.match(liquid, /"javascript": "delivery-date\.js"/);
    assert.doesNotMatch(liquid, /<script[^>]+delivery-date\.js/);
  });

  it("starts hidden with no usable fallback and requires the app proxy", async () => {
    const liquid = await readFile(blockUrl, "utf8");
    assert.match(liquid, /data-edd-entitlement-url="\/apps\/supadatewise\/entitlement"/);
    assert.match(liquid, /data-edd[\s\S]*?hidden/);
    assert.match(liquid, /\{ "enabled": false \}/);
    assert.doesNotMatch(liquid, /"id": "processing_days"/);
  });
});
