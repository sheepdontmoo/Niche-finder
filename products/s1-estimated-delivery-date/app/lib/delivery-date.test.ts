import { describe, it } from "node:test";
import assert from "node:assert/strict";
import {
  DEFAULT_SETTINGS,
  estimateDelivery,
  formatEstimate,
  normalizeSettings,
  type DeliverySettings,
} from "./delivery-date.ts";

// Fixed UTC references. The base settings use UTC, so these are independent
// of the machine timezone running the test suite.
const MON_9AM = new Date("2026-01-05T09:00:00Z");
const MON_4PM = new Date("2026-01-05T16:00:00Z");
const FRI_9AM = new Date("2026-01-09T09:00:00Z");

const base: DeliverySettings = {
  ...DEFAULT_SETTINGS,
  processingDays: 1,
  cutoffHour: 14,
  transitDaysMin: 3,
  transitDaysMax: 5,
  workingDays: [1, 2, 3, 4, 5],
};

const ymd = (d: Date) =>
  `${d.getUTCFullYear()}-${String(d.getUTCMonth() + 1).padStart(2, "0")}-${String(
    d.getUTCDate(),
  ).padStart(2, "0")}`;

describe("estimateDelivery", () => {
  it("orders before cutoff ship same-day-start", () => {
    // Mon start → +1 processing = Tue ship → +3..+5 business = Fri..Wed(next)
    const e = estimateDelivery(MON_9AM, base);
    assert.equal(ymd(e.min), "2026-01-09"); // Fri
    assert.equal(ymd(e.max), "2026-01-13"); // next Tue (skips Sat/Sun)
    assert.equal(e.single, false);
  });

  it("orders at/after cutoff roll to the next day", () => {
    const e = estimateDelivery(MON_4PM, base);
    // start bumps Mon→Tue, +1 processing = Wed ship → +3..+5 = Mon..Wed
    assert.equal(ymd(e.min), "2026-01-12"); // Mon
    assert.equal(ymd(e.max), "2026-01-14"); // Wed
  });

  it("skips weekends when counting business days", () => {
    // Fri start, 0 processing → ship Fri → +1 business day = Mon (not Sat)
    const e = estimateDelivery(FRI_9AM, {
      ...base,
      processingDays: 0,
      transitDaysMin: 1,
      transitDaysMax: 1,
    });
    assert.equal(ymd(e.min), "2026-01-12"); // Mon
    assert.equal(e.single, true);
  });

  it("single-day window when min === max transit", () => {
    const e = estimateDelivery(MON_9AM, {
      ...base,
      transitDaysMin: 2,
      transitDaysMax: 2,
    });
    assert.equal(e.single, true);
  });

  it("rolls a start that lands on a non-working day forward", () => {
    // Ship only on Mondays; order Tue → next working day is next Mon.
    const e = estimateDelivery(new Date("2026-01-06T09:00:00Z"), {
      ...base,
      workingDays: [1],
      processingDays: 0,
      transitDaysMin: 0,
      transitDaysMax: 0,
    });
    assert.equal(ymd(e.min), "2026-01-12"); // the following Monday
  });

  it("uses the merchant timezone rather than the shopper or server timezone", () => {
    const instant = new Date("2026-01-05T14:30:00Z");
    const newYork = estimateDelivery(instant, {
      ...base,
      timeZone: "America/New_York",
    });
    const utc = estimateDelivery(instant, { ...base, timeZone: "UTC" });

    // 14:30Z is 09:30 in New York (before cutoff) but 14:30 in UTC (after).
    assert.equal(ymd(newYork.min), "2026-01-09");
    assert.equal(ymd(utc.min), "2026-01-12");
  });
});

describe("normalizeSettings", () => {
  it("clamps out-of-range numbers and swaps inverted transit bounds", () => {
    const s = normalizeSettings({
      cutoffHour: 99,
      processingDays: -3,
      transitDaysMin: 8,
      transitDaysMax: 2,
    });
    assert.equal(s.cutoffHour, 23);
    assert.equal(s.processingDays, 0);
    assert.equal(s.transitDaysMin, 2);
    assert.equal(s.transitDaysMax, 8);
  });

  it("dedupes/sorts working days and rejects invalid ones", () => {
    const s = normalizeSettings({ workingDays: [5, 1, 1, 9, -2, 3] as number[] });
    assert.deepEqual(s.workingDays, [1, 3, 5]);
  });

  it("falls back to defaults for empty working days", () => {
    const s = normalizeSettings({ workingDays: [] });
    assert.deepEqual(s.workingDays, DEFAULT_SETTINGS.workingDays);
  });

  it("keeps a valid custom template and rejects an empty one", () => {
    assert.equal(normalizeSettings({ template: "  " }).template, DEFAULT_SETTINGS.template);
    assert.equal(normalizeSettings({ template: "Arrives {min}" }).template, "Arrives {min}");
  });

  it("keeps valid IANA timezones and rejects invalid values", () => {
    assert.equal(normalizeSettings({ timeZone: "Europe/Dublin" }).timeZone, "Europe/Dublin");
    assert.equal(normalizeSettings({ timeZone: "Not/A_Zone" }).timeZone, "UTC");
  });
});

describe("formatEstimate", () => {
  it("substitutes {date} with a single date when single", () => {
    const e = estimateDelivery(MON_9AM, {
      ...base,
      transitDaysMin: 2,
      transitDaysMax: 2,
    });
    const out = formatEstimate(e, { ...base, template: "Get it by {date}", locale: "en-IE" });
    assert.match(out, /^Get it by /);
    assert.ok(!out.includes("–"), "single day should not contain a range dash");
  });

  it("renders a range for a min/max window", () => {
    const e = estimateDelivery(MON_9AM, base);
    const out = formatEstimate(e, { ...base, template: "{min} to {max}" });
    assert.match(out, / to /);
  });

  it("falls back safely when a merchant saved an invalid locale", () => {
    const e = estimateDelivery(MON_9AM, base);
    assert.doesNotThrow(() =>
      formatEstimate(e, { ...base, locale: "not_a_locale" }),
    );
  });
});
