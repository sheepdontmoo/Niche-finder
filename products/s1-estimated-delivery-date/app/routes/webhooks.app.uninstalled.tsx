import type { ActionFunctionArgs } from "react-router";
import { authenticate } from "../shopify.server";
import db from "../db.server";
import { deleteShopData } from "../lib/shop-data.server";

export const action = async ({ request }: ActionFunctionArgs) => {
  const { shop, topic } = await authenticate.webhook(request);

  console.log(`Received ${topic} webhook for ${shop}`);
  // Webhooks are retried and can arrive after Shopify has already invalidated
  // the session. Delete by authenticated shop unconditionally and idempotently.
  await deleteShopData(db, shop);

  return new Response();
};
