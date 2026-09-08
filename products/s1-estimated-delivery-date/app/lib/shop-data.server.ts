type SessionStore = {
  session: {
    deleteMany(args: { where: { shop: string } }): Promise<unknown>;
  };
};

/** Delete every database record this app stores for a shop, idempotently. */
export async function deleteShopData(
  store: SessionStore,
  shop: string,
): Promise<void> {
  if (!shop.trim()) throw new Error("Shop domain is required for data deletion");
  await store.session.deleteMany({ where: { shop } });
}
