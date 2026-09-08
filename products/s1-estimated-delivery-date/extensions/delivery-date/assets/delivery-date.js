/**
 * Storefront renderer for the Estimated Delivery Date block.
 *
 * This is a verbatim port of app/lib/delivery-date.ts. Keep the two in sync —
 * the TS version has unit tests (app/lib/delivery-date.test.ts) that lock the
 * behaviour asserted here.
 */
(function () {
  "use strict";

  var REFRESH_MS = 15000;
  var REQUEST_TIMEOUT_MS = 5000;
  var MAX_ENTITLEMENT_MS = 60000;
  var entitlementUntil = 0;
  var entitlementUrl;
  var refreshTimer;
  var expiryTimer;
  var requestTimer;
  var requestController;
  var requestPending = false;
  var requestGeneration = 0;
  var suspended = false;

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
      processingDays: clampInt(
        s.processingDays,
        0,
        365,
        DEFAULTS.processingDays,
      ),
      cutoffHour: clampInt(s.cutoffHour, 0, 23, DEFAULTS.cutoffHour),
      transitDaysMin: Math.min(min, max),
      transitDaysMax: Math.max(min, max),
      workingDays: workingDays,
      template:
        typeof s.template === "string" && s.template.trim()
          ? s.template
          : DEFAULTS.template,
      dateStyle:
        validStyles.indexOf(s.dateStyle) !== -1
          ? s.dateStyle
          : DEFAULTS.dateStyle,
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

  function clock() {
    return performance.now();
  }

  function revokeEntitlement() {
    entitlementUntil = 0;
    clearTimeout(expiryTimer);
    var blocks = document.querySelectorAll("[data-edd]");
    for (var i = 0; i < blocks.length; i++) blocks[i].hidden = true;
  }

  function finishRequest(generation, requestedAt, body) {
    // A late response from a timeout or a suspended page must never resurrect
    // an expired grant or overwrite a newer decision.
    if (generation !== requestGeneration) return;
    requestGeneration++;
    requestPending = false;
    clearTimeout(requestTimer);
    var validForMs = body && body.validForMs;
    var until = requestedAt + validForMs;
    if (
      !suspended &&
      !document.hidden &&
      body &&
      body.active === true &&
      typeof validForMs === "number" &&
      isFinite(validForMs) &&
      validForMs > 0 &&
      validForMs <= MAX_ENTITLEMENT_MS &&
      until > clock()
    ) {
      entitlementUntil = until;
      clearTimeout(expiryTimer);
      expiryTimer = setTimeout(revokeEntitlement, until - clock());
    } else {
      revokeEntitlement();
    }
    renderAll();
    if (!suspended && !document.hidden) {
      // Share one bounded request across every block. The independent expiry
      // timer hides them even if this refresh hangs or the network is offline.
      var remaining = entitlementUntil - clock();
      refreshTimer = setTimeout(
        refreshEntitlement,
        remaining > 0
          ? Math.max(1000, Math.min(REFRESH_MS, remaining))
          : REFRESH_MS,
      );
    }
  }

  function refreshEntitlement() {
    clearTimeout(refreshTimer);
    if (suspended || document.hidden) return;
    var url = renderAll();
    if (!url || requestPending || typeof fetch !== "function") return;
    if (url !== entitlementUrl) revokeEntitlement();
    entitlementUrl = url;
    requestPending = true;
    var generation = ++requestGeneration;
    var requestedAt = clock();
    var controller = new AbortController();
    requestController = controller;
    requestTimer = setTimeout(function () {
      controller.abort();
      finishRequest(generation, requestedAt, null);
    }, REQUEST_TIMEOUT_MS);
    // Count the entire round trip against the server's remaining lifetime.
    // Cached responses therefore cannot reset the browser's expiry clock.
    Promise.resolve()
      .then(function () {
        if (generation !== requestGeneration) return { ok: false };
        return fetch(url, {
          credentials: "same-origin",
          cache: "no-store",
          signal: controller.signal,
          headers: { Accept: "application/json" },
        });
      })
      .then(function (response) {
        if (!response.ok) return null;
        return response.json();
      })
      .then(function (body) {
        finishRequest(generation, requestedAt, body);
      })
      .catch(function () {
        finishRequest(generation, requestedAt, null);
      });
  }

  function render(block) {
    block.hidden = true;
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

    var url = block.getAttribute("data-edd-entitlement-url");
    if (!url) return;
    if (
      !suspended &&
      !document.hidden &&
      url === entitlementUrl &&
      entitlementUntil > clock()
    ) {
      output.textContent = format(estimate(new Date(), s), s);
      block.hidden = false;
    }
    return url;
  }

  function renderAll() {
    var blocks = document.querySelectorAll("[data-edd]");
    var url;
    for (var i = 0; i < blocks.length; i++) url = render(blocks[i]) || url;
    return url;
  }

  function suspend() {
    suspended = true;
    requestGeneration++;
    requestPending = false;
    clearTimeout(refreshTimer);
    clearTimeout(requestTimer);
    if (requestController) requestController.abort();
    revokeEntitlement();
  }

  function resume() {
    suspend();
    suspended = false;
    refreshEntitlement();
  }

  document.addEventListener("visibilitychange", function () {
    if (document.hidden) suspend();
    else resume();
  });
  window.addEventListener("pagehide", suspend);
  window.addEventListener("pageshow", function (event) {
    if (event.persisted) resume();
  });
  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", refreshEntitlement);
  } else {
    refreshEntitlement();
  }
})();
