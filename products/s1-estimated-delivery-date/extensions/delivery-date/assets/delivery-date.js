/**
 * Storefront renderer for the Estimated Delivery Date block.
 *
 * This is a verbatim port of app/lib/delivery-date.ts. Keep the two in sync —
 * the TS version has unit tests (app/lib/delivery-date.test.ts) that lock the
 * behaviour asserted here.
 */
(function () {
  "use strict";

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
  };

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
    };
  }

  function isWorkingDay(date, workingDays) {
    return workingDays.indexOf(date.getDay()) !== -1;
  }

  function toWorkingDay(date, workingDays) {
    var d = new Date(date);
    if (!workingDays.length) return d;
    var guard = 0;
    while (!isWorkingDay(d, workingDays) && guard < 14) {
      d.setDate(d.getDate() + 1);
      guard++;
    }
    return d;
  }

  function addBusinessDays(date, days, workingDays) {
    var d = toWorkingDay(date, workingDays);
    var remaining = Math.max(0, days);
    var guard = 0;
    while (remaining > 0 && guard < 3650) {
      d.setDate(d.getDate() + 1);
      if (isWorkingDay(d, workingDays)) remaining--;
      guard++;
    }
    return d;
  }

  function estimate(now, settings) {
    var s = normalize(settings);
    var start = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    if (now.getHours() >= s.cutoffHour) start.setDate(start.getDate() + 1);
    var ship = addBusinessDays(start, s.processingDays, s.workingDays);
    var min = addBusinessDays(ship, s.transitDaysMin, s.workingDays);
    var max = addBusinessDays(ship, s.transitDaysMax, s.workingDays);
    return { min: min, max: max, single: min.getTime() === max.getTime() };
  }

  function format(est, settings) {
    var s = normalize(settings);
    var fmt = function (d) {
      try {
        return new Intl.DateTimeFormat(s.locale, { dateStyle: s.dateStyle }).format(d);
      } catch (e) {
        return new Intl.DateTimeFormat(undefined, { dateStyle: s.dateStyle }).format(d);
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

    var s = normalize(settings);
    if (!s.enabled) {
      block.style.display = "none";
      return;
    }

    output.textContent = format(estimate(new Date(), s), s);
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
