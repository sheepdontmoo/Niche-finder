import type { LoaderFunctionArgs } from "react-router";
import { authenticate } from "../shopify.server";
import {
  getAuthenticatedShopId,
  getStorefrontEntitlement,
  readPartnerBillingConfig,
} from "../lib/partner-subscription.server";

const RESPONSE_HEADERS = {
  "Cache-Control": "no-store",
  "Content-Type": "application/json; charset=utf-8",
};

function entitlementResponse(
  active: boolean,
  status = 200,
  validForMs = 0,
): Response {
  return new Response(JSON.stringify({ active, validForMs }), {
    status,
    headers: RESPONSE_HEADERS,
  });
}

/**
 * HMAC-authenticated Shopify app-proxy endpoint used by the theme extension.
 * It fails closed if the app is not installed, the offline session is missing,
 * provider configuration is invalid, or Shopify cannot confirm a subscription.
 */
export const loader = async ({ request }: LoaderFunctionArgs) => {
  try {
    const { admin, session } = await authenticate.public.appProxy(request);
    if (!admin || !session) return entitlementResponse(false, 403);

    const billingConfig = readPartnerBillingConfig(process.env);
    const shopId = await getAuthenticatedShopId(admin.graphql);
    const entitlement = await getStorefrontEntitlement(billingConfig, shopId);
    return entitlementResponse(entitlement.active, 200, entitlement.validForMs);
  } catch {
    // Keep provider details and credentials out of the public response. The
    // storefront stays hidden until an authoritative check succeeds.
    console.error("Storefront subscription verification failed");
    return entitlementResponse(false, 503);
  }
};
