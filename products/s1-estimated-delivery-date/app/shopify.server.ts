import "@shopify/shopify-app-react-router/adapters/node";
import {
  ApiVersion,
  AppDistribution,
  LogSeverity,
  shopifyApp,
} from "@shopify/shopify-app-react-router/server";
import { PrismaSessionStorage } from "@shopify/shopify-app-session-storage-prisma";
import prisma from "./db.server";

// Pricing is handled by Shopify App Pricing (formerly Managed Pricing). This
// app defines no Billing API plans or charges; the app root verifies the
// current subscription through Shopify's Partner API.

const shopify = shopifyApp({
  apiKey: process.env.SHOPIFY_API_KEY,
  apiSecretKey: process.env.SHOPIFY_API_SECRET || "",
  apiVersion: ApiVersion.October25,
  // The HMAC-authenticated storefront entitlement endpoint requires only the
  // app-proxy scope. Settings remain app-owned ($app) metafields and no order
  // or customer-record scopes are requested. Keep this exactly aligned with
  // `access_scopes` in shopify.app.toml so fresh installs do not OAuth-loop.
  scopes: ["write_app_proxy"],
  appUrl: process.env.SHOPIFY_APP_URL || "",
  authPathPrefix: "/auth",
  sessionStorage: new PrismaSessionStorage(prisma),
  distribution: AppDistribution.AppStore,
  // Keep SDK request logging off so authenticated Shopify URLs cannot be
  // exposed if a future environment or dependency changes logging defaults.
  logger: {
    level: LogSeverity.Info,
    httpRequests: false,
  },
  future: {
    expiringOfflineAccessTokens: true,
  },
  ...(process.env.SHOP_CUSTOM_DOMAIN
    ? { customShopDomains: [process.env.SHOP_CUSTOM_DOMAIN] }
    : {}),
});

export default shopify;
export const apiVersion = ApiVersion.October25;
export const addDocumentResponseHeaders = shopify.addDocumentResponseHeaders;
export const authenticate = shopify.authenticate;
export const unauthenticated = shopify.unauthenticated;
export const login = shopify.login;
export const registerWebhooks = shopify.registerWebhooks;
export const sessionStorage = shopify.sessionStorage;
