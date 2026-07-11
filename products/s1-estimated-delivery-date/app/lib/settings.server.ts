/**
 * Reads/writes the app's delivery settings as a shop metafield so the theme
 * app extension can read them in Liquid via `shop.metafields.delivery_date.settings`.
 *
 * A metafield *definition* with storefront read access is ensured on first
 * write — that is what makes the value visible to the Online Store (Liquid).
 * This path needs to be verified on a real store (see products README); the
 * theme extension falls back to its own block settings if the metafield is
 * missing, so the storefront is never broken while this is being wired.
 */
import type { AdminApiContext } from "@shopify/shopify-app-react-router/server";
import {
  DEFAULT_SETTINGS,
  normalizeSettings,
  type DeliverySettings,
} from "./delivery-date";

export const METAFIELD_NAMESPACE = "delivery_date";
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

/**
 * Ensure a shop metafield definition exists with storefront read access.
 * Idempotent — a "definition already exists" error (TAKEN) is treated as success.
 */
async function ensureDefinition(graphql: Admin): Promise<void> {
  const res = await graphql(
    `#graphql
      mutation EnsureDeliveryDefinition($definition: MetafieldDefinitionInput!) {
        metafieldDefinitionCreate(definition: $definition) {
          createdDefinition { id }
          userErrors { code field message }
        }
      }`,
    {
      variables: {
        definition: {
          name: "Delivery date settings",
          namespace: METAFIELD_NAMESPACE,
          key: METAFIELD_KEY,
          description: "Estimated Delivery Date app configuration (JSON).",
          type: "json",
          ownerType: "SHOP",
          access: { storefront: "PUBLIC_READ" },
        },
      },
    },
  );
  const body = await res.json();
  const errors =
    body.data?.metafieldDefinitionCreate?.userErrors ?? [];
  // TAKEN = the definition already exists, which is the normal steady state.
  const fatal = errors.filter((e: { code?: string }) => e.code !== "TAKEN");
  if (fatal.length) {
    throw new Error(
      `metafieldDefinitionCreate failed: ${JSON.stringify(fatal)}`,
    );
  }
}

/** Persist settings to the shop metafield (normalized first). */
export async function writeSettings(
  graphql: Admin,
  input: Partial<DeliverySettings>,
): Promise<DeliverySettings> {
  const settings = normalizeSettings(input);
  await ensureDefinition(graphql);
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
