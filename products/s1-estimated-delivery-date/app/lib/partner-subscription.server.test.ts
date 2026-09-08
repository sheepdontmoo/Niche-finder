import { describe, it } from "node:test";
import assert from "node:assert/strict";
import {
  clearPartnerSubscriptionCacheForTests,
  getAuthenticatedShopId,
  hasCachedActivePartnerSubscription,
  hasActivePartnerSubscription,
  hasActivePartnerSubscriptionAfterPlanSelection,
  PARTNER_API_VERSION,
  POST_APPROVAL_RECHECK_COOLDOWN_MS,
  POST_APPROVAL_RECHECK_MS,
  readPartnerBillingConfig,
  rememberPartnerSubscription,
  type PartnerBillingConfig,
} from "./partner-subscription.server.ts";

const CONFIG: PartnerBillingConfig = {
  organizationId: "12345",
  accessToken: "test-token-not-a-real-secret",
  appId: "gid://shopify/App/67890",
  appHandle: "verified-app-handle",
};

function jsonResponse(value: unknown, status = 200) {
  return new Response(JSON.stringify(value), {
    status,
    headers: { "Content-Type": "application/json" },
  });
}

describe("Shopify Partner API subscription check", () => {
  it("requires every provider-confirmed configuration value", () => {
    assert.throws(() => readPartnerBillingConfig({}), /SHOPIFY_PARTNER_ORG_ID/);
    assert.throws(
      () =>
        readPartnerBillingConfig({
          SHOPIFY_PARTNER_ORG_ID: "12345",
          SHOPIFY_APP_GID: "not-a-gid",
          SHOPIFY_PARTNER_API_ACCESS_TOKEN: "token",
          SHOPIFY_APP_HANDLE: "handle",
        }),
      /SHOPIFY_APP_GID is invalid/,
    );
    assert.throws(
      () =>
        readPartnerBillingConfig({
          SHOPIFY_PARTNER_ORG_ID: "12345",
          SHOPIFY_APP_GID: "gid://shopify/App/not-numeric",
          SHOPIFY_PARTNER_API_ACCESS_TOKEN: "token",
          SHOPIFY_APP_HANDLE: "handle",
        }),
      /SHOPIFY_APP_GID is invalid/,
    );
  });

  it("reads the shop GID from the authenticated Admin API", async () => {
    const graphql = async () =>
      ({
        json: async () => ({ data: { shop: { id: "gid://shopify/Shop/42" } } }),
      }) as Response;
    assert.equal(
      await getAuthenticatedShopId(
        graphql as Parameters<typeof getAuthenticatedShopId>[0],
      ),
      "gid://shopify/Shop/42",
    );
  });

  it("fails closed when the Admin API does not return a shop GID", async () => {
    const graphql = async () =>
      ({ json: async () => ({ errors: [{ message: "failed" }] }) }) as Response;
    await assert.rejects(
      () =>
        getAuthenticatedShopId(
          graphql as Parameters<typeof getAuthenticatedShopId>[0],
        ),
      /authenticated shop ID/,
    );
  });

  it("returns true only when Partner API reports an active subscription", async () => {
    let requestUrl = "";
    let requestInit: RequestInit | undefined;
    const fetcher: typeof fetch = async (input, init) => {
      requestUrl = String(input);
      requestInit = init;
      return jsonResponse({
        data: {
          activeSubscription: {
            billingPeriod: "EVERY_30_DAYS",
            trialEndsAt: "2026-09-06T00:00:00Z",
          },
        },
      });
    };

    assert.equal(
      await hasActivePartnerSubscription(
        CONFIG,
        "gid://shopify/Shop/42",
        fetcher,
      ),
      true,
    );
    assert.equal(
      requestUrl,
      `https://partners.shopify.com/12345/api/${PARTNER_API_VERSION}/graphql.json`,
    );
    assert.equal(
      (requestInit?.headers as Record<string, string>)[
        "X-Shopify-Access-Token"
      ],
      CONFIG.accessToken,
    );
    const sent = JSON.parse(String(requestInit?.body));
    assert.deepEqual(sent.variables, {
      appId: CONFIG.appId,
      shopId: "gid://shopify/Shop/42",
    });
    assert.ok(requestInit?.signal instanceof AbortSignal);
    assert.equal(requestInit.signal.aborted, false);
  });

  it("returns false when no active managed-pricing contract exists", async () => {
    const fetcher: typeof fetch = async () =>
      jsonResponse({ data: { activeSubscription: null } });
    assert.equal(
      await hasActivePartnerSubscription(
        CONFIG,
        "gid://shopify/Shop/42",
        fetcher,
      ),
      false,
    );
  });

  it("rechecks once after plan selection when the first contract read is null", async () => {
    clearPartnerSubscriptionCacheForTests();
    let calls = 0;
    const waits: number[] = [];
    const fetcher: typeof fetch = async () => {
      calls++;
      return jsonResponse({
        data: {
          activeSubscription:
            calls === 1 ? null : { billingPeriod: "EVERY_30_DAYS" },
        },
      });
    };

    assert.equal(
      await hasActivePartnerSubscriptionAfterPlanSelection(
        CONFIG,
        "gid://shopify/Shop/42",
        fetcher,
        async (milliseconds) => {
          waits.push(milliseconds);
        },
        1_000,
      ),
      true,
    );
    assert.equal(calls, 2);
    assert.deepEqual(waits, [POST_APPROVAL_RECHECK_MS]);
    clearPartnerSubscriptionCacheForTests();
  });

  it("stays fail closed when both post-plan contract reads are null", async () => {
    clearPartnerSubscriptionCacheForTests();
    let calls = 0;
    const fetcher: typeof fetch = async () => {
      calls++;
      return jsonResponse({ data: { activeSubscription: null } });
    };

    assert.equal(
      await hasActivePartnerSubscriptionAfterPlanSelection(
        CONFIG,
        "gid://shopify/Shop/42",
        fetcher,
        async () => {},
        1_000,
      ),
      false,
    );
    assert.equal(calls, 2);
    clearPartnerSubscriptionCacheForTests();
  });

  it("does not retry after plan selection when the first read is active", async () => {
    clearPartnerSubscriptionCacheForTests();
    let calls = 0;
    const fetcher: typeof fetch = async () => {
      calls++;
      return jsonResponse({
        data: {
          activeSubscription: { billingPeriod: "EVERY_30_DAYS" },
        },
      });
    };

    assert.equal(
      await hasActivePartnerSubscriptionAfterPlanSelection(
        CONFIG,
        "gid://shopify/Shop/42",
        fetcher,
        async () => {
          throw new Error("active contract must not wait");
        },
        1_000,
      ),
      true,
    );
    assert.equal(calls, 1);
    clearPartnerSubscriptionCacheForTests();
  });

  it("rate-limits repeated post-plan rereads for the same shop", async () => {
    clearPartnerSubscriptionCacheForTests();
    let calls = 0;
    let waits = 0;
    const fetcher: typeof fetch = async () => {
      calls++;
      return jsonResponse({ data: { activeSubscription: null } });
    };
    const sleeper = async () => {
      waits++;
    };

    assert.equal(
      await hasActivePartnerSubscriptionAfterPlanSelection(
        CONFIG,
        "gid://shopify/Shop/42",
        fetcher,
        sleeper,
        1_000,
      ),
      false,
    );
    assert.equal(
      await hasActivePartnerSubscriptionAfterPlanSelection(
        CONFIG,
        "gid://shopify/Shop/42",
        fetcher,
        sleeper,
        1_000 + POST_APPROVAL_RECHECK_COOLDOWN_MS - 1,
      ),
      false,
    );
    assert.equal(calls, 3);
    assert.equal(waits, 1);
    clearPartnerSubscriptionCacheForTests();
  });

  it("fails closed on Partner API transport, GraphQL, or schema errors", async () => {
    await assert.rejects(
      () =>
        hasActivePartnerSubscription(
          CONFIG,
          "gid://shopify/Shop/42",
          async () => jsonResponse({}, 401),
        ),
      /subscription check failed \(401\)/,
    );
    await assert.rejects(
      () =>
        hasActivePartnerSubscription(
          CONFIG,
          "gid://shopify/Shop/42",
          async () => jsonResponse({ errors: [{ message: "failure" }] }),
        ),
      /subscription check failed/,
    );
    await assert.rejects(
      () =>
        hasActivePartnerSubscription(
          CONFIG,
          "gid://shopify/Shop/42",
          async () => jsonResponse({ data: {} }),
        ),
      /subscription response is invalid/,
    );
    await assert.rejects(
      () =>
        hasActivePartnerSubscription(
          CONFIG,
          "gid://shopify/Shop/42",
          async () => jsonResponse({ data: { activeSubscription: {} } }),
        ),
      /subscription response is invalid/,
    );
  });

  it("deduplicates storefront checks and expires them after one minute", async () => {
    clearPartnerSubscriptionCacheForTests();
    let calls = 0;
    const fetcher: typeof fetch = async () => {
      calls++;
      return jsonResponse({
        data: {
          activeSubscription: {
            billingPeriod: "EVERY_30_DAYS",
            trialEndsAt: null,
          },
        },
      });
    };

    const first = hasCachedActivePartnerSubscription(
      CONFIG,
      "gid://shopify/Shop/42",
      fetcher,
      1_000,
    );
    const concurrent = hasCachedActivePartnerSubscription(
      CONFIG,
      "gid://shopify/Shop/42",
      fetcher,
      1_000,
    );
    assert.equal(await first, true);
    assert.equal(await concurrent, true);
    assert.equal(calls, 1);

    assert.equal(
      await hasCachedActivePartnerSubscription(
        CONFIG,
        "gid://shopify/Shop/42",
        fetcher,
        60_999,
      ),
      true,
    );
    assert.equal(calls, 1);

    assert.equal(
      await hasCachedActivePartnerSubscription(
        CONFIG,
        "gid://shopify/Shop/42",
        fetcher,
        61_000,
      ),
      true,
    );
    assert.equal(calls, 2);
    clearPartnerSubscriptionCacheForTests();
  });

  it("lets an authenticated admin check replace a stale storefront result", async () => {
    clearPartnerSubscriptionCacheForTests();
    rememberPartnerSubscription(
      CONFIG,
      "gid://shopify/Shop/42",
      false,
      1_000,
    );
    rememberPartnerSubscription(
      CONFIG,
      "gid://shopify/Shop/42",
      true,
      2_000,
    );

    assert.equal(
      await hasCachedActivePartnerSubscription(
        CONFIG,
        "gid://shopify/Shop/42",
        async () => {
          throw new Error("cache should avoid a provider call");
        },
        2_001,
      ),
      true,
    );
    clearPartnerSubscriptionCacheForTests();
  });

  it("does not let an older inactive storefront read replace newer active admin state", async () => {
    clearPartnerSubscriptionCacheForTests();
    let resolveResponse: ((response: Response) => void) | undefined;
    const deferred = new Promise<Response>((resolve) => {
      resolveResponse = resolve;
    });
    const fetcher: typeof fetch = async () => deferred;

    const pending = hasCachedActivePartnerSubscription(
      CONFIG,
      "gid://shopify/Shop/42",
      fetcher,
      1_000,
      () => 2_001,
    );
    rememberPartnerSubscription(CONFIG, "gid://shopify/Shop/42", true, 2_000);
    resolveResponse?.(jsonResponse({ data: { activeSubscription: null } }));

    assert.equal(await pending, true);
    assert.equal(
      await hasCachedActivePartnerSubscription(
        CONFIG,
        "gid://shopify/Shop/42",
        async () => {
          throw new Error("newer admin state must remain cached");
        },
        2_001,
      ),
      true,
    );
    clearPartnerSubscriptionCacheForTests();
  });

  it("does not let an older active storefront read replace newer inactive admin state", async () => {
    clearPartnerSubscriptionCacheForTests();
    let resolveResponse: ((response: Response) => void) | undefined;
    const deferred = new Promise<Response>((resolve) => {
      resolveResponse = resolve;
    });
    const fetcher: typeof fetch = async () => deferred;

    const pending = hasCachedActivePartnerSubscription(
      CONFIG,
      "gid://shopify/Shop/42",
      fetcher,
      1_000,
      () => 2_001,
    );
    rememberPartnerSubscription(CONFIG, "gid://shopify/Shop/42", false, 2_000);
    resolveResponse?.(
      jsonResponse({
        data: {
          activeSubscription: { billingPeriod: "EVERY_30_DAYS" },
        },
      }),
    );

    assert.equal(await pending, false);
    assert.equal(
      await hasCachedActivePartnerSubscription(
        CONFIG,
        "gid://shopify/Shop/42",
        async () => {
          throw new Error("newer inactive admin state must remain cached");
        },
        2_001,
      ),
      false,
    );
    clearPartnerSubscriptionCacheForTests();
  });

  it("fails closed when newer admin state expires before an older request finishes", async () => {
    clearPartnerSubscriptionCacheForTests();
    let resolveResponse: ((response: Response) => void) | undefined;
    const deferred = new Promise<Response>((resolve) => {
      resolveResponse = resolve;
    });
    const fetcher: typeof fetch = async () => deferred;

    const pending = hasCachedActivePartnerSubscription(
      CONFIG,
      "gid://shopify/Shop/42",
      fetcher,
      1_000,
      () => 62_000,
    );
    rememberPartnerSubscription(CONFIG, "gid://shopify/Shop/42", true, 2_000);
    resolveResponse?.(
      jsonResponse({
        data: {
          activeSubscription: { billingPeriod: "EVERY_30_DAYS" },
        },
      }),
    );

    assert.equal(await pending, false);
    let freshCalls = 0;
    assert.equal(
      await hasCachedActivePartnerSubscription(
        CONFIG,
        "gid://shopify/Shop/42",
        async () => {
          freshCalls++;
          return jsonResponse({ data: { activeSubscription: null } });
        },
        62_000,
      ),
      false,
    );
    assert.equal(freshCalls, 1);
    clearPartnerSubscriptionCacheForTests();
  });
});
