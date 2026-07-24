// Play Billing / StoreKit via RevenueCat.
//
// The plugin is native-only. On the web build (and in the browser during
// development) every call here degrades to "not subscribed, can't buy", so
// the paywall still renders with the fallback prices from app/page.tsx.

import type { PurchasesPackage } from "@revenuecat/purchases-capacitor";

// Set in RevenueCat → Project settings → API keys (the *public* Google Play
// key — it is safe in the client bundle; the secret key never ships).
const RC_KEY = process.env.NEXT_PUBLIC_REVENUECAT_ANDROID_KEY ?? "";
const RC_KEY_IOS = process.env.NEXT_PUBLIC_REVENUECAT_IOS_KEY ?? "";

// Entitlement identifier configured in RevenueCat.
export const ENTITLEMENT = "pro";

export type Plan = {
  key: "monthly" | "yearly";
  priceString: string;
  productId: string;
  pkg: PurchasesPackage;
};

let ready: Promise<boolean> | null = null;

async function native() {
  const { Capacitor } = await import("@capacitor/core");
  return Capacitor.isNativePlatform();
}

/** Configure the SDK once, keyed to the same anonymous device id the API uses. */
export function initBilling(appUserId: string): Promise<boolean> {
  if (ready) return ready;
  ready = (async () => {
    try {
      if (!(await native())) return false;
      const { Capacitor } = await import("@capacitor/core");
      const apiKey = Capacitor.getPlatform() === "ios" ? RC_KEY_IOS : RC_KEY;
      if (!apiKey) return false;
      const { Purchases } = await import("@revenuecat/purchases-capacitor");
      await Purchases.configure({ apiKey, appUserID: appUserId });
      return true;
    } catch (e) {
      console.error("Billing unavailable", e);
      return false;
    }
  })();
  return ready;
}

/** Live plans from the store, so prices and trials are always the real ones. */
export async function getPlans(): Promise<Plan[]> {
  if (!(await ready)) return [];
  try {
    const { Purchases } = await import("@revenuecat/purchases-capacitor");
    const offerings = await Purchases.getOfferings();
    const packages = offerings.current?.availablePackages ?? [];
    return packages
      .map((pkg): Plan | null => {
        const type = String(pkg.packageType).toUpperCase();
        const key =
          type === "ANNUAL" ? "yearly" : type === "MONTHLY" ? "monthly" : null;
        if (!key) return null;
        return {
          key,
          priceString: pkg.product.priceString,
          productId: pkg.product.identifier,
          pkg,
        };
      })
      .filter((p): p is Plan => p !== null);
  } catch (e) {
    console.error("Could not load plans", e);
    return [];
  }
}

export async function isSubscribed(): Promise<boolean> {
  if (!(await ready)) return false;
  try {
    const { Purchases } = await import("@revenuecat/purchases-capacitor");
    const { customerInfo } = await Purchases.getCustomerInfo();
    return Boolean(customerInfo.entitlements.active[ENTITLEMENT]);
  } catch {
    return false;
  }
}

export type PurchaseOutcome =
  | { status: "subscribed" }
  | { status: "cancelled" }
  | { status: "unavailable" }
  | { status: "error"; message: string };

export async function purchase(plan: Plan): Promise<PurchaseOutcome> {
  if (!(await ready)) return { status: "unavailable" };
  try {
    const { Purchases } = await import("@revenuecat/purchases-capacitor");
    const { customerInfo } = await Purchases.purchasePackage({
      aPackage: plan.pkg,
    });
    return customerInfo.entitlements.active[ENTITLEMENT]
      ? { status: "subscribed" }
      : { status: "error", message: "The purchase didn't activate. Try Restore." };
  } catch (e) {
    // The plugin rejects with userCancelled when the user backs out.
    if (e && typeof e === "object" && "userCancelled" in e && e.userCancelled) {
      return { status: "cancelled" };
    }
    return {
      status: "error",
      message: e instanceof Error ? e.message : "That purchase didn't complete",
    };
  }
}

/** Required by both stores: let an existing subscriber get access back. */
export async function restore(): Promise<PurchaseOutcome> {
  if (!(await ready)) return { status: "unavailable" };
  try {
    const { Purchases } = await import("@revenuecat/purchases-capacitor");
    const { customerInfo } = await Purchases.restorePurchases();
    return customerInfo.entitlements.active[ENTITLEMENT]
      ? { status: "subscribed" }
      : { status: "error", message: "No previous subscription found on this account." };
  } catch (e) {
    return {
      status: "error",
      message: e instanceof Error ? e.message : "Couldn't restore purchases",
    };
  }
}
