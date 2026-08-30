/**
 * Storefront renderer for the Estimated Delivery Date block.
 *
 * This is a verbatim port of app/lib/delivery-date.ts. Keep the two in sync —
 * the TS version has unit tests (app/lib/delivery-date.test.ts) that lock the
 * behaviour asserted here.
 */
(function () {
  "use strict";

  var entitlementRequest;

  var DEFAULTS = {
    enabled: true,
    processingDays: 1,
    cutoffHour: 14,
    transitDaysMin: 3,
    transitDaysMax: 5,
    workingDays: [1, 2, 3, 4, 5],
    template: "Order today to get it by {date}",
    dateStyle: "medium",
    locale: "en-IE",
    timeZone: "UTC",
  };

  function isValidTimeZone(value) {
    if (typeof value !== "string" || !value.trim()) return false;
    var timeZone = value.trim();
    try {
      new Intl.DateTimeFormat("en", { timeZone: timeZone }).format(new Date(0));
      return true;
    } catch (e) {
      return false;
    }
  }

  function normalizeTimeZone(value) {
    return isValidTimeZone(value) ? value.trim() : DEFAULTS.timeZone;
  }

  function clampInt(v, min, max, fallback) {
    var n = Math.round(Number(v));
    return isFinite(n) ? Math.min(max, Math.max(min, n)) : fallback;
  }

  function normalize(input) {
    var s = Object.assign({}, DEFAULTS, input || {});
    var workingDays = Array.isArray(s.workingDays)
      ? s.workingDays
          .map(function (d) {
            return Math.round(Number(d));
          })
          .filter(function (d, i, arr) {
            return (
              Number.isInteger(d) && d >= 0 && d <= 6 && arr.indexOf(d) === i
            );
          })
          .sort(function (a, b) {
            return a - b;
          })
      : DEFAULTS.workingDays.slice();
    if (!workingDays.length) workingDays = DEFAULTS.workingDays.slice();

    var min = clampInt(s.transitDaysMin, 0, 365, DEFAULTS.transitDaysMin);
    var max = clampInt(s.transitDaysMax, 0, 365, DEFAULTS.transitDaysMax);
    var validStyles = ["full", "long", "medium", "short"];

    return {
      enabled: Boolean(s.enabled),
      processingDays: clampInt(s.processingDays, 0, 365, DEFAULTS.processingDays),
      cutoffHour: clampInt(s.cutoffHour, 0, 23, DEFAULTS.cutoffHour),
      transitDaysMin: Math.min(min, max),
      transitDaysMax: Math.max(min, max),
      workingDays: workingDays,
      template:
        typeof s.template === "string" && s.template.trim()
          ? s.template
          : DEFAULTS.template,
      dateStyle:
        validStyles.indexOf(s.dateStyle) !== -1 ? s.dateStyle : DEFAULTS.dateStyle,
      locale:
        typeof s.locale === "string" && s.locale.trim()
          ? s.locale
          : DEFAULTS.locale,
      timeZone: normalizeTimeZone(s.timeZone),
    };
  }

  function isWorkingDay(date, workingDays) {
    return workingDays.indexOf(date.getUTCDay()) !== -1;
  }

  function toWorkingDay(date, workingDays) {
    var d = new Date(date);
    if (!workingDays.length) return d;
    var guard = 0;
    while (!isWorkingDay(d, workingDays) && guard < 14) {
      d.setUTCDate(d.getUTCDate() + 1);
      guard++;
    }
    return d;
  }

  function addBusinessDays(date, days, workingDays) {
    var d = toWorkingDay(date, workingDays);
    var remaining = Math.max(0, days);
    var guard = 0;
    while (remaining > 0 && guard < 3650) {
      d.setUTCDate(d.getUTCDate() + 1);
      if (isWorkingDay(d, workingDays)) remaining--;
      guard++;
    }
    return d;
  }

  function zonedDateTimeParts(now, timeZone) {
    var parts = new Intl.DateTimeFormat("en-US-u-ca-gregory-nu-latn", {
      timeZone: timeZone,
      year: "numeric",
      month: "numeric",
      day: "numeric",
      hour: "numeric",
      hourCycle: "h23",
    }).formatToParts(now);
    var value = function (type) {
      var part = parts.find(function (candidate) {
        return candidate.type === type;
      });
      return Number(part && part.value);
    };
    return {
      year: value("year"),
      month: value("month"),
      day: value("day"),
      hour: value("hour"),
    };
  }

  function estimate(now, settings) {
    var s = normalize(settings);
    var parts = zonedDateTimeParts(now, s.timeZone);
    var start = new Date(Date.UTC(parts.year, parts.month - 1, parts.day));
    if (parts.hour >= s.cutoffHour) start.setUTCDate(start.getUTCDate() + 1);
    var ship = addBusinessDays(start, s.processingDays, s.workingDays);
    var min = addBusinessDays(ship, s.transitDaysMin, s.workingDays);
    var max = addBusinessDays(ship, s.transitDaysMax, s.workingDays);
    return { min: min, max: max, single: min.getTime() === max.getTime() };
  }

  function format(est, settings) {
    var s = normalize(settings);
    var fmt = function (d) {
      try {
        return new Intl.DateTimeFormat(s.locale, {
          dateStyle: s.dateStyle,
          timeZone: "UTC",
        }).format(d);
      } catch (e) {
        return new Intl.DateTimeFormat(undefined, {
          dateStyle: s.dateStyle,
          timeZone: "UTC",
        }).format(d);
      }
    };
    var minStr = fmt(est.min);
    var maxStr = fmt(est.max);
    var dateStr = est.single ? minStr : minStr + " – " + maxStr;
    return s.template
      .replace(/\{date\}/g, dateStr)
      .replace(/\{min\}/g, minStr)
      .replace(/\{max\}/g, maxStr);
  }

  function hasActiveSubscription(url) {
    if (!entitlementRequest) {
      if (typeof fetch !== "function") return Promise.resolve(false);
      entitlementRequest = fetch(url, {
        credentials: "same-origin",
        headers: { Accept: "application/json" },
      })
        .then(function (response) {
          if (!response.ok) return { active: false };
          return response.json();
        })
        .then(function (body) {
          return Boolean(body && body.active === true);
        })
        .catch(function () {
          return false;
        });
    }
    return entitlementRequest;
  }

  function render(block) {
    var configEl = block.querySelector("[data-edd-config]");
    var output = block.querySelector("[data-edd-output]");
    if (!configEl || !output) return;

    var settings;
    try {
      settings = JSON.parse(configEl.textContent);
    } catch (e) {
      block.style.display = "none";
      return;
    }

    // Legacy saved metafields did not contain a timezone. Never present UTC as
    // the merchant's zone: keep the block hidden until the merchant opens the
    // app and saves once, which writes Shopify's authoritative IANA timezone.
    if (!settings || !isValidTimeZone(settings.timeZone)) return;

    var s = normalize(settings);
    if (!s.enabled) {
      return;
    }

    var entitlementUrl = block.getAttribute("data-edd-entitlement-url");
    if (!entitlementUrl) return;

    hasActiveSubscription(entitlementUrl).then(function (active) {
      if (!active) return;
      output.textContent = format(estimate(new Date(), s), s);
      block.hidden = false;
    });
  }

  function init() {
    var blocks = document.querySelectorAll("[data-edd]");
    for (var i = 0; i < blocks.length; i++) render(blocks[i]);
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
  } else {
    init();
  }
})();
