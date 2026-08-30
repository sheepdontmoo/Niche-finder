import { describe, it } from "node:test";
import assert from "node:assert/strict";
import {
  buildThemeActivationUrl,
  DELIVERY_DATE_BLOCK_HANDLE,
} from "./theme-activation.ts";

describe("buildThemeActivationUrl", () => {
  it("targets the authenticated shop's product template and app block", () => {
    const value = buildThemeActivationUrl(
      "example.myshopify.com",
      "public-client-id",
    );
    assert.ok(value);

    const url = new URL(value);
    assert.equal(url.origin, "https://example.myshopify.com");
    assert.equal(url.pathname, "/admin/themes/current/editor");
    assert.equal(url.searchParams.get("template"), "product");
    assert.equal(
      url.searchParams.get("addAppBlockId"),
      `public-client-id/${DELIVERY_DATE_BLOCK_HANDLE}`,
    );
    assert.equal(url.searchParams.get("target"), "mainSection");
  });

  it("fails closed when the public client ID is unavailable", () => {
    assert.equal(buildThemeActivationUrl("example.myshopify.com", undefined), null);
    assert.equal(buildThemeActivationUrl("example.myshopify.com", "  "), null);
  });
});
