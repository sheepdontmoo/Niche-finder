import { describe, it } from "node:test";
import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import vm from "node:vm";
import { DEFAULT_SETTINGS } from "./delivery-date.ts";
import {
  clearPartnerSubscriptionCacheForTests,
  getStorefrontEntitlement,
} from "./partner-subscription.server.ts";

const CONFIG = {
  organizationId: "12345",
  accessToken: "test-token-not-a-real-secret",
  appId: "gid://shopify/App/67890",
  appHandle: "verified-app-handle",
};
type Reply = { ok: boolean; json: () => Promise<unknown> };
const reply = (body: unknown): Reply => ({ ok: true, json: async () => body });
const flush = () => new Promise<void>((resolve) => setImmediate(resolve));

async function storefront(
  respond: (elapsed: number) => Promise<Reply> = async () =>
    reply({ active: true, validForMs: 60_000 }),
  blockCount = 1,
) {
  const source = await readFile(
    new URL(
      "../../extensions/delivery-date/assets/delivery-date.js",
      import.meta.url,
    ),
    "utf8",
  );
  let elapsed = 0,
    timerId = 0;
  const timers = new Map<number, { at: number; run: () => void }>();
  const listeners = new Map<
    string,
    Array<(event: { persisted?: boolean }) => void>
  >();
  const requests: Array<{
    url: string;
    elapsed: number;
    signal: AbortSignal;
    cache: string;
  }> = [];
  const config = { textContent: JSON.stringify(DEFAULT_SETTINGS) };
  const blocks = Array.from({ length: blockCount }, () => {
    const output = { textContent: "" };
    return {
      hidden: true,
      style: { display: "" },
      output,
      getAttribute: () => "/apps/supadatewise/entitlement",
      querySelector: (selector: string) =>
        selector === "[data-edd-config]" ? config : output,
    };
  });
  const on = (
    name: string,
    callback: (event: { persisted?: boolean }) => void,
  ) => {
    listeners.set(name, [...(listeners.get(name) ?? []), callback]);
  };
  const document = {
    readyState: "complete",
    hidden: false,
    querySelectorAll: () => blocks,
    addEventListener: on,
  };
  vm.runInNewContext(source, {
    document,
    window: { addEventListener: on },
    performance: { now: () => elapsed },
    AbortController,
    fetch: async (
      url: string,
      options: { signal: AbortSignal; cache: string },
    ) => {
      requests.push({
        url,
        elapsed,
        signal: options.signal,
        cache: options.cache,
      });
      return respond(elapsed);
    },
    Date,
    Intl,
    JSON,
    Number,
    Array,
    Object,
    isFinite,
    setTimeout: (run: () => void, delay: number) => {
      const id = ++timerId;
      timers.set(id, { at: elapsed + delay, run });
      return id;
    },
    clearTimeout: (id: number) => timers.delete(id),
  });
  await flush();
  return {
    blocks,
    requests,
    async advance(milliseconds: number) {
      const end = elapsed + milliseconds;
      let steps = 0;
      while (timers.size > 0) {
        const next = [...timers.entries()]
          .filter(([, t]) => t.at <= end)
          .sort((a, b) => a[1].at - b[1].at)[0];
        if (!next) break;
        assert.ok(++steps < 1000, "renderer must not spin in a timer loop");
        const [id, timer] = next;
        elapsed = Math.max(elapsed, timer.at);
        timers.delete(id);
        timer.run();
        await flush();
      }
      elapsed = end;
      await flush();
    },
    jumpWithoutTimers(milliseconds: number) {
      elapsed += milliseconds;
    },
    visibility(hidden: boolean) {
      document.hidden = hidden;
      for (const callback of listeners.get("visibilitychange") ?? [])
        callback({});
    },
    page(name: string) {
      for (const callback of listeners.get(name) ?? [])
        callback({ persisted: true });
    },
  };
}

describe("storefront entitlement lifecycle", () => {
  it("hides an already-open page within one minute despite the real server cache", async () => {
    clearPartnerSubscriptionCacheForTests();
    let providerCalls = 0;
    const rendered = await storefront(async (elapsed) =>
      reply(
        await getStorefrontEntitlement(
          CONFIG,
          "gid://shopify/Shop/42",
          async () => {
            providerCalls++;
            return new Response(
              JSON.stringify({
                data: {
                  activeSubscription:
                    elapsed === 0 ? { billingPeriod: "EVERY_30_DAYS" } : null,
                },
              }),
            );
          },
          () => elapsed,
        ),
      ),
    );
    assert.equal(rendered.blocks[0].hidden, false);
    await rendered.advance(59_999);
    assert.equal(rendered.blocks[0].hidden, false);
    assert.equal(
      providerCalls,
      1,
      "refreshes must reuse only the remaining server lifetime",
    );
    await rendered.advance(1);
    assert.equal(
      rendered.blocks[0].hidden,
      true,
      "cancellation must hide without navigation",
    );
    await rendered.advance(1_000);
    assert.equal(rendered.blocks[0].hidden, true);
    assert.equal(providerCalls, 2);
    clearPartnerSubscriptionCacheForTests();
  });

  it("keeps a subscribed page working across server cache renewals", async () => {
    clearPartnerSubscriptionCacheForTests();
    let providerCalls = 0;
    const rendered = await storefront(async (elapsed) =>
      reply(
        await getStorefrontEntitlement(
          CONFIG,
          "gid://shopify/Shop/42",
          async () => {
            providerCalls++;
            return new Response(
              JSON.stringify({
                data: {
                  activeSubscription: { billingPeriod: "EVERY_30_DAYS" },
                },
              }),
            );
          },
          () => elapsed,
        ),
      ),
    );
    await rendered.advance(125_000);
    assert.equal(rendered.blocks[0].hidden, false);
    assert.equal(providerCalls, 3);
    clearPartnerSubscriptionCacheForTests();
  });

  it("hides on provider failure and recovers on a later successful check", async () => {
    const rendered = await storefront(async (elapsed) => {
      if (elapsed >= 15_000 && elapsed < 30_000) throw new Error("offline");
      return reply({ active: true, validForMs: 60_000 });
    });
    await rendered.advance(15_000);
    assert.equal(rendered.blocks[0].hidden, true);
    await rendered.advance(15_000);
    assert.equal(rendered.blocks[0].hidden, false);
  });

  it("times out a hanging refresh and ignores its late success", async () => {
    let resolveLate: ((response: Reply) => void) | undefined;
    const rendered = await storefront(async (elapsed) =>
      elapsed === 0
        ? reply({ active: true, validForMs: 60_000 })
        : new Promise<Reply>((resolve) => {
            resolveLate = resolve;
          }),
    );
    await rendered.advance(20_000);
    assert.equal(rendered.blocks[0].hidden, true);
    assert.equal(rendered.requests[1].signal.aborted, true);
    resolveLate!(reply({ active: true, validForMs: 60_000 }));
    await flush();
    assert.equal(rendered.blocks[0].hidden, true);
  });

  it("expires the grant independently of the next refresh", async () => {
    const rendered = await storefront(async () =>
      reply({ active: true, validForMs: 600 }),
    );
    assert.equal(rendered.blocks[0].hidden, false);
    await rendered.advance(600);
    assert.equal(rendered.blocks[0].hidden, true);
    assert.equal(rendered.requests.length, 1);
  });

  it("counts network delay against the remaining server lifetime", async () => {
    let resolve: ((response: Reply) => void) | undefined;
    const rendered = await storefront(
      () =>
        new Promise<Reply>((done) => {
          resolve = done;
        }),
    );
    await rendered.advance(1_100);
    resolve!(reply({ active: true, validForMs: 1_000 }));
    await flush();
    assert.equal(rendered.blocks[0].hidden, true);
  });

  it("rejects old-protocol and malformed positive grants", async () => {
    for (const body of [
      { active: true },
      { active: true, validForMs: "60000" },
      { active: true, validForMs: 60_001 },
      { active: true, validForMs: 0 },
      { active: "true", validForMs: 60_000 },
    ]) {
      const rendered = await storefront(async () => reply(body));
      assert.equal(rendered.blocks[0].hidden, true);
    }
  });

  it("shares one uncached request between multiple blocks on each refresh", async () => {
    const rendered = await storefront(undefined, 3);
    assert.equal(rendered.requests.length, 1);
    assert.equal(rendered.requests[0].cache, "no-store");
    assert.ok(rendered.blocks.every((block) => !block.hidden));
    await rendered.advance(15_000);
    assert.equal(rendered.requests.length, 2);
  });

  it("hides on suspension and revalidates before showing a restored page", async () => {
    const rendered = await storefront(async (elapsed) =>
      reply({
        active: elapsed === 0,
        validForMs: elapsed === 0 ? 60_000 : 0,
      }),
    );
    assert.equal(rendered.blocks[0].hidden, false);
    rendered.visibility(true);
    assert.equal(rendered.blocks[0].hidden, true);
    rendered.jumpWithoutTimers(70_000);
    rendered.visibility(false);
    assert.equal(rendered.blocks[0].hidden, true);
    await flush();
    assert.equal(rendered.blocks[0].hidden, true);
    assert.equal(rendered.requests.length, 2);
  });

  it("ignores an in-flight response from before a back-forward-cache restore", async () => {
    let resolveOld: ((response: Reply) => void) | undefined;
    let calls = 0;
    const rendered = await storefront(async () =>
      ++calls === 1
        ? new Promise<Reply>((resolve) => {
            resolveOld = resolve;
          })
        : reply({ active: false, validForMs: 0 }),
    );
    rendered.page("pagehide");
    assert.equal(rendered.requests[0].signal.aborted, true);
    rendered.jumpWithoutTimers(70_000);
    rendered.page("pageshow");
    await flush();
    resolveOld!(reply({ active: true, validForMs: 60_000 }));
    await flush();
    assert.equal(rendered.blocks[0].hidden, true);
    assert.equal(rendered.requests.length, 2);
  });
});
