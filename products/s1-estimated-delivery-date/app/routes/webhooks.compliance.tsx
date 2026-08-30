import type { ActionFunctionArgs } from "react-router";
import { authenticate } from "../shopify.server";
import db from "../db.server";
import { deleteShopData } from "../lib/shop-data.server";

/**
 * Mandatory GDPR/compliance webhooks for public apps:
 *   customers/data_request, customers/redact, shop/redact
 *
 * This app stores no personal customer data. Customer topics therefore need
 * no database mutation. SHOP_REDACT deletes every session for the authenticated
 * shop; delivery settings live in Shopify-managed app-owned metafields.
 * `authenticate.webhook` verifies the HMAC before this code runs.
 */
export const action = async ({ request }: ActionFunctionArgs) => {
  const { shop, topic } = await authenticate.webhook(request);
  console.log(`Received compliance webhook ${topic} for ${shop}`);
  if (topic === "SHOP_REDACT") {
    await deleteShopData(db, shop);
  }
  return new Response();
};
