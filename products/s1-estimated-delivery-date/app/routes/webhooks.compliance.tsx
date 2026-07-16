import type { ActionFunctionArgs } from "react-router";
import { authenticate } from "../shopify.server";

/**
 * Mandatory GDPR/compliance webhooks for public apps:
 *   customers/data_request, customers/redact, shop/redact
 *
 * This app stores no personal customer data — only shop-level delivery
 * settings (a metafield) and the Shopify session. There is nothing to return
 * for a data request and nothing customer-specific to erase, so we
 * acknowledge with 200. `authenticate.webhook` verifies the HMAC; an invalid
 * signature throws a 401 before we reach this code.
 */
export const action = async ({ request }: ActionFunctionArgs) => {
  const { shop, topic } = await authenticate.webhook(request);
  console.log(`Received compliance webhook ${topic} for ${shop}`);
  return new Response();
};
