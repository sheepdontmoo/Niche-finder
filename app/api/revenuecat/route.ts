import { NextRequest, NextResponse } from "next/server";
import { meteringEnabled, setPro } from "@/lib/metering";

export const runtime = "nodejs";

// RevenueCat webhook. Configure it in RevenueCat → Integrations → Webhooks:
//   URL:           https://<your-deployment>/api/revenuecat
//   Authorization: the value you set as REVENUECAT_WEBHOOK_SECRET
//
// RevenueCat is the source of truth for entitlements; this endpoint just
// mirrors the current state into Redis so /api/analyze can skip the scan gate
// for subscribers.

// Events that mean the subscriber currently has access.
const GRANTS = new Set([
  "INITIAL_PURCHASE",
  "RENEWAL",
  "UNCANCELLATION",
  "PRODUCT_CHANGE",
  "SUBSCRIPTION_EXTENDED",
  "TEMPORARY_ENTITLEMENT_GRANT",
]);

// Events that mean access has ended. Note CANCELLATION is deliberately absent:
// a cancelled subscription keeps access until it expires.
const REVOKES = new Set(["EXPIRATION", "SUBSCRIPTION_PAUSED", "TRANSFER"]);

type RcEvent = {
  type?: string;
  app_user_id?: string;
  original_app_user_id?: string;
};

export async function POST(req: NextRequest) {
  const secret = process.env.REVENUECAT_WEBHOOK_SECRET;
  if (!secret) {
    console.error("REVENUECAT_WEBHOOK_SECRET is not set — rejecting webhook");
    return NextResponse.json({ error: "Not configured" }, { status: 500 });
  }
  if (req.headers.get("authorization") !== secret) {
    return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
  }

  let body: { event?: RcEvent };
  try {
    body = await req.json();
  } catch {
    return NextResponse.json({ error: "Invalid JSON" }, { status: 400 });
  }

  const event = body.event;
  const type = event?.type;
  const userId = event?.app_user_id ?? event?.original_app_user_id;

  if (!type || !userId) {
    return NextResponse.json({ error: "Missing event type or user" }, { status: 400 });
  }

  if (!meteringEnabled()) {
    // Nothing to mirror into; acknowledge so RevenueCat doesn't retry forever.
    return NextResponse.json({ ok: true, stored: false });
  }

  try {
    if (GRANTS.has(type)) {
      await setPro(userId, true);
    } else if (REVOKES.has(type)) {
      await setPro(userId, false);
    }
    // Anything else (CANCELLATION, BILLING_ISSUE, TEST…) is informational.
    return NextResponse.json({ ok: true });
  } catch (e) {
    console.error("Failed to store entitlement", e);
    // 500 makes RevenueCat retry, which is what we want on a transient failure.
    return NextResponse.json({ error: "Storage failed" }, { status: 500 });
  }
}
