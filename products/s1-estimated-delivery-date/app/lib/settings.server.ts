/**
 * Reads/writes the app's delivery settings as an APP-OWNED shop metafield.
 *
 * App-owned metafields use the reserved "$app" namespace, require NO access
 * scope, and are automatically readable by this same app's theme app extension
 * in Liquid via `shop.metafields.app.settings`. The theme extension also falls
 * back to its own block settings if the metafield is missing, so the storefront
 * is never broken.
 */
import type { AdminApiContext } from "@shopify/shopify-app-react-router/server";
import {
  DEFAULT_SETTINGS,
  normalizeSettings,
  type DeliverySettings,
} from "./delivery-date";

// "$app" is the app's reserved metafield namespace (app-owned, no scope needed).
export const METAFIELD_NAMESPACE = "$app";
export const METAFIELD_KEY = "settings";

type Admin = AdminApiContext["graphql"];

async function getShopGid(graphql: Admin): Promise<string> {
  const res = await graphql(`#graphql
    query ShopId { shop { id } }
  `);
  const body = await res.json();
  return body.data!.shop!.id as string;
}

/** Read persisted settings, or defaults when none exist yet. */
export async function readSettings(graphql: Admin): Promise<DeliverySettings> {
  const res = await graphql(
    `#graphql
      query DeliverySettings($namespace: String!, $key: String!) {
        shop {
          metafield(namespace: $namespace, key: $key) {
            value
          }
        }
      }`,
    { variables: { namespace: METAFIELD_NAMESPACE, key: METAFIELD_KEY } },
  );
  const body = await res.json();
  const raw = body.data?.shop?.metafield?.value;
  if (!raw) return { ...DEFAULT_SETTINGS };
  try {
    return normalizeSettings(JSON.parse(raw));
  } catch {
    return { ...DEFAULT_SETTINGS };
  }
}

/** Persist settings to the app-owned shop metafield (normalized first). */
export async function writeSettings(
  graphql: Admin,
  input: Partial<DeliverySettings>,
): Promise<DeliverySettings> {
  const settings = normalizeSettings(input);
  const shopId = await getShopGid(graphql);

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
            ownerId: shopId,
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
  const errors = body.data?.metafieldsSet?.userErrors ?? [];
  if (errors.length) {
    throw new Error(`metafieldsSet failed: ${JSON.stringify(errors)}`);
  }
  return settings;
}
