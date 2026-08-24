import assert from "node:assert/strict";
import test from "node:test";

import {
  LAST_VERIFIED,
  SHOPIFY_LISTING_URL,
  SHOPIFY_METAFIELD_LIFECYCLE_URL,
  faqSchema,
  privacyFaqs,
  supportFaqs,
} from "./site-data.ts";

const wordCount = (value: string) => value.trim().split(/\s+/).length;

test("AEO FAQ answers are substantial and schema mirrors visible source data", () => {
  const allFaqs = [...supportFaqs, ...privacyFaqs];

  for (const faq of allFaqs) {
    assert.ok(wordCount(faq.answer) >= 50, `${faq.question} is too short`);
    assert.ok(wordCount(faq.answer) <= 80, `${faq.question} is too long`);
  }

  const schema = faqSchema(supportFaqs);
  assert.deepEqual(
    schema.mainEntity.map((item) => [item.name, item.acceptedAnswer.text]),
    supportFaqs.map((faq) => [faq.question, faq.answer])
  );
});

test("official source, price freshness, and listing path are explicit", () => {
  assert.equal(LAST_VERIFIED, "2026-08-24");
  assert.equal(
    SHOPIFY_LISTING_URL,
    "https://apps.shopify.com/estimated-delivery-date-6"
  );
  assert.equal(
    SHOPIFY_METAFIELD_LIFECYCLE_URL,
    "https://shopify.dev/docs/apps/build/metafields/definitions"
  );
});
