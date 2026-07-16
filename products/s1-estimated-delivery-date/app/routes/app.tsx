import type { HeadersFunction, LoaderFunctionArgs } from "react-router";
import { Outlet, useLoaderData, useRouteError } from "react-router";
import { boundary } from "@shopify/shopify-app-react-router/server";
import { AppProvider } from "@shopify/shopify-app-react-router/react";

import { authenticate, PRO_PLAN } from "../shopify.server";

export const loader = async ({ request }: LoaderFunctionArgs) => {
  const { admin, billing } = await authenticate.admin(request);

  // Development stores (including Shopify's App Store reviewers) can ONLY
  // accept test charges — creating a real charge there throws, which surfaces
  // as "Application Error". Detect the store type at runtime: test charge for
  // dev stores, real charge for real merchants.
  const planRes = await admin.graphql(`#graphql
    query ShopPlan { shop { plan { partnerDevelopment } } }`);
  const planBody = await planRes.json();
  const isDevStore = Boolean(planBody.data?.shop?.plan?.partnerDevelopment);

  // Enforce billing in the loader so the payment-confirmation redirect happens
  // at the top window via App Bridge (the correct, App Store-compliant flow).
  // Unpaid merchants are sent to Shopify's confirmation page (7-day trial);
  // once they approve, the subscription is active and the app loads.
  await billing.require({
    plans: [PRO_PLAN],
    // Accept any active subscription, test or real.
    isTest: true,
    onFailure: async () =>
      billing.request({
        plan: PRO_PLAN,
        isTest: isDevStore,
        // eslint-disable-next-line no-undef
        returnUrl: `${process.env.SHOPIFY_APP_URL}/app`,
      }),
  });

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
