import { describe, it } from "node:test";
import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import vm from "node:vm";
import {
  DEFAULT_SETTINGS,
  estimateDelivery,
  formatEstimate,
  type DeliverySettings,
} from "./delivery-date.ts";

const rendererUrl = new URL(
  "../../extensions/delivery-date/assets/delivery-date.js",
  import.meta.url,
);

async function renderStorefront(
  settings: Partial<DeliverySettings>,
  instant: string,
  active = true,
): Promise<{ text: string; hidden: boolean; requests: string[] }> {
  const source = await readFile(rendererUrl, "utf8");
  const config = { textContent: JSON.stringify(settings) };
  const output = { textContent: "" };
  const requests: string[] = [];
  const block = {
    hidden: true,
    style: { display: "" },
    getAttribute(name: string) {
      return name === "data-edd-entitlement-url"
        ? "/apps/supadatewise/entitlement"
        : null;
    },
    querySelector(selector: string) {
      if (selector === "[data-edd-config]") return config;
      if (selector === "[data-edd-output]") return output;
      return null;
    },
  };
  const document = {
    readyState: "complete",
    querySelectorAll: () => [block],
    addEventListener: () => undefined,
  };
  const FixedDate = class extends Date {
    constructor(value?: string | number | Date) {
      const normalized =
        value instanceof Date ? value.getTime() : (value ?? instant);
      super(normalized);
    }

    static now() {
      return new Date(instant).getTime();
    }
  };

  vm.runInNewContext(source, {
    document,
    window: { addEventListener: () => undefined },
    performance: { now: () => 0 },
    AbortController,
    setTimeout: () => 1,
    clearTimeout: () => undefined,
    fetch: async (url: string) => {
      requests.push(url);
      return {
        ok: true,
        json: async () => ({ active, validForMs: active ? 60_000 : 0 }),
      };
    },
    Date: FixedDate,
    Intl,
    JSON,
    Number,
    Array,
    Object,
    isFinite,
  });
  await new Promise((resolve) => setImmediate(resolve));
  return { text: output.textContent, hidden: block.hidden, requests };
}

describe("storefront delivery-date renderer", () => {
  it("matches the server algorithm for a cross-timezone cutoff", async () => {
    const instant = "2026-01-05T14:30:00Z";
    const settings: DeliverySettings = {
      ...DEFAULT_SETTINGS,
      timeZone: "America/New_York",
      template: "Arrives {min} to {max}",
    };
    const expected = formatEstimate(
      estimateDelivery(new Date(instant), settings),
      settings,
    );

    const rendered = await renderStorefront(settings, instant);
    assert.equal(rendered.text, expected);
    assert.equal(rendered.hidden, false);
    assert.deepEqual(rendered.requests, ["/apps/supadatewise/entitlement"]);
  });

  it("stays hidden when Shopify cannot confirm an active subscription", async () => {
    const rendered = await renderStorefront(
      DEFAULT_SETTINGS,
      "2026-01-05T14:30:00Z",
      false,
    );
    assert.equal(rendered.text, "");
    assert.equal(rendered.hidden, true);
  });

  it("keeps legacy settings hidden until the merchant saves a Shopify timezone", async () => {
    const legacySettings: Partial<DeliverySettings> = { ...DEFAULT_SETTINGS };
    delete legacySettings.timeZone;
    const rendered = await renderStorefront(
      legacySettings,
      "2026-01-05T14:30:00Z",
      true,
    );
    assert.equal(rendered.text, "");
    assert.equal(rendered.hidden, true);
    assert.deepEqual(rendered.requests, []);
  });
});
