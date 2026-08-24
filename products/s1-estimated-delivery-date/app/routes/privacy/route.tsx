import {
  LAST_VERIFIED,
  SHOPIFY_METAFIELD_LIFECYCLE_URL,
  SITE_URL,
  faqSchema,
  privacyFaqs,
  siteJson,
} from "../../lib/site-data";
import styles from "../_index/styles.module.css";

export const meta = () => [
  { title: "Privacy | SupaDatewise" },
  {
    name: "description",
    content:
      "How SupaDatewise handles merchant delivery settings, Shopify sessions, and customer data.",
  },
  { tagName: "link", rel: "canonical", href: `${SITE_URL}/privacy` },
];

export default function Privacy() {
  return (
    <main className={styles.index}>
      <article className={styles.content}>
        <p className={styles.eyebrow}>SupaDatewise privacy</p>
        <h1 className={styles.heading}>What SupaDatewise stores and does not store</h1>
        <p className={styles.text}>
          SupaDatewise stores merchant delivery settings—such as processing
          time, cutoff, shipping window, working days, wording, date format,
          and locale—plus a standard Shopify app session needed to operate the
          app. It does not collect or store shopper names, emails, addresses,
          or order contents. The delivery estimate is calculated in the
          shopper’s browser from the merchant’s selected rules.
        </p>
        <p className={styles.meta}>
          Platform source: {" "}
          <a href={SHOPIFY_METAFIELD_LIFECYCLE_URL}>
            Shopify metafield-definition lifecycle
          </a>
          . Last verified: {LAST_VERIFIED}.
        </p>
        <section aria-labelledby="privacy-faqs">
          <h2 id="privacy-faqs">Privacy questions</h2>
          {privacyFaqs.map((faq) => (
            <section className={styles.answer} key={faq.question}>
              <h3>{faq.question}</h3>
              <p>{faq.answer}</p>
            </section>
          ))}
        </section>
        <p>
          For a privacy question, contact the support contact listed on the
          Shopify App Store page for SupaDatewise.
        </p>
      </article>
      <script
        type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: siteJson(faqSchema(privacyFaqs)) }}
      />
    </main>
  );
}
