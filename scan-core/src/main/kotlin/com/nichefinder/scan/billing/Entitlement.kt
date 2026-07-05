package com.nichefinder.scan.billing

/** Mirrors Play Billing's `Purchase.PurchaseState` without depending on the Android SDK. */
enum class PurchaseState { PENDING, PURCHASED, UNSPECIFIED }

/** The fields of a Play Billing `Purchase` that the unlock decision actually depends on. */
data class PurchaseRecord(
    val productId: String,
    val state: PurchaseState,
    val acknowledged: Boolean,
)

/**
 * Pure decision logic for the one-time Pro unlock, kept separate from `BillingClient` so it can
 * be unit tested directly.
 *
 * This exists because of a specific, named failure mode: CamScanner customers who paid for the
 * one-time unlock reported the app still showing "not purchased." That bug class is almost always
 * a UI layer that only trusts a purchase *after* acknowledgement, instead of trusting Play's
 * PURCHASED state immediately and acknowledging in the background. [isProUnlocked] deliberately
 * does the former — unlocked the instant Play reports PURCHASED, acknowledged or not.
 */
object Entitlement {
    const val PRO_PRODUCT_ID = "pro_unlock"

    /** True the instant Play reports a PURCHASED record for [productId] — acknowledged or not. */
    fun isProUnlocked(purchases: List<PurchaseRecord>, productId: String = PRO_PRODUCT_ID): Boolean =
        purchases.any { it.productId == productId && it.state == PurchaseState.PURCHASED }

    /**
     * Play Billing refunds purchases automatically if not acknowledged within 3 days. Any
     * PURCHASED-but-unacknowledged record needs an immediate acknowledgement call.
     */
    fun needsAcknowledgement(purchases: List<PurchaseRecord>, productId: String = PRO_PRODUCT_ID): Boolean =
        purchases.any { it.productId == productId && it.state == PurchaseState.PURCHASED && !it.acknowledged }

    /** Pending purchases (e.g. a cash-based payment method still processing) aren't failures, just not unlocked yet. */
    fun isPending(purchases: List<PurchaseRecord>, productId: String = PRO_PRODUCT_ID): Boolean =
        purchases.any { it.productId == productId && it.state == PurchaseState.PENDING }
}
