import { describe, it } from "node:test";
import assert from "node:assert/strict";
import { deleteShopData } from "./shop-data.server.ts";

describe("shop data deletion", () => {
  it("deletes by authenticated shop even when no webhook session exists", async () => {
    const calls: unknown[] = [];
    const store = {
      session: {
        deleteMany: async (args: unknown) => {
          calls.push(args);
          return { count: 0 };
        },
      },
    };

    await deleteShopData(store, "example-store.myshopify.com");
    assert.deepEqual(calls, [
      { where: { shop: "example-store.myshopify.com" } },
    ]);
  });
});
