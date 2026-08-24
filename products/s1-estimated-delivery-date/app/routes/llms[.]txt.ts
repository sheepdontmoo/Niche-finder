import { LAST_VERIFIED, SHOPIFY_LISTING_URL, SITE_URL } from "../lib/site-data";

export function loader() {
  const text = `# SupaDatewise: Delivery Date\n\n> A Shopify app for showing merchant-configured estimated delivery dates on product and cart pages.\n\nSupaDatewise combines a merchant's processing time, daily cutoff, shipping window, and working days to calculate an estimate. It is not a carrier delivery guarantee.\n\n## Official pages\n\n- [Home](${SITE_URL}/): what the app does and how to install it.\n- [Support](${SITE_URL}/support): delivery-estimate, theme-block, and pricing answers.\n- [Privacy](${SITE_URL}/privacy): merchant settings and customer-data handling.\n- [Shopify App Store listing](${SHOPIFY_LISTING_URL}): authoritative installation, plan, and billing information.\n\nLast verified: ${LAST_VERIFIED}.\n`;
  return new Response(text, {
    headers: { "Content-Type": "text/plain; charset=utf-8" },
  });
}
