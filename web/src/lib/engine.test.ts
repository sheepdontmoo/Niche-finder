import { describe, it, expect } from 'vitest';
import {
  compute, diagnose, prize, gaps, emptySnapshot,
  DEFAULT_BENCHMARKS, type Snapshot,
} from './engine';

const B = DEFAULT_BENCHMARKS.Services;

/** A business with no binding constraint — every rung clear. */
const healthy = (over: Partial<Snapshot> = {}): Snapshot => ({
  ...emptySnapshot(),
  revenue: 4_000_000, gross_profit: 3_000_000, owner_hours: 5,
  cac: 1_000, ltgp: 5_000, fecc: 2_500, payback_days: 20,
  customers_per_month: 40,
  cv_optin: 32, cv_booked: 42, cv_showed: 72, cv_closed: 26,
  leads_per_month: 800, speed_to_lead_seconds: 45, page_load_seconds: 1.4,
  closers_count: 4, channels: ['warm', 'content', 'paid'],
  offer_types: ['attraction', 'upsell', 'downsell', 'continuity'],
  has_rubric: true,
  ...over,
});

describe('compute', () => {
  it('derives the three ratios', () => {
    const m = compute(healthy(), B);
    expect(m.ltgpCac).toBe(5);
    expect(m.feccCac).toBe(2.5);
    expect(m.paybackDays).toBe(20);
    expect(m.margin).toBe(0.75);
  });

  it('never coerces a missing metric to zero', () => {
    const m = compute(healthy({ cac: null, ltgp: null, revenue: null }), B);
    expect(m.ltgpCac).toBeNull();
    expect(m.margin).toBeNull();
    expect(m.stage).toBeNull();
    // A null CAC must not read as a divide-by-zero or an infinitely good ratio.
    expect(m.ltgpCac).not.toBe(Infinity);
  });

  it('guards against divide-by-zero on a genuine zero CAC', () => {
    expect(compute(healthy({ cac: 0 }), B).ltgpCac).toBeNull();
  });

  it('classifies growth level on the boundaries', () => {
    const at = (revenue: number) => compute(healthy({ revenue }), B).stage;
    expect(at(999_999)).toBe('$0–1M');
    expect(at(1_000_000)).toBe('$1–3M');
    expect(at(2_999_999)).toBe('$1–3M');
    expect(at(3_000_000)).toBe('$3–10M');
    expect(at(9_999_999)).toBe('$3–10M');
    expect(at(10_000_000)).toBe('$10–100M');
  });

  it('finds the weakest step by ratio to benchmark, not by raw value', () => {
    // 12% opt-in (0.40 of bench) is weaker than 22% close (0.88 of bench),
    // even though 22 > 12 in absolute terms.
    const m = compute(healthy({ cv_optin: 12, cv_closed: 22 }), B);
    expect(m.weakest?.key).toBe('cv_optin');
  });

  it('computes end-to-end only when all four steps are known', () => {
    // 32% × 42% × 72% × 26% = 2.516%
    expect(compute(healthy(), B).endToEnd).toBeCloseTo(2.516, 3);
    expect(compute(healthy({ cv_booked: null }), B).endToEnd).toBeNull();
  });
});

describe('diagnose — ladder ordering', () => {
  it('finds no binding constraint in a healthy business', () => {
    const s = healthy();
    expect(diagnose(s, compute(s, B)).binding).toBeNull();
  });

  it('returns exactly one binding rung, never more', () => {
    const s = healthy({ cv_optin: 8, ltgp: 1_000, fecc: 500, payback_days: 90 });
    const { rungs } = diagnose(s, compute(s, B));
    expect(rungs.filter((r) => r.state === 'binding')).toHaveLength(1);
  });

  it('checks conversion BEFORE leads — never advises buying traffic for a leaky funnel', () => {
    // Paid-only channels would flag the leads rung, but conversion is broken and
    // sits above it on the ladder, so conversion must bind.
    const s = healthy({ cv_optin: 8, channels: ['paid'] });
    const d = diagnose(s, compute(s, B));
    expect(d.binding?.key).toBe('conversion');
    expect(d.rungs.find((r) => r.key === 'leads')?.state).toBe('at-risk');
  });

  it('binds the first failing rung and leaves later failures at-risk', () => {
    const s = healthy({ speed_to_lead_seconds: 900, payback_days: 120 });
    const d = diagnose(s, compute(s, B));
    expect(d.binding?.key).toBe('sales');
    expect(d.rungs.find((r) => r.key === 'cash')?.state).toBe('at-risk');
  });

  it('reports unknown rather than ok when the data is absent', () => {
    const s = emptySnapshot();
    const d = diagnose(s, compute(s, B));
    expect(d.rungs.find((r) => r.key === 'conversion')?.state).toBe('unknown');
    expect(d.rungs.find((r) => r.key === 'leads')?.state).toBe('unknown');
  });

  it('flags paid-only channels as a sequencing problem', () => {
    const s = healthy({ channels: ['paid'] });
    expect(diagnose(s, compute(s, B)).binding?.key).toBe('leads');
  });

  it('flags 3+ reps with no rubric as a sales problem', () => {
    const s = healthy({ closers_count: 5, has_rubric: false });
    expect(diagnose(s, compute(s, B)).binding?.key).toBe('sales');
  });

  it('calls out that LTGP:CAC below 1 destroys the business', () => {
    const s = healthy({ ltgp: 500, cac: 1_000 });
    const d = diagnose(s, compute(s, B));
    expect(d.binding?.key).toBe('ltv');
    expect(d.binding?.why).toContain('destroys the business');
  });

  it('treats a missing downsell as a cash leak', () => {
    const s = healthy({ offer_types: ['attraction', 'upsell', 'continuity'] });
    const d = diagnose(s, compute(s, B));
    expect(d.binding?.key).toBe('cash');
    expect(d.binding?.why).toContain('fully-funded loss');
  });

  it('only calls the owner a constraint once the business is past $1M', () => {
    expect(diagnose(healthy({ owner_hours: 40, revenue: 400_000 }), compute(healthy({ owner_hours: 40, revenue: 400_000 }), B)).binding?.key)
      .not.toBe('people');
    const big = healthy({ owner_hours: 40, revenue: 5_000_000 });
    expect(diagnose(big, compute(big, B)).binding?.key).toBe('people');
  });
});

describe('benchmarks are model-specific', () => {
  it('reaches a different verdict for the same numbers under a different model', () => {
    // 22% opt-in clears Ecommerce's 20% bench but fails Services' 30% badly.
    const s = healthy({ cv_optin: 20, cv_booked: 16, cv_showed: 91, cv_closed: 41 });
    const asEcom = diagnose(s, compute(s, DEFAULT_BENCHMARKS.Ecommerce));
    const asServices = diagnose(s, compute(s, DEFAULT_BENCHMARKS.Services));
    expect(asEcom.rungs[0].state).toBe('ok');
    expect(asServices.binding?.key).toBe('conversion');
  });
});

describe('prize', () => {
  it('quantifies lifting the weakest step to benchmark', () => {
    const s = healthy({ cv_optin: 12 });
    const p = prize(compute(s, B));
    // 12% → 30% benchmark is a 2.5× multiple, i.e. +150%.
    expect(p?.upliftPct).toBeCloseTo(150, 5);
    expect(p?.step.key).toBe('cv_optin');
  });

  it('offers no prize when every step already beats its benchmark', () => {
    expect(prize(compute(healthy({ cv_optin: 45, cv_booked: 60, cv_showed: 90, cv_closed: 40 }), B))).toBeNull();
  });
});

describe('gaps', () => {
  it('lists untracked fields and nothing else', () => {
    expect(gaps(healthy())).toEqual([]);
    expect(gaps(healthy({ cac: null, revenue: null }))).toEqual(['Revenue', 'CAC']);
  });
});
