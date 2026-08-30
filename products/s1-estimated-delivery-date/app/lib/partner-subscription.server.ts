import type { AdminApiContext } from "@shopify/shopify-app-react-router/server";
import { requireShopifyAppHandle } from "./billing.ts";

export const PARTNER_API_VERSION = "2026-07";

export interface PartnerBillingConfig {
  organizationId: string;
  accessToken: string;
  appId: string;
  appHandle: string;
}

type Environment = Record<string, string | undefined>;
type AdminGraphql = AdminApiContext["graphql"];
type Fetcher = typeof fetch;
type Sleeper = (milliseconds: number) => Promise<void>;

const STOREFRONT_CACHE_MS = 60_000;
export const POST_APPROVAL_RECHECK_MS = 400;
export const POST_APPROVAL_RECHECK_COOLDOWN_MS = 5_000;
export const PARTNER_API_TIMEOUT_MS = 5_000;
const subscriptionCache = new Map<
  string,
  { active: boolean; expiresAt: number }
>();
const subscriptionRequests = new Map<string, Promise<boolean>>();
const subscriptionCacheRevisions = new Map<string, number>();
const postApprovalRechecks = new Map<string, number>();

const sleep: Sleeper = (milliseconds) =>
  new Promise((resolve) => setTimeout(resolve, milliseconds));

function requireValue(value: string | undefined, name: string): string {
  const normalized = value?.trim();
  if (!normalized) throw new Error(`${name} is required`);
  return normalized;
}

function hasGraphQLErrors(body: unknown): boolean {
  if (!body || typeof body !== "object") return false;
  const errors = (body as { errors?: unknown }).errors;
  return Array.isArray(errors) && errors.length > 0;
}

export function readPartnerBillingConfig(env: Environment): PartnerBillingConfig {
  const organizationId = requireValue(
    env.SHOPIFY_PARTNER_ORG_ID,
    "SHOPIFY_PARTNER_ORG_ID",
  );
  if (!/^\d+$/.test(organizationId)) {
    throw new Error("SHOPIFY_PARTNER_ORG_ID is invalid");
  }

  const appId = requireValue(env.SHOPIFY_APP_GID, "SHOPIFY_APP_GID");
  if (!/^gid:\/\/shopify\/App\/\d+$/.test(appId)) {
    throw new Error("SHOPIFY_APP_GID is invalid");
  }

  return {
    organizationId,
    accessToken: requireValue(
      env.SHOPIFY_PARTNER_API_ACCESS_TOKEN,
      "SHOPIFY_PARTNER_API_ACCESS_TOKEN",
    ),
    appId,
    appHandle: requireShopifyAppHandle(env.SHOPIFY_APP_HANDLE),
  };
}

export async function getAuthenticatedShopId(
  graphql: AdminGraphql,
): Promise<string> {
  const response = await graphql(`#graphql
    query BillingShopId {
      shop { id }
    }
  `);
  const body = await response.json();
  const shopId = body.data?.shop?.id;
  if (
    hasGraphQLErrors(body) ||
    typeof shopId !== "string" ||
    !/^gid:\/\/shopify\/Shop\/[^/\s]+$/.test(shopId)
  ) {
    throw new Error("Shopify did not return the authenticated shop ID");
  }
  return shopId;
}

export async function hasActivePartnerSubscription(
  config: PartnerBillingConfig,
  shopId: string,
  fetcher: Fetcher = fetch,
): Promise<boolean> {
  const endpoint = `https://partners.shopify.com/${config.organizationId}/api/${PARTNER_API_VERSION}/graphql.json`;
  const response = await fetcher(endpoint, {
    method: "POST",
    signal: AbortSignal.timeout(PARTNER_API_TIMEOUT_MS),
    headers: {
      "Content-Type": "application/json",
      "X-Shopify-Access-Token": config.accessToken,
    },
    body: JSON.stringify({
      query: `query ActiveSubscription($appId: ID!, $shopId: ID!) {
        activeSubscription(appId: $appId, shopId: $shopId) {
          billingPeriod
          trialEndsAt
        }
      }`,
      variables: { appId: config.appId, shopId },
    }),
  });

  if (!response.ok) {
    throw new Error(
      `Shopify Partner API subscription check failed (${response.status})`,
    );
  }

  const body = (await response.json()) as unknown;
  if (hasGraphQLErrors(body) || !body || typeof body !== "object") {
    throw new Error("Shopify Partner API subscription check failed");
  }
  const data = (body as { data?: { activeSubscription?: unknown } }).data;
  if (!data || !("activeSubscription" in data)) {
    throw new Error("Shopify Partner API subscription response is invalid");
  }
  if (data.activeSubscription === null) return false;
  if (
    typeof data.activeSubscription === "object" &&
    !Array.isArray(data.activeSubscription) &&
    typeof (data.activeSubscription as { billingPeriod?: unknown })
      .billingPeriod === "string"
  ) {
    return true;
  }
  throw new Error("Shopify Partner API subscription response is invalid");
}

/**
 * Shopify redirects a merchant back with `plan_handle` after plan selection.
 * That parameter is only a signal to tolerate a short provider-consistency
 * window: access is still granted solely after Partner API returns an active
 * contract. Ordinary unpaid and storefront requests remain one API call.
 */
export async function hasActivePartnerSubscriptionAfterPlanSelection(
  config: PartnerBillingConfig,
  shopId: string,
  fetcher: Fetcher = fetch,
  sleeper: Sleeper = sleep,
  now = Date.now(),
): Promise<boolean> {
  if (await hasActivePartnerSubscription(config, shopId, fetcher)) return true;
  const key = subscriptionCacheKey(config, shopId);
  const previousRecheck = postApprovalRechecks.get(key);
  if (
    previousRecheck !== undefined &&
    now - previousRecheck < POST_APPROVAL_RECHECK_COOLDOWN_MS
  ) {
    return false;
  }
  postApprovalRechecks.set(key, now);
  await sleeper(POST_APPROVAL_RECHECK_MS);
  return hasActivePartnerSubscription(config, shopId, fetcher);
}

function subscriptionCacheKey(
  config: PartnerBillingConfig,
  shopId: string,
): string {
  return `${config.appId}:${shopId}`;
}

/**
 * Prime the short storefront entitlement cache after an authoritative admin
 * check. This prevents a just-approved plan from waiting for an older negative
 * storefront result to expire.
 */
export function rememberPartnerSubscription(
  config: PartnerBillingConfig,
  shopId: string,
  active: boolean,
  now = Date.now(),
): void {
  const key = subscriptionCacheKey(config, shopId);
  subscriptionCacheRevisions.set(
    key,
    (subscriptionCacheRevisions.get(key) ?? 0) + 1,
  );
  subscriptionCache.set(key, {
    active,
    expiresAt: now + STOREFRONT_CACHE_MS,
  });
}

/**
 * Storefronts can generate many page views, while the Partner API permits only
 * four requests per second per client. Deduplicate concurrent checks and cache
 * both active and inactive results for at most one minute. Provider failures
 * are never cached and still fail closed.
 */
export async function hasCachedActivePartnerSubscription(
  config: PartnerBillingConfig,
  shopId: string,
  fetcher: Fetcher = fetch,
  now = Date.now(),
  completionClock: () => number = Date.now,
): Promise<boolean> {
  const key = subscriptionCacheKey(config, shopId);
  const cached = subscriptionCache.get(key);
  if (cached && cached.expiresAt > now) return cached.active;

  const pending = subscriptionRequests.get(key);
  if (pending) return pending;

  const revisionAtStart = subscriptionCacheRevisions.get(key) ?? 0;
  const request = hasActivePartnerSubscription(config, shopId, fetcher)
    .then((active) => {
      // An authenticated admin check may have completed while this storefront
      // request was in flight. Never let the older response replace that newer
      // authoritative decision, especially after cancellation.
      if ((subscriptionCacheRevisions.get(key) ?? 0) !== revisionAtStart) {
        const newer = subscriptionCache.get(key);
        return newer && newer.expiresAt > completionClock()
          ? newer.active
          : false;
      }
      rememberPartnerSubscription(config, shopId, active, now);
      return active;
    })
    .finally(() => subscriptionRequests.delete(key));
  subscriptionRequests.set(key, request);
  return request;
}

export function clearPartnerSubscriptionCacheForTests(): void {
  subscriptionCache.clear();
  subscriptionRequests.clear();
  subscriptionCacheRevisions.clear();
  postApprovalRechecks.clear();
}
