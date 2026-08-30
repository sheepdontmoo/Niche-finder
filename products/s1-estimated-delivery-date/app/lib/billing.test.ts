import { describe, it } from "node:test";
import assert from "node:assert/strict";
import {
  buildPricingPlansUrl,
  requireActiveAppPayment,
  requireShopifyAppHandle,
} from "./billing.ts";

const APP_HANDLE = "verified-app-handle";

describe("Shopify App Pricing gate", () => {
  it("builds Shopify's hosted plan-selection URL from an authenticated shop", () => {
    assert.equal(
      buildPricingPlansUrl("example-store.myshopify.com", APP_HANDLE),
      `https://admin.shopify.com/store/example-store/charges/${APP_HANDLE}/pricing_plans`,
    );
  });

  it("rejects a non-Shopify domain", () => {
    assert.throws(() => buildPricingPlansUrl("example.com", APP_HANDLE));
  });

  it("requires the provider-confirmed App Home handle", () => {
    assert.throws(() => requireShopifyAppHandle(undefined));
    assert.throws(() => requireShopifyAppHandle("public/listing/slug"));
    assert.equal(requireShopifyAppHandle(APP_HANDLE), APP_HANDLE);
  });

  it("allows an active subscription to enter the app", async () => {
    let redirected = false;
    const result = await requireActiveAppPayment(
      true,
      () => {
        redirected = true;
        return new Response(null, { status: 302 });
      },
      "example-store.myshopify.com",
      APP_HANDLE,
    );

    assert.equal(result, null);
    assert.equal(redirected, false);
  });

  it("redirects an unpaid install at the top-level frame", async () => {
    let destination = "";
    let target = "";
    const response = await requireActiveAppPayment(
      false,
      (url, options) => {
        destination = url;
        target = options.target;
        return new Response(null, { status: 302, headers: { location: url } });
      },
      "example-store.myshopify.com",
      APP_HANDLE,
    );

    assert.equal(
      destination,
      buildPricingPlansUrl("example-store.myshopify.com", APP_HANDLE),
    );
    assert.equal(target, "_top");
    assert.equal(response?.status, 302);
  });
});
