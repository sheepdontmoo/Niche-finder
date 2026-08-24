export const SITE_URL = "https://edd-supadesign.fly.dev";
export const SHOPIFY_LISTING_URL =
  "https://apps.shopify.com/estimated-delivery-date-6";
export const SHOPIFY_METAFIELD_LIFECYCLE_URL =
  "https://shopify.dev/docs/apps/build/metafields/definitions";
export const LAST_VERIFIED = "2026-08-24";

export const supportFaqs = [
  {
    question: "How does SupaDatewise calculate an estimated delivery date?",
    answer:
      "SupaDatewise combines the delivery rules you choose: processing time, a daily order cutoff, a minimum and maximum shipping window, and working days. An order placed after the cutoff rolls into the next processing window. The storefront block then formats that result as a clear delivery estimate on product and cart pages. Merchants remain responsible for configuring rules that match their fulfilment operation.",
  },
  {
    question: "Can I add the delivery estimate without editing theme code?",
    answer:
      "Yes. SupaDatewise provides a theme app extension block for compatible Online Store 2.0 themes. After installing the app, add the block in Shopify's theme editor and place it where it helps shoppers make a decision, such as near the Add to cart button. The message wording, date format, locale, working days, and delivery rules are configured in the app rather than by editing theme files.",
  },
  {
    question: "What does SupaDatewise cost?",
    answer:
      "The current Shopify App Store listing shows one Standard plan at $6.99 per month with a seven-day free trial and automatic delivery-date estimates. Shopify handles installation and billing through the App Store. Pricing, trial availability, and plan terms can change, so the Shopify listing is the source of truth to check before installing. SupaDatewise does not claim that it guarantees carrier delivery times.",
  },
] as const;

export const privacyFaqs = [
  {
    question: "Does SupaDatewise collect customer personal data?",
    answer:
      "No customer names, email addresses, shipping addresses, or order contents are collected, stored, or processed by SupaDatewise for its delivery estimate. The estimate displayed to a shopper is calculated in that shopper's browser from merchant-configured delivery rules. The app does store merchant delivery settings and a standard Shopify app session so the merchant can configure and operate the app while it remains installed.",
  },
  {
    question: "What happens to SupaDatewise data when a merchant uninstalls?",
    answer:
      "On uninstall, SupaDatewise deletes its stored Shopify app session. Delivery settings are app-owned Shopify metafields, so Shopify—not SupaDatewise—controls their post-uninstall lifecycle. Shopify deletes the app-owned definitions but may temporarily retain metafields and their values without definitions; a quick reinstall can reassociate retained values. Because SupaDatewise does not hold customer personal data, customer data-request and customer-redaction requests have no customer records to return or erase. Shopify's metafield documentation is the source for the platform lifecycle.",
  },
] as const;

export function faqSchema(faqs: readonly { question: string; answer: string }[]) {
  return {
    "@context": "https://schema.org",
    "@type": "FAQPage",
    mainEntity: faqs.map((faq) => ({
      "@type": "Question",
      name: faq.question,
      acceptedAnswer: { "@type": "Answer", text: faq.answer },
    })),
  };
}

export function siteJson(value: unknown) {
  return JSON.stringify(value).replace(/</g, "\\u003c");
}
