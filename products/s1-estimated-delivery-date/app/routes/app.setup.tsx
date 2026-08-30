import type { HeadersFunction, LoaderFunctionArgs } from "react-router";
import { useLoaderData } from "react-router";
import { boundary } from "@shopify/shopify-app-react-router/server";
import { authenticate } from "../shopify.server";
import { buildThemeActivationUrl } from "../lib/theme-activation";

export const loader = async ({ request }: LoaderFunctionArgs) => {
  const { session } = await authenticate.admin(request);
  return {
    activationUrl: buildThemeActivationUrl(
      session.shop,
      process.env.SHOPIFY_API_KEY,
    ),
  };
};

export default function Setup() {
  const { activationUrl } = useLoaderData<typeof loader>();

  return (
    <s-page heading="Setup guide">
      <s-section heading="Activate on a product page">
        <s-paragraph>
          First configure and save your delivery rules on the Settings page.
          Then open your current theme with the Estimated Delivery Date block
          ready to add, and preview its placement before saving the theme.
        </s-paragraph>
        {activationUrl ? (
          <s-button variant="primary" href={activationUrl} target="_top">
            Open theme editor
          </s-button>
        ) : (
          <s-paragraph>
            The activation link is unavailable because the app client ID is
            missing. Use the manual steps below.
          </s-paragraph>
        )}
      </s-section>

      <s-section heading="Add the delivery estimate to your storefront">
        <s-ordered-list>
          <s-list-item>
            Configure your rules on the{" "}
            <s-link href="/app">Settings</s-link> page (processing time,
            cutoff, shipping time, working days, and wording).
          </s-list-item>
          <s-list-item>
            In your Shopify admin, open{" "}
            <s-text>Online Store → Themes → Customize</s-text>.
          </s-list-item>
          <s-list-item>
            Open a <s-text>Product</s-text> template, choose{" "}
            <s-text>Add block</s-text>, and add{" "}
            <s-text>Estimated Delivery Date</s-text> under the app blocks.
          </s-list-item>
          <s-list-item>
            Position the block where you want the estimate to appear (usually
            just under the Add to cart button), then <s-text>Save</s-text>.
          </s-list-item>
        </s-ordered-list>
      </s-section>

      <s-section heading="Good to know">
        <s-unordered-list>
          <s-list-item>
            The estimate updates automatically based on the current date and
            your cutoff time.
          </s-list-item>
          <s-list-item>
            Estimates are computed from your working days, so weekends and
            non-shipping days are skipped.
          </s-list-item>
          <s-list-item>
            No theme code is required — the block is drag-and-drop.
          </s-list-item>
        </s-unordered-list>
      </s-section>
    </s-page>
  );
}

export const headers: HeadersFunction = (headersArgs) => {
  return boundary.headers(headersArgs);
};
