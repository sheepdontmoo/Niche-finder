import { useEffect } from "react";
import type {
  ActionFunctionArgs,
  HeadersFunction,
  LoaderFunctionArgs,
} from "react-router";
import { useFetcher, useLoaderData } from "react-router";
import { useAppBridge } from "@shopify/app-bridge-react";
import { boundary } from "@shopify/shopify-app-react-router/server";
import { authenticate, PRO_PLAN } from "../shopify.server";
import { readSettings, writeSettings } from "../lib/settings.server";
import {
  estimateDelivery,
  formatEstimate,
  normalizeSettings,
  type DeliverySettings,
} from "../lib/delivery-date";

const WEEKDAYS = [
  { value: 1, label: "Mon" },
  { value: 2, label: "Tue" },
  { value: 3, label: "Wed" },
  { value: 4, label: "Thu" },
  { value: 5, label: "Fri" },
  { value: 6, label: "Sat" },
  { value: 0, label: "Sun" },
];

// Charge real money only in production; dev/test stores use Shopify test billing.
const isTestBilling = process.env.NODE_ENV !== "production";

export const loader = async ({ request }: LoaderFunctionArgs) => {
  const { admin, billing } = await authenticate.admin(request);

  const [settings, billingCheck] = await Promise.all([
    readSettings(admin.graphql),
    billing.check({ plans: [PRO_PLAN], isTest: isTestBilling }),
  ]);

  const estimate = estimateDelivery(new Date(), settings);
  return {
    settings,
    hasActivePayment: billingCheck.hasActivePayment,
    preview: formatEstimate(estimate, settings),
  };
};

export const action = async ({ request }: ActionFunctionArgs) => {
  const { admin, billing } = await authenticate.admin(request);
  const form = await request.formData();

  if (form.get("intent") === "subscribe") {
    // Redirects to Shopify's subscription confirmation page.
    return await billing.request({
      plan: PRO_PLAN,
      isTest: isTestBilling,
      returnUrl: `${process.env.SHOPIFY_APP_URL}/app`,
    });
  }

  const parsed: Partial<DeliverySettings> = {
    enabled: form.get("enabled") === "on",
    processingDays: Number(form.get("processingDays")),
    cutoffHour: Number(form.get("cutoffHour")),
    transitDaysMin: Number(form.get("transitDaysMin")),
    transitDaysMax: Number(form.get("transitDaysMax")),
    workingDays: form.getAll("workingDays").map((d) => Number(d)),
    template: String(form.get("template") ?? ""),
    locale: String(form.get("locale") ?? ""),
    dateStyle: String(
      form.get("dateStyle") ?? "medium",
    ) as DeliverySettings["dateStyle"],
  };

  const saved = await writeSettings(admin.graphql, parsed);
  const estimate = estimateDelivery(new Date(), saved);
  return {
    ok: true,
    settings: saved,
    preview: formatEstimate(estimate, saved),
  };
};

export default function Index() {
  const loaded = useLoaderData<typeof loader>();
  const fetcher = useFetcher<typeof action>();
  const shopify = useAppBridge();
  const result = fetcher.data;

  useEffect(() => {
    if (result?.ok) shopify.toast.show("Delivery settings saved");
  }, [result?.ok, shopify]);

  // After a save the action echoes fresh settings/preview; otherwise use loader.
  const settings = normalizeSettings(result?.settings ?? loaded.settings);
  const preview = result?.preview ?? loaded.preview;
  const saving = fetcher.state !== "idle";

  return (
    <s-page heading="Estimated Delivery Date">
      {!loaded.hasActivePayment && (
        <s-banner tone="warning" heading="Start your subscription">
          <s-paragraph>
            You&apos;re on a free trial. Subscribe to keep the delivery estimate
            live on your storefront after it ends.
          </s-paragraph>
          <fetcher.Form method="post">
            <input type="hidden" name="intent" value="subscribe" />
            <s-button variant="primary" type="submit">
              Subscribe — $6.99/month
            </s-button>
          </fetcher.Form>
        </s-banner>
      )}

      <s-section heading="How it looks">
        <s-paragraph>Preview for an order placed right now:</s-paragraph>
        <s-box
          padding="base"
          borderWidth="base"
          borderRadius="base"
          background="subdued"
        >
          <s-text>{preview}</s-text>
        </s-box>
      </s-section>

      <fetcher.Form method="post">
        <s-section heading="Delivery rules">
          <s-stack direction="block" gap="base">
            <s-checkbox
              name="enabled"
              label="Show the delivery estimate on my storefront"
              {...(settings.enabled ? { checked: true } : {})}
            />
            <s-number-field
              name="processingDays"
              label="Processing time (business days)"
              min={0}
              max={60}
              step={1}
              value={String(settings.processingDays)}
            />
            <s-number-field
              name="cutoffHour"
              label="Daily order cutoff hour (0–23, shop time)"
              min={0}
              max={23}
              step={1}
              value={String(settings.cutoffHour)}
            />
            <s-stack direction="inline" gap="base">
              <s-number-field
                name="transitDaysMin"
                label="Shipping time — min (business days)"
                min={0}
                max={60}
                step={1}
                value={String(settings.transitDaysMin)}
              />
              <s-number-field
                name="transitDaysMax"
                label="Shipping time — max (business days)"
                min={0}
                max={60}
                step={1}
                value={String(settings.transitDaysMax)}
              />
            </s-stack>
          </s-stack>
        </s-section>

        <s-section heading="Working days">
          <s-paragraph>Days your business ships orders.</s-paragraph>
          <s-stack direction="inline" gap="base">
            {WEEKDAYS.map((d) => (
              <s-checkbox
                key={d.value}
                name="workingDays"
                value={String(d.value)}
                label={d.label}
                {...(settings.workingDays.includes(d.value)
                  ? { checked: true }
                  : {})}
              />
            ))}
          </s-stack>
        </s-section>

        <s-section heading="Wording and format">
          <s-stack direction="block" gap="base">
            <s-text-field
              name="template"
              label="Message template"
              details="Use {date} for the estimate, or {min} and {max} for a range."
              value={settings.template}
            />
            <s-text-field
              name="locale"
              label="Locale (e.g. en-IE, en-US, de-DE)"
              value={settings.locale}
            />
            <s-select name="dateStyle" label="Date format" value={settings.dateStyle}>
              <s-option value="full">Full (Monday, 5 January 2026)</s-option>
              <s-option value="long">Long (5 January 2026)</s-option>
              <s-option value="medium">Medium (5 Jan 2026)</s-option>
              <s-option value="short">Short (05/01/2026)</s-option>
            </s-select>
          </s-stack>
        </s-section>

        <s-section>
          <s-button
            variant="primary"
            type="submit"
            {...(saving ? { loading: true } : {})}
          >
            Save settings
          </s-button>
        </s-section>
      </fetcher.Form>
    </s-page>
  );
}

export const headers: HeadersFunction = (headersArgs) => {
  return boundary.headers(headersArgs);
};
