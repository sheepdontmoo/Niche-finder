import type { HeadersFunction, LoaderFunctionArgs } from "react-router";
import { Outlet, useLoaderData, useRouteError } from "react-router";
import { boundary } from "@shopify/shopify-app-react-router/server";
import { AppProvider } from "@shopify/shopify-app-react-router/react";

import { authenticate } from "../shopify.server";
import {
  requireActiveAppPayment,
} from "../lib/billing";
import {
  getAuthenticatedShopId,
  hasActivePartnerSubscription,
  hasActivePartnerSubscriptionAfterPlanSelection,
  readPartnerBillingConfig,
  rememberPartnerSubscription,
} from "../lib/partner-subscription.server";

export const loader = async ({ request }: LoaderFunctionArgs) => {
  const { admin, redirect, session } = await authenticate.admin(request);
  const billingConfig = readPartnerBillingConfig(process.env);
  const shopId = await getAuthenticatedShopId(admin.graphql);
  const returnedFromPlanSelection = new URL(request.url).searchParams.has(
    "plan_handle",
  );
  const hasActivePayment = returnedFromPlanSelection
    ? await hasActivePartnerSubscriptionAfterPlanSelection(
        billingConfig,
        shopId,
      )
    : await hasActivePartnerSubscription(billingConfig, shopId);
  rememberPartnerSubscription(billingConfig, shopId, hasActivePayment);
  const paymentRedirect = await requireActiveAppPayment(
    hasActivePayment,
    redirect,
    session.shop,
    billingConfig.appHandle,
  );
  if (paymentRedirect) return paymentRedirect;

  // eslint-disable-next-line no-undef
  return { apiKey: process.env.SHOPIFY_API_KEY || "" };
};

export default function App() {
  const { apiKey } = useLoaderData<typeof loader>();

  return (
    <AppProvider embedded apiKey={apiKey}>
      <s-app-nav>
        <s-link href="/app">Settings</s-link>
        <s-link href="/app/setup">Setup guide</s-link>
      </s-app-nav>
      <Outlet />
    </AppProvider>
  );
}

// Shopify needs React Router to catch some thrown responses, so that their headers are included in the response.
export function ErrorBoundary() {
  return boundary.error(useRouteError());
}

export const headers: HeadersFunction = (headersArgs) => {
  return boundary.headers(headersArgs);
};
