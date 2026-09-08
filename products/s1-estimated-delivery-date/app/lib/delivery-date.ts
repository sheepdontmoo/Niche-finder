/**
 * Pure, dependency-free delivery-date estimation.
 *
 * This module is the heart of the app and is intentionally free of any
 * Shopify / React Router / DOM imports so it can be unit-tested in plain Node
 * AND ported verbatim into the theme app extension's client-side JS. Keep the
 * algorithm here identical to `extensions/delivery-date/assets/delivery-date.js`.
 */

export interface DeliverySettings {
  /** Master on/off. When false the storefront block renders nothing. */
  enabled: boolean;
  /** Business days spent preparing an order before it ships. */
  processingDays: number;
  /** Hour of day (0-23, shop local time). Orders placed at/after this hour
   *  are treated as placed the next day. */
  cutoffHour: number;
  /** Minimum shipping transit time, in business days. */
  transitDaysMin: number;
  /** Maximum shipping transit time, in business days. */
  transitDaysMax: number;
  /** Days the business ships on. 0=Sunday … 6=Saturday. */
  workingDays: number[];
  /** Copy template. Placeholders: {date}, {min}, {max}. */
  template: string;
  /** Intl date style used to format the estimate. */
  dateStyle: "full" | "long" | "medium" | "short";
  /** BCP-47 locale for formatting, e.g. "en-IE". */
  locale: string;
  /** IANA timezone used for the merchant's daily cutoff, e.g. "Europe/Dublin". */
  timeZone: string;
}

const FALLBACK_TIME_ZONE = "UTC";

export const DEFAULT_SETTINGS: DeliverySettings = {
  enabled: true,
  processingDays: 1,
  cutoffHour: 14,
  transitDaysMin: 3,
  transitDaysMax: 5,
  workingDays: [1, 2, 3, 4, 5], // Mon–Fri
  template: "Order today to get it by {date}",
  dateStyle: "medium",
  locale: "en-IE",
  timeZone: FALLBACK_TIME_ZONE,
};

function normalizeTimeZone(value: unknown): string {
  if (typeof value !== "string" || !value.trim()) return FALLBACK_TIME_ZONE;
  const timeZone = value.trim();
  try {
    new Intl.DateTimeFormat("en", { timeZone }).format(new Date(0));
    return timeZone;
  } catch {
    return FALLBACK_TIME_ZONE;
  }
}

/** Clamp/validate an untrusted settings object into a safe DeliverySettings. */
export function normalizeSettings(
  input: Partial<DeliverySettings> | null | undefined,
): DeliverySettings {
  const s = { ...DEFAULT_SETTINGS, ...(input ?? {}) };
  const clampInt = (v: unknown, min: number, max: number, fallback: number) => {
    const n = Math.round(Number(v));
    return Number.isFinite(n) ? Math.min(max, Math.max(min, n)) : fallback;
  };
  const workingDays = Array.isArray(s.workingDays)
    ? Array.from(
        new Set(
          s.workingDays
            .map((d) => Math.round(Number(d)))
            .filter((d) => Number.isInteger(d) && d >= 0 && d <= 6),
        ),
      ).sort((a, b) => a - b)
    : DEFAULT_SETTINGS.workingDays;

  const min = clampInt(s.transitDaysMin, 0, 365, DEFAULT_SETTINGS.transitDaysMin);
  const max = clampInt(s.transitDaysMax, 0, 365, DEFAULT_SETTINGS.transitDaysMax);

  return {
    enabled: Boolean(s.enabled),
    processingDays: clampInt(s.processingDays, 0, 365, DEFAULT_SETTINGS.processingDays),
    cutoffHour: clampInt(s.cutoffHour, 0, 23, DEFAULT_SETTINGS.cutoffHour),
    transitDaysMin: Math.min(min, max),
    transitDaysMax: Math.max(min, max),
    workingDays: workingDays.length ? workingDays : DEFAULT_SETTINGS.workingDays,
    template:
      typeof s.template === "string" && s.template.trim()
        ? s.template
        : DEFAULT_SETTINGS.template,
    dateStyle: (["full", "long", "medium", "short"] as const).includes(
      s.dateStyle as never,
    )
      ? s.dateStyle
      : DEFAULT_SETTINGS.dateStyle,
    locale:
      typeof s.locale === "string" && s.locale.trim()
        ? s.locale
        : DEFAULT_SETTINGS.locale,
    timeZone: normalizeTimeZone(s.timeZone),
  };
}

function isWorkingDay(date: Date, workingDays: number[]): boolean {
  return workingDays.includes(date.getUTCDay());
}

/** Advance to the next working day (no-op if already a working day). */
function toWorkingDay(date: Date, workingDays: number[]): Date {
  const d = new Date(date);
  // Guard against an empty set (normalizeSettings prevents it, but be safe).
  if (workingDays.length === 0) return d;
  let guard = 0;
  while (!isWorkingDay(d, workingDays) && guard < 14) {
    d.setUTCDate(d.getUTCDate() + 1);
    guard++;
  }
  return d;
}

/** Add N business days, skipping non-working days. */
function addBusinessDays(date: Date, days: number, workingDays: number[]): Date {
  const d = toWorkingDay(date, workingDays);
  let remaining = Math.max(0, days);
  let guard = 0;
  while (remaining > 0 && guard < 3650) {
    d.setUTCDate(d.getUTCDate() + 1);
    if (isWorkingDay(d, workingDays)) remaining--;
    guard++;
  }
  return d;
}

function zonedDateTimeParts(now: Date, timeZone: string) {
  const parts = new Intl.DateTimeFormat("en-US-u-ca-gregory-nu-latn", {
    timeZone,
    year: "numeric",
    month: "numeric",
    day: "numeric",
    hour: "numeric",
    hourCycle: "h23",
  }).formatToParts(now);
  const value = (type: Intl.DateTimeFormatPartTypes) =>
    Number(parts.find((part) => part.type === type)?.value);
  return {
    year: value("year"),
    month: value("month"),
    day: value("day"),
    hour: value("hour"),
  };
}

export interface DeliveryEstimate {
  /** Earliest expected arrival (local midnight). */
  min: Date;
  /** Latest expected arrival (local midnight). */
  max: Date;
  /** True when min and max fall on the same day. */
  single: boolean;
}

/**
 * Estimate the delivery window for an order placed at `now`.
 *
 * Model: order → (cutoff bump) → processing business days → ship date →
 * transit business days → arrival. The order's calendar day and hour are
 * resolved in the merchant's configured IANA timezone, never the shopper's
 * browser timezone or the server timezone.
 */
export function estimateDelivery(
  now: Date,
  settings: DeliverySettings,
): DeliveryEstimate {
  const s = normalizeSettings(settings);
  const parts = zonedDateTimeParts(now, s.timeZone);

  // UTC is used as a stable carrier for merchant-local calendar dates. This
  // prevents the runtime's own timezone from shifting the resulting day.
  const start = new Date(Date.UTC(parts.year, parts.month - 1, parts.day));
  // Past the cutoff hour → treat as the next day.
  if (parts.hour >= s.cutoffHour) {
    start.setUTCDate(start.getUTCDate() + 1);
  }

  // Ship date = start rolled to a working day + processing business days.
  const shipDate = addBusinessDays(start, s.processingDays, s.workingDays);

  const min = addBusinessDays(shipDate, s.transitDaysMin, s.workingDays);
  const max = addBusinessDays(shipDate, s.transitDaysMax, s.workingDays);

  return {
    min,
    max,
    single: min.getTime() === max.getTime(),
  };
}

/** Render the estimate to a display string using the settings' template. */
export function formatEstimate(
  estimate: DeliveryEstimate,
  settings: DeliverySettings,
): string {
  const s = normalizeSettings(settings);
  const fmt = (d: Date) => {
    try {
      return new Intl.DateTimeFormat(s.locale, {
        dateStyle: s.dateStyle,
        timeZone: "UTC",
      }).format(d);
    } catch {
      return new Intl.DateTimeFormat(undefined, {
        dateStyle: s.dateStyle,
        timeZone: "UTC",
      }).format(d);
    }
  };

  const minStr = fmt(estimate.min);
  const maxStr = fmt(estimate.max);
  const dateStr = estimate.single ? minStr : `${minStr} – ${maxStr}`;

  return s.template
    .replace(/\{date\}/g, dateStr)
    .replace(/\{min\}/g, minStr)
    .replace(/\{max\}/g, maxStr);
}
