package com.nichefinder.scan.billing

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EntitlementTest {

    @Test fun `no purchases means locked, not pending, nothing to acknowledge`() {
        assertFalse(Entitlement.isProUnlocked(emptyList()))
        assertFalse(Entitlement.isPending(emptyList()))
        assertFalse(Entitlement.needsAcknowledgement(emptyList()))
    }

    @Test fun `purchased and acknowledged is fully unlocked`() {
        val purchases = listOf(PurchaseRecord(Entitlement.PRO_PRODUCT_ID, PurchaseState.PURCHASED, acknowledged = true))
        assertTrue(Entitlement.isProUnlocked(purchases))
        assertFalse(Entitlement.needsAcknowledgement(purchases))
        assertFalse(Entitlement.isPending(purchases))
    }

    @Test fun `purchased but not yet acknowledged is still unlocked immediately`() {
        // This is the exact bug class that sank CamScanner's rating: a user who paid must never
        // see "not purchased" while Play's own record says PURCHASED.
        val purchases = listOf(PurchaseRecord(Entitlement.PRO_PRODUCT_ID, PurchaseState.PURCHASED, acknowledged = false))
        assertTrue(Entitlement.isProUnlocked(purchases))
        assertTrue(Entitlement.needsAcknowledgement(purchases))
    }

    @Test fun `pending purchase is not unlocked but is flagged as pending`() {
        val purchases = listOf(PurchaseRecord(Entitlement.PRO_PRODUCT_ID, PurchaseState.PENDING, acknowledged = false))
        assertFalse(Entitlement.isProUnlocked(purchases))
        assertTrue(Entitlement.isPending(purchases))
        assertFalse(Entitlement.needsAcknowledgement(purchases))
    }

    @Test fun `purchase of a different product does not unlock pro`() {
        val purchases = listOf(PurchaseRecord("some_other_sku", PurchaseState.PURCHASED, acknowledged = true))
        assertFalse(Entitlement.isProUnlocked(purchases))
    }

    @Test fun `unlock is found among multiple unrelated purchase records`() {
        val purchases = listOf(
            PurchaseRecord("some_other_sku", PurchaseState.PURCHASED, acknowledged = true),
            PurchaseRecord(Entitlement.PRO_PRODUCT_ID, PurchaseState.PENDING, acknowledged = false),
            PurchaseRecord(Entitlement.PRO_PRODUCT_ID, PurchaseState.PURCHASED, acknowledged = false),
        )
        assertTrue(Entitlement.isProUnlocked(purchases))
        assertTrue(Entitlement.needsAcknowledgement(purchases))
        assertTrue(Entitlement.isPending(purchases))
    }

    @Test fun `unspecified state does not unlock`() {
        val purchases = listOf(PurchaseRecord(Entitlement.PRO_PRODUCT_ID, PurchaseState.UNSPECIFIED, acknowledged = false))
        assertFalse(Entitlement.isProUnlocked(purchases))
        assertFalse(Entitlement.isPending(purchases))
    }
}
