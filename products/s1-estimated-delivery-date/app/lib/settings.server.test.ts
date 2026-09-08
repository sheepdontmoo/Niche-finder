import { describe, it } from "node:test";
import assert from "node:assert/strict";
import { readSettings, writeSettings } from "./settings.server.ts";

type GraphqlOptions = { variables?: Record<string, unknown> };
type GraphqlCall = { query: string; options?: GraphqlOptions };

function graphqlSequence(payloads: unknown[]) {
  const calls: GraphqlCall[] = [];
  let index = 0;
  const graphql = async (query: string, options?: GraphqlOptions) => {
    calls.push({ query, options });
    const payload = payloads[index++];
    return { json: async () => payload } as Response;
  };
  return {
    calls,
    graphql: graphql as Parameters<typeof readSettings>[0],
  };
}

describe("delivery settings persistence", () => {
  it("overrides legacy or stale settings with Shopify's authoritative timezone", async () => {
    const legacy = JSON.stringify({
      processingDays: 2,
      cutoffHour: 15,
      timeZone: "America/New_York",
    });
    const { graphql } = graphqlSequence([
      {
        data: {
          shop: {
            ianaTimezone: "Europe/Dublin",
            metafield: { value: legacy },
          },
        },
      },
    ]);

    const settings = await readSettings(graphql);
    assert.equal(settings.processingDays, 2);
    assert.equal(settings.cutoffHour, 15);
    assert.equal(settings.timeZone, "Europe/Dublin");
  });

  it("fails closed when Shopify omits the shop timezone", async () => {
    const { graphql } = graphqlSequence([
      { data: { shop: { metafield: null } } },
    ]);

    await assert.rejects(
      () => readSettings(graphql),
      /Shopify did not return the shop IANA timezone/,
    );
  });

  it("fails closed when Shopify returns GraphQL errors", async () => {
    const { graphql } = graphqlSequence([
      { errors: [{ message: "provider failure" }] },
    ]);

    await assert.rejects(
      () => readSettings(graphql),
      /Shopify shop settings query failed/,
    );
  });

  it("writes normalized settings to the app-owned shop metafield", async () => {
    const { calls, graphql } = graphqlSequence([
      {
        data: {
          shop: {
            id: "gid://shopify/Shop/123",
            ianaTimezone: "America/New_York",
          },
        },
      },
      { data: { metafieldsSet: { metafields: [{ id: "gid://shopify/Metafield/1" }], userErrors: [] } } },
    ]);

    const saved = await writeSettings(graphql, {
      processingDays: 3,
      cutoffHour: 16,
    });
    assert.equal(saved.timeZone, "America/New_York");

    const variables = calls[1]?.options?.variables;
    const metafields = variables?.metafields as Array<Record<string, unknown>>;
    assert.equal(metafields[0]?.ownerId, "gid://shopify/Shop/123");
    assert.equal(metafields[0]?.namespace, "$app");
    assert.equal(metafields[0]?.key, "settings");
    assert.equal(
      (JSON.parse(String(metafields[0]?.value)) as { timeZone: string }).timeZone,
      "America/New_York",
    );
  });

  it("does not report a save when Shopify omits mutation confirmation", async () => {
    const { graphql } = graphqlSequence([
      {
        data: {
          shop: {
            id: "gid://shopify/Shop/123",
            ianaTimezone: "Europe/Dublin",
          },
        },
      },
      { data: { metafieldsSet: { metafields: [], userErrors: [] } } },
    ]);

    await assert.rejects(
      () => writeSettings(graphql, { processingDays: 3 }),
      /did not confirm the saved delivery settings/,
    );
  });

  it("fails closed on top-level mutation errors", async () => {
    const { graphql } = graphqlSequence([
      {
        data: {
          shop: {
            id: "gid://shopify/Shop/123",
            ianaTimezone: "Europe/Dublin",
          },
        },
      },
      { errors: [{ message: "provider failure" }] },
    ]);

    await assert.rejects(
      () => writeSettings(graphql, { processingDays: 3 }),
      /Shopify settings mutation failed/,
    );
  });
});
