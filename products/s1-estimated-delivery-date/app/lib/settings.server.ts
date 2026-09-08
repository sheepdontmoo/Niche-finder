/**
 * Reads/writes the app's delivery settings as an APP-OWNED shop metafield.
 *
 * App-owned metafields use the reserved "$app" namespace, require NO access
 * scope, and are automatically readable by this same app's theme app extension
 * in Liquid via `shop.metafields["$app"].settings`. The theme extension stays
 * hidden when settings or a valid Shopify IANA timezone are missing.
 */
import type { AdminApiContext } from "@shopify/shopify-app-react-router/server";
import {
  DEFAULT_SETTINGS,
  normalizeSettings,
  type DeliverySettings,
} from "./delivery-date.ts";

// "$app" is the app's reserved metafield namespace (app-owned, no scope needed).
export const METAFIELD_NAMESPACE = "$app";
export const METAFIELD_KEY = "settings";

type Admin = AdminApiContext["graphql"];

function hasGraphQLErrors(body: unknown): boolean {
  if (!body || typeof body !== "object") return false;
  const errors = (body as { errors?: unknown }).errors;
  return Array.isArray(errors) && errors.length > 0;
}

function requireShopTimeZone(value: unknown): string {
  if (typeof value !== "string" || !value.trim()) {
    throw new Error("Shopify did not return the shop IANA timezone");
  }
  const timeZone = value.trim();
  try {
    new Intl.DateTimeFormat("en", { timeZone }).format(new Date(0));
    return timeZone;
  } catch {
    throw new Error("Shopify returned an invalid shop IANA timezone");
  }
}

async function getShopContext(
  graphql: Admin,
): Promise<{ id: string; timeZone: string }> {
  const res = await graphql(`#graphql
    query DeliverySettingsShopContext {
      shop {
        id
        ianaTimezone
      }
    }
  `);
  const body = await res.json();
  const shop = body.data?.shop;
  if (
    hasGraphQLErrors(body) ||
    typeof shop?.id !== "string" ||
    !/^gid:\/\/shopify\/Shop\/\d+$/.test(shop.id)
  ) {
    throw new Error("Shopify did not return the shop ID and IANA timezone");
  }
  return {
    id: shop.id,
    timeZone: requireShopTimeZone(shop.ianaTimezone),
  };
}

/** Read persisted settings, or defaults when none exist yet. */
export async function readSettings(graphql: Admin): Promise<DeliverySettings> {
  const res = await graphql(
    `#graphql
      query DeliverySettings($namespace: String!, $key: String!) {
        shop {
          ianaTimezone
          metafield(namespace: $namespace, key: $key) {
            value
          }
        }
      }`,
    { variables: { namespace: METAFIELD_NAMESPACE, key: METAFIELD_KEY } },
  );
  const body = await res.json();
  if (hasGraphQLErrors(body)) {
    throw new Error("Shopify shop settings query failed");
  }
  const shopTimeZone = requireShopTimeZone(body.data?.shop?.ianaTimezone);
  const fallback = normalizeSettings({
    ...DEFAULT_SETTINGS,
    timeZone: shopTimeZone,
  });
  const raw = body.data?.shop?.metafield?.value;
  if (!raw) return fallback;
  try {
    const stored = JSON.parse(raw) as Partial<DeliverySettings>;
    return normalizeSettings({
      ...stored,
      // Always use Shopify's current value. This repairs legacy, invalid, or
      // stale stored timezones in the admin and on the merchant's next save.
      timeZone: shopTimeZone,
    });
  } catch {
    return fallback;
  }
}

/** Persist settings to the app-owned shop metafield (normalized first). */
export async function writeSettings(
  graphql: Admin,
  input: Partial<DeliverySettings>,
): Promise<DeliverySettings> {
  const shop = await getShopContext(graphql);
  const settings = normalizeSettings({ ...input, timeZone: shop.timeZone });

  const res = await graphql(
    `#graphql
      mutation SetDeliverySettings($metafields: [MetafieldsSetInput!]!) {
        metafieldsSet(metafields: $metafields) {
          metafields { id }
          userErrors { field message }
        }
      }`,
    {
      variables: {
        metafields: [
          {
            ownerId: shop.id,
            namespace: METAFIELD_NAMESPACE,
            key: METAFIELD_KEY,
            type: "json",
            value: JSON.stringify(settings),
          },
        ],
      },
    },
  );
  const body = await res.json();
  if (hasGraphQLErrors(body)) {
    throw new Error("Shopify settings mutation failed");
  }
  const result = body.data?.metafieldsSet;
  if (!result || !Array.isArray(result.userErrors)) {
    throw new Error("Shopify settings mutation response is invalid");
  }
  const errors = result.userErrors;
  if (errors.length) {
    throw new Error(`metafieldsSet failed: ${JSON.stringify(errors)}`);
  }
  if (
    !Array.isArray(result.metafields) ||
    typeof result.metafields[0]?.id !== "string"
  ) {
    throw new Error("Shopify did not confirm the saved delivery settings");
  }
  return settings;
}
