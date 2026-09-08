type ShopifyRedirect = (
  url: string,
  options: { target: "_top" },
) => Response;

export function requireShopifyAppHandle(value: string | undefined): string {
  const handle = value?.trim();
  if (!handle || !/^[a-z0-9][a-z0-9-]*[a-z0-9]$|^[a-z0-9]$/.test(handle)) {
    throw new Error("SHOPIFY_APP_HANDLE is missing or invalid");
  }
  return handle;
}

export function buildPricingPlansUrl(shop: string, appHandle: string): string {
  const normalizedShop = shop.trim().toLowerCase();
  const suffix = ".myshopify.com";
  if (!normalizedShop.endsWith(suffix)) {
    throw new Error("Authenticated Shopify shop domain is invalid");
  }

  const storeHandle = normalizedShop.slice(0, -suffix.length);
  if (!/^[a-z0-9][a-z0-9-]*[a-z0-9]$|^[a-z0-9]$/.test(storeHandle)) {
    throw new Error("Authenticated Shopify store handle is invalid");
  }

  return `https://admin.shopify.com/store/${storeHandle}/charges/${requireShopifyAppHandle(appHandle)}/pricing_plans`;
}

/**
 * Shopify App Pricing does not automatically protect app routes. Check the
 * provider-backed subscription state at the app root and send merchants with
 * no active payment to Shopify's hosted plan selector.
 */
export async function requireActiveAppPayment(
  hasActivePayment: boolean,
  redirect: ShopifyRedirect,
  shop: string,
  appHandle: string,
): Promise<Response | null> {
  if (hasActivePayment) return null;
  return redirect(buildPricingPlansUrl(shop, appHandle), { target: "_top" });
}
