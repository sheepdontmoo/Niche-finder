/**
 * ACQ diagnostic engine.
 *
 * Pure logic — no AI, no I/O, no framework. Given a snapshot of a business's
 * metrics it computes the ratios, walks the constraint ladder, and returns the
 * single binding constraint plus what to do about it.
 *
 * Ported from dashboard/acq-console.html. Rules are documented in
 * knowledge/acq/00-diagnosis.md.
 *
 * Invariant: a null metric means "not tracked" and must never be coerced to 0.
 * A missing number is a finding, not a zero.
 */

export type BusinessModel =
  | 'Services' | 'Product' | 'SaaS' | 'Ecommerce' | 'Local' | 'Info' | 'Other';

export interface Snapshot {
  revenue: number | null;
  gross_profit: number | null;
  owner_hours: number | null;
  cac: number | null;
  ltgp: number | null;
  fecc: number | null;
  payback_days: number | null;
  customers_per_month: number | null;
  cv_optin: number | null;
  cv_booked: number | null;
  cv_showed: number | null;
  cv_closed: number | null;
  leads_per_month: number | null;
  speed_to_lead_seconds: number | null;
  page_load_seconds: number | null;
  closers_count: number | null;
  channels: Channel[];
  offer_types: OfferType[];
  has_rubric: boolean;
}

export type Channel = 'warm' | 'content' | 'cold' | 'paid';
export type OfferType = 'attraction' | 'upsell' | 'downsell' | 'continuity';
export type StepKey = 'cv_optin' | 'cv_booked' | 'cv_showed' | 'cv_closed';
export type RungKey = 'conversion' | 'leads' | 'sales' | 'ltv' | 'cash' | 'people';
export type RungState = 'ok' | 'at-risk' | 'binding' | 'unknown';
export type GrowthLevel = '$0–1M' | '$1–3M' | '$3–10M' | '$10–100M';

/** Targets. Rules of thumb, not laws — the UI must say so. */
export const TARGETS = { ltgpCac: 3, feccCac: 2, paybackDays: 30 } as const;

export const STEPS: { key: StepKey; label: string }[] = [
  { key: 'cv_optin', label: 'Visit → opt-in' },
  { key: 'cv_booked', label: 'Opt-in → booked' },
  { key: 'cv_showed', label: 'Booked → showed' },
  { key: 'cv_closed', label: 'Showed → closed' },
];

export type Benchmarks = Record<StepKey, number>;

/**
 * Benchmarks per business model. These are defaults seeded into the
 * funnel_benchmarks table; the app should read that table so they can be tuned
 * without a code change. A wrong benchmark yields a confidently wrong verdict,
 * so treat any model not measured here as provisional.
 */
export const DEFAULT_BENCHMARKS: Record<BusinessModel, Benchmarks> = {
  Services:  { cv_optin: 30, cv_booked: 40, cv_showed: 70, cv_closed: 25 },
  Local:     { cv_optin: 30, cv_booked: 45, cv_showed: 75, cv_closed: 30 },
  Info:      { cv_optin: 35, cv_booked: 30, cv_showed: 60, cv_closed: 15 },
  SaaS:      { cv_optin: 25, cv_booked: 30, cv_showed: 70, cv_closed: 20 },
  Ecommerce: { cv_optin: 20, cv_booked: 15, cv_showed: 90, cv_closed: 40 },
  Product:   { cv_optin: 25, cv_booked: 30, cv_showed: 80, cv_closed: 30 },
  Other:     { cv_optin: 30, cv_booked: 40, cv_showed: 70, cv_closed: 25 },
};

export interface StepResult {
  key: StepKey; label: string;
  value: number | null; bench: number; ratio: number | null;
}

export interface Metrics {
  margin: number | null;
  ltgpCac: number | null;
  feccCac: number | null;
  paybackDays: number | null;
  steps: StepResult[];
  weakest: StepResult | null;
  endToEnd: number | null;
  stage: GrowthLevel | null;
  stageFocus: string;
}

export interface Rung { key: RungKey; name: string; state: RungState; why: string }
export interface Diagnosis { rungs: Rung[]; binding: Rung | null }

const has = (v: number | null | undefined): v is number =>
  v !== null && v !== undefined && !Number.isNaN(v);

const money = (n: number): string =>
  Math.abs(n) >= 1e6 ? `$${(n / 1e6).toFixed(n % 1e6 === 0 ? 0 : 1)}M`
  : Math.abs(n) >= 1e3 ? `$${Math.round(n / 1e3)}k`
  : `$${Math.round(n)}`;

const STAGE_FOCUS: Record<GrowthLevel, string> = {
  '$0–1M': 'Validate the core offer. One offer, one channel, one avatar.',
  '$1–3M': 'Avoid distraction. Double down on what already works.',
  '$3–10M': 'Build and empower a team. Move from doing to managing.',
  '$10–100M': 'Subject-matter experts per department.',
};

export function compute(s: Snapshot, bench: Benchmarks): Metrics {
  const margin = has(s.revenue) && has(s.gross_profit) && s.revenue > 0
    ? s.gross_profit / s.revenue : null;
  const ltgpCac = has(s.ltgp) && has(s.cac) && s.cac > 0 ? s.ltgp / s.cac : null;
  const feccCac = has(s.fecc) && has(s.cac) && s.cac > 0 ? s.fecc / s.cac : null;

  const steps: StepResult[] = STEPS.map(({ key, label }) => {
    const value = has(s[key]) ? (s[key] as number) : null;
    return { key, label, value, bench: bench[key], ratio: value === null ? null : value / bench[key] };
  });

  const known = steps.filter((x) => x.value !== null);
  const endToEnd = known.length === STEPS.length
    ? steps.reduce((a, x) => a * ((x.value as number) / 100), 1) * 100 : null;

  let weakest: StepResult | null = null;
  for (const x of known) {
    if (!weakest || (x.ratio as number) < (weakest.ratio as number)) weakest = x;
  }

  const stage: GrowthLevel | null = !has(s.revenue) ? null
    : s.revenue < 1e6 ? '$0–1M'
    : s.revenue < 3e6 ? '$1–3M'
    : s.revenue < 10e6 ? '$3–10M' : '$10–100M';

  return {
    margin, ltgpCac, feccCac, paybackDays: has(s.payback_days) ? s.payback_days : null,
    steps, weakest, endToEnd, stage, stageFocus: stage ? STAGE_FOCUS[stage] : '',
  };
}

/**
 * Walk the ladder in order: conversion → leads → sales → LTV → cash → people.
 * The FIRST at-risk rung binds; later ones stay at-risk. The ordering is the
 * logic — conversion is deliberately checked before leads so the engine can
 * never advise buying traffic to feed a leaky funnel.
 */
export function diagnose(s: Snapshot, m: Metrics): Diagnosis {
  const rungs: Rung[] = [];
  const add = (key: RungKey, name: string, state: RungState, why: string) =>
    rungs.push({ key, name, state, why });

  // 0 — Conversion (hidden constraint)
  if (m.weakest && (m.weakest.ratio as number) < 0.7) {
    const w = m.weakest;
    add('conversion', 'Conversion (hidden constraint)', 'at-risk',
      `${w.label} converts at ${w.value}%, against a ${w.bench}% benchmark — ` +
      `${Math.round((1 - (w.ratio as number)) * 100)}% below. Buying more traffic to feed this is the most expensive available mistake.`);
  } else if (!m.weakest) {
    add('conversion', 'Conversion (hidden constraint)', 'unknown',
      'No funnel data entered. This is checked before leads for a reason — verify it before spending on traffic.');
  } else {
    add('conversion', 'Conversion (hidden constraint)', 'ok',
      'Every measured step is within range of its benchmark.');
  }

  // 1 — Leads
  const ch = s.channels ?? [];
  if (ch.length === 0) {
    add('leads', 'Leads', 'unknown',
      'No channels marked. Of the Core Four — warm, content, cold, paid — which are actually running at real volume?');
  } else if (ch.length === 1 && ch[0] === 'paid') {
    add('leads', 'Leads', 'at-risk',
      'Paid ads only. The sequencing rule is warm → content → cold → paid; paid amplifies whatever is already true, including a broken funnel.');
  } else if (!has(s.leads_per_month)) {
    add('leads', 'Leads', 'unknown',
      `${ch.length} of the Core Four running, but lead volume isn't tracked.`);
  } else {
    add('leads', 'Leads', 'ok',
      `${s.leads_per_month} leads/mo across ${ch.length} of the Core Four. Exhaust More before reaching for New.`);
  }

  // 2 — Sales
  const sales: string[] = [];
  const benchClosed = m.steps.find((x) => x.key === 'cv_closed')!.bench;
  const benchShowed = m.steps.find((x) => x.key === 'cv_showed')!.bench;
  if (has(s.speed_to_lead_seconds) && s.speed_to_lead_seconds > 60)
    sales.push(`speed to lead is ${s.speed_to_lead_seconds}s against a 60s target — conversion falls roughly 70% → 20% once a lead leaves without booking`);
  if (has(s.cv_closed) && s.cv_closed < benchClosed * 0.7)
    sales.push(`close rate ${s.cv_closed}% vs ~${benchClosed}% benchmark`);
  if (has(s.cv_showed) && s.cv_showed < benchShowed * 0.8)
    sales.push(`show rate ${s.cv_showed}% vs ~${benchShowed}% benchmark`);
  if (has(s.closers_count) && s.closers_count >= 3 && !s.has_rubric)
    sales.push(`${s.closers_count} reps with no call rubric — performance can't be diagnosed or delegated`);
  if (sales.length) add('sales', 'Sales', 'at-risk', sales.join('; ') + '.');
  else if (!has(s.speed_to_lead_seconds) && !has(s.cv_closed))
    add('sales', 'Sales', 'unknown', 'No close rate or speed-to-lead recorded.');
  else add('sales', 'Sales', 'ok', 'Close rate, show rate and speed to lead are within range.');

  // 3 — LTV
  const offers = s.offer_types ?? [];
  const ltv: string[] = [];
  if (m.ltgpCac !== null && m.ltgpCac < TARGETS.ltgpCac)
    ltv.push(`LTGP:CAC is ${m.ltgpCac.toFixed(2)} against a target above ${TARGETS.ltgpCac}` +
      (m.ltgpCac < 1 ? ' — below 1, growth actively destroys the business' : " — growth doesn't compound here"));
  if (!offers.includes('continuity'))
    ltv.push('no continuity offer, so lifetime gross profit is capped at a single transaction');
  if (ltv.length) add('ltv', 'LTV', 'at-risk', ltv.join('; ') + '.');
  else if (m.ltgpCac === null)
    add('ltv', 'LTV', 'unknown', "LTGP or CAC missing — the return on acquisition can't be computed.");
  else add('ltv', 'LTV', 'ok', `LTGP:CAC ${m.ltgpCac.toFixed(2)}, above the ${TARGETS.ltgpCac}× threshold.`);

  // 4 — Margin & cash
  const cash: string[] = [];
  if (m.feccCac !== null && m.feccCac < TARGETS.feccCac)
    cash.push(`FECC:CAC is ${m.feccCac.toFixed(2)} against a target above ${TARGETS.feccCac} — you can't collect enough up front to re-fund acquisition, so growth is capped by the balance sheet rather than by demand`);
  if (m.paybackDays !== null && m.paybackDays > TARGETS.paybackDays)
    cash.push(`payback takes ${m.paybackDays} days against a ${TARGETS.paybackDays}-day target, so the same dollar can't be recycled monthly`);
  if (!offers.includes('upsell'))
    cash.push('no upsell at point of sale — the highest-intent moment is going unused');
  if (!offers.includes('downsell'))
    cash.push("no downsell, so every 'no' is a fully-funded loss");
  if (cash.length) add('cash', 'Margin & cash', 'at-risk', cash.join('; ') + '.');
  else if (m.feccCac === null && m.paybackDays === null)
    add('cash', 'Margin & cash', 'unknown', 'FECC and payback period missing.');
  else add('cash', 'Margin & cash', 'ok', 'Front-end cash covers acquisition and payback is inside 30 days.');

  // 5 — People & ops
  const ppl: string[] = [];
  if (has(s.owner_hours) && s.owner_hours >= 20 && has(s.revenue) && s.revenue >= 1e6)
    ppl.push(`the owner is still ${s.owner_hours} hrs/wk inside delivery at ${money(s.revenue)} — at this stage the owner is the constraint`);
  if (m.margin !== null && m.margin < 0.3)
    ppl.push(`gross margin is ${Math.round(m.margin * 100)}%, thin enough that delivery cost limits what you can spend to grow`);
  if (ppl.length) add('people', 'People & ops', 'at-risk', ppl.join('; ') + '.');
  else if (!has(s.owner_hours))
    add('people', 'People & ops', 'unknown', 'Owner hours in delivery not recorded.');
  else add('people', 'People & ops', 'ok', 'Owner is out of delivery and margin supports the next stage.');

  const binding = rungs.find((r) => r.state === 'at-risk') ?? null;
  if (binding) binding.state = 'binding';
  return { rungs, binding };
}

/** What lifting the weakest step to benchmark is worth, as a growth multiple. */
export function prize(m: Metrics): { step: StepResult; upliftPct: number; endToEndAfter: number | null } | null {
  const w = m.weakest;
  if (!w || w.value === null || w.value <= 0 || (w.ratio as number) >= 1) return null;
  const uplift = w.bench / w.value - 1;
  return {
    step: w,
    upliftPct: uplift * 100,
    endToEndAfter: m.endToEnd === null ? null : m.endToEnd * (1 + uplift),
  };
}

export interface Prescription { do: string; meta: string; watch: string }

export const PRESCRIPTIONS: Record<RungKey, Prescription[]> = {
  conversion: [
    { do: 'Run one CRO test per week on the weakest step', meta: 'Target a ~50% relative lift, not 5%. Concentrate 80% of the work above the fold on both desktop and mobile.', watch: 'step conversion %' },
    { do: 'Audit page load speed', meta: 'Roughly 7% of revenue is lost per second of delay. Cheapest win available and it compounds against every future traffic dollar.', watch: 'load time (s)' },
    { do: 'Restructure the opt-in page: headline → video/image → testimonials', meta: 'All three above the fold where possible. Use incomplete-information bias — blurred assets, locked video — to lift opt-ins.', watch: 'opt-in rate' },
  ],
  leads: [
    { do: 'Scale what already works before adding anything new', meta: "More, then Better, then New. Most 'the channel stopped working' is actually 'we never scaled it'.", watch: 'leads/mo' },
    { do: 'Turn on warm outreach properly', meta: "Contact people who already know you and ask who else they know — don't pitch. Fastest zero-cost path, routinely skipped because it's uncomfortable.", watch: 'leads/mo from warm' },
    { do: 'Build a lead magnet good enough to charge for', meta: 'Solve a narrow problem completely so it reveals the broader one your paid offer solves.', watch: 'cost per lead' },
  ],
  sales: [
    { do: 'Get median speed to lead under 60 seconds', meta: 'Unbooked applicants go open; first setter claims with a double dial + text; lead stays live 7 days if contacted daily.', watch: 'median speed to lead (s)' },
    { do: 'Build a 31-point call rubric and grade weekly', meta: 'Score not-quite / good / great. Give feedback on the single lowest component only — multiple corrections produce zero change.', watch: 'close rate %' },
    { do: 'Switch to weekly sales competitions', meta: 'Deliver rewards randomly, quickly and publicly. Weekly keeps all seven days live and lets a bad week reset.', watch: 'close rate %' },
  ],
  ltv: [
    { do: 'Attach a continuity offer to the outcome', meta: 'What recurring need does the result create? Recurring revenue is what makes LTGP large enough to clear 3× CAC.', watch: 'LTGP:CAC' },
    { do: 'Raise price with a transition date', meta: 'Existing clients keep the old price until date X; new clients pay more. Creates urgency without discounting.', watch: 'LTGP' },
    { do: 'Fix retention before buying more traffic', meta: 'Churn caps lifetime gross profit no matter how good acquisition gets.', watch: 'monthly churn %' },
  ],
  cash: [
    { do: 'Add an upsell at the point of sale', meta: 'The moment right after purchase is the highest-intent moment you will ever have. This is where CAC actually gets paid back.', watch: 'payback (days)' },
    { do: "Add a downsell to every 'no'", meta: 'You already paid to acquire that conversation. A lower-priced alternative converts a funded loss into revenue.', watch: 'FECC:CAC' },
    { do: 'Pull cash earlier: deposits, annual billing, pre-payment discounts', meta: 'Cash timing beats cash amount for growth rate. Earlier beats bigger.', watch: 'payback (days)' },
  ],
  people: [
    { do: 'Get the owner out of delivery', meta: 'Hire for traits and teach the skills. First hires remove the owner from delivery, not from selling.', watch: 'owner hrs/wk in delivery' },
    { do: 'Give every role one owner and one number', meta: 'The three recurring failures are unclear KPIs, weak hiring filters and inconsistent onboarding.', watch: 'gross margin %' },
    { do: 'Unbundle every soft skill into gradeable behaviour', meta: "If it can't be graded it can't be trained.", watch: 'delivery consistency' },
  ],
};

export const NEXT_CONSTRAINT: Record<RungKey, string> = {
  conversion: 'Sales capacity — a better-converting funnel puts more calls on the calendar than the current team can take.',
  leads: 'Sales — more volume exposes close rate and speed to lead.',
  sales: 'Margin and cash — a higher close rate consumes acquisition cash faster than you collect it.',
  ltv: 'Leads — once each customer is worth more, buying more of them becomes the limit.',
  cash: 'Leads — with a 30-day payback you can spend as fast as you collect, so traffic becomes the ceiling.',
  people: 'Marketing — capacity freed up at the top means the funnel has to fill it.',
};

/** Fields the operator left untracked. Reported, never estimated. */
export const GAP_LABELS: Record<string, string> = {
  revenue: 'Revenue', gross_profit: 'Gross profit', cac: 'CAC', ltgp: 'LTGP',
  fecc: 'FECC', payback_days: 'Payback period', cv_optin: 'Opt-in rate',
  cv_booked: 'Booked rate', cv_showed: 'Show rate', cv_closed: 'Close rate',
  leads_per_month: 'Leads/mo', speed_to_lead_seconds: 'Speed to lead',
  page_load_seconds: 'Page load', owner_hours: 'Owner hours',
};

export function gaps(s: Snapshot): string[] {
  return Object.keys(GAP_LABELS)
    .filter((k) => !has(s[k as keyof Snapshot] as number | null))
    .map((k) => GAP_LABELS[k]);
}

export const emptySnapshot = (): Snapshot => ({
  revenue: null, gross_profit: null, owner_hours: null, cac: null, ltgp: null,
  fecc: null, payback_days: null, customers_per_month: null, cv_optin: null,
  cv_booked: null, cv_showed: null, cv_closed: null, leads_per_month: null,
  speed_to_lead_seconds: null, page_load_seconds: null, closers_count: null,
  channels: [], offer_types: ['attraction'], has_rubric: false,
});
