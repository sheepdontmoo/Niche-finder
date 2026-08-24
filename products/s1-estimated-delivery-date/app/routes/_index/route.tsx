import type { LoaderFunctionArgs } from "react-router";
import { redirect, Form, useLoaderData } from "react-router";

import { login } from "../../shopify.server";
import {
  LAST_VERIFIED,
  SHOPIFY_LISTING_URL,
  SITE_URL,
  siteJson,
} from "../../lib/site-data";

import styles from "./styles.module.css";

export const loader = async ({ request }: LoaderFunctionArgs) => {
  const url = new URL(request.url);

  if (url.searchParams.get("shop")) {
    throw redirect(`/app?${url.searchParams.toString()}`);
  }

  return { showForm: Boolean(login) };
};

export const meta = () => [
  { title: "SupaDatewise | Estimated delivery dates for Shopify" },
  {
    name: "description",
    content:
      "Show a merchant-configured estimated delivery date on Shopify product and cart pages with a no-code theme block.",
  },
  { tagName: "link", rel: "canonical", href: SITE_URL },
];

export default function App() {
  const { showForm } = useLoaderData<typeof loader>();

  return (
    <div className={styles.index}>
      <div className={styles.content}>
        <p className={styles.eyebrow}>SupaDatewise: Delivery Date</p>
        <h1 className={styles.heading}>Estimated delivery dates for Shopify stores</h1>
        <p className={styles.text}>
          SupaDatewise helps Shopify merchants show a clear estimated delivery
          date on product and cart pages. It uses the fulfilment rules the
          merchant sets—processing time, a daily cutoff, a shipping window, and
          working days—to calculate a shopper-facing estimate. Add the no-code
          block in an Online Store 2.0 theme. It communicates an estimate, not
          a carrier delivery guarantee.
        </p>
        <p className={styles.meta}>
          Source: {" "}
          <a href={SHOPIFY_LISTING_URL}>SupaDatewise on the Shopify App Store</a>.
          Last verified: {LAST_VERIFIED}.
        </p>
        <p>
          <a className={styles.button} href={SHOPIFY_LISTING_URL}>
            Install from the Shopify App Store
          </a>
        </p>
        {showForm && (
          <Form className={styles.form} method="post" action="/auth/login">
            <label className={styles.label}>
              <span>Shop domain</span>
              <input className={styles.input} type="text" name="shop" />
              <span>e.g: my-shop-domain.myshopify.com</span>
            </label>
            <button className={styles.button} type="submit">
              Log in
            </button>
          </Form>
        )}
        <ul className={styles.list}>
          <li>
            <strong>Automatic estimates</strong>. A get-it-by-date message
            calculated from your processing time, cutoff and shipping window.
          </li>
          <li>
            <strong>No theme code</strong>. Add a drag-and-drop block to your
            product pages in the theme editor.
          </li>
          <li>
            <strong>Fully customizable</strong>. Control the wording, date
            format, working days and locale.
          </li>
        </ul>
        <p className={styles.footerLinks}>
          <a href="/support">Support</a> · <a href="/privacy">Privacy</a>
        </p>
      </div>
      <script
        type="application/ld+json"
        dangerouslySetInnerHTML={{
          __html: siteJson({
            "@context": "https://schema.org",
            "@type": "SoftwareApplication",
            name: "SupaDatewise: Delivery Date",
            applicationCategory: "BusinessApplication",
            operatingSystem: "Shopify",
            description:
              "A Shopify app that shows merchant-configured estimated delivery dates on product and cart pages.",
            url: SITE_URL,
            installUrl: SHOPIFY_LISTING_URL,
            sameAs: SHOPIFY_LISTING_URL,
            dateModified: LAST_VERIFIED,
            offers: {
              "@type": "Offer",
              name: "Standard",
              price: "6.99",
              priceCurrency: "USD",
              billingDuration: "P1M",
              description:
                "Current Shopify App Store plan: $6.99 per month with a seven-day free trial.",
              url: SHOPIFY_LISTING_URL,
            },
          }),
        }}
      />
    </div>
  );
}
