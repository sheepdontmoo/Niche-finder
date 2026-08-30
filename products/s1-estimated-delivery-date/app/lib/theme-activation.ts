export const DELIVERY_DATE_BLOCK_HANDLE = "delivery_date";

/**
 * Build Shopify's official theme-editor deep link for the product template.
 * The authenticated session supplies `shop`; the client ID is public app
 * configuration, not a secret.
 */
export function buildThemeActivationUrl(
  shop: string,
  apiKey: string | undefined,
): string | null {
  if (!apiKey?.trim()) return null;

  const url = new URL(`https://${shop}/admin/themes/current/editor`);
  url.searchParams.set("template", "product");
  url.searchParams.set(
    "addAppBlockId",
    `${apiKey.trim()}/${DELIVERY_DATE_BLOCK_HANDLE}`,
  );
  url.searchParams.set("target", "mainSection");
  return url.toString();
}
