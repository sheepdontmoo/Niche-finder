import {
  LAST_VERIFIED,
  SHOPIFY_LISTING_URL,
  SITE_URL,
  faqSchema,
  siteJson,
  supportFaqs,
} from "../../lib/site-data";
import styles from "../_index/styles.module.css";

export const meta = () => [
  { title: "SupaDatewise support | Estimated delivery dates for Shopify" },
  {
    name: "description",
    content:
      "Answers about SupaDatewise delivery-date estimates, Shopify theme blocks, installation, and current App Store pricing.",
  },
  { tagName: "link", rel: "canonical", href: `${SITE_URL}/support` },
];

export default function Support() {
  const [featuredFaq, ...additionalFaqs] = supportFaqs;

  return (
    <main className={styles.index}>
      <article className={styles.content}>
        <p className={styles.eyebrow}>SupaDatewise support</p>
        <h1 className={styles.heading}>Estimated delivery dates for Shopify, explained</h1>
        <p className={styles.text}>
          SupaDatewise is a Shopify app that helps merchants show a calculated
          delivery-date estimate on product and cart pages. The answer is
          configured by the merchant: processing time, cutoff, shipping window,
          and working days determine the date shoppers see. It is an estimate,
          not a carrier promise. Install and current plan details are maintained
          on the Shopify App Store.
        </p>
        <p className={styles.meta}>
          Source and current plan: {" "}
          <a href={SHOPIFY_LISTING_URL}>SupaDatewise on the Shopify App Store</a>.
          Last verified: {LAST_VERIFIED}.
        </p>
        <section className={styles.answer}>
          <h2>{featuredFaq.question}</h2>
          <p>{featuredFaq.answer}</p>
        </section>
        <section aria-labelledby="support-faqs">
          <h2 id="support-faqs">Frequently asked questions</h2>
          {additionalFaqs.map((faq) => (
            <section className={styles.answer} key={faq.question}>
              <h3>{faq.question}</h3>
              <p>{faq.answer}</p>
            </section>
          ))}
        </section>
        <p>
          <a className={styles.button} href={SHOPIFY_LISTING_URL}>
            View SupaDatewise in the Shopify App Store
          </a>
        </p>
      </article>
      <script
        type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: siteJson(faqSchema(supportFaqs)) }}
      />
    </main>
  );
}
