package com.nichefinder.docstash.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.nichefinder.scan.billing.Entitlement
import com.nichefinder.scan.billing.PurchaseRecord
import com.nichefinder.scan.billing.PurchaseState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Play Billing wiring for the single one-time Pro unlock (`pro_unlock`) — one product, one price,
 * never a subscription. Every unlock decision is delegated to scan-core's [Entitlement], which
 * trusts Play's PURCHASED state the instant it's reported — before, and regardless of,
 * acknowledgement. That is the deliberate fix for the exact "charged but the app still says not
 * purchased" bug that helped sink CamScanner's rating.
 */
class BillingManager(context: Context) : PurchasesUpdatedListener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val client = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    private val _proUnlocked = MutableStateFlow(false)
    val proUnlocked: StateFlow<Boolean> = _proUnlocked.asStateFlow()

    /** Localized price ("$7.99") straight from Play — never hardcoded, currency-correct everywhere. */
    private val _priceLabel = MutableStateFlow<String?>(null)
    val priceLabel: StateFlow<String?> = _priceLabel.asStateFlow()

    @Volatile private var productDetails: ProductDetails? = null

    init {
        scope.launch {
            val setupResult = startConnection()
            if (setupResult.responseCode == BillingClient.BillingResponseCode.OK) {
                loadProductDetails()
                refreshPurchases()
            }
        }
    }

    private suspend fun startConnection(): BillingResult = suspendCancellableCoroutine { cont ->
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (cont.isActive) cont.resume(result)
            }
            override fun onBillingServiceDisconnected() {
                // Auto service reconnection (default since Billing Library 8) handles retries.
            }
        })
    }

    private suspend fun loadProductDetails() {
        val product = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(Entitlement.PRO_PRODUCT_ID)
            .setProductType(ProductType.INAPP)
            .build()
        val params = QueryProductDetailsParams.newBuilder().setProductList(listOf(product)).build()
        val result = client.queryProductDetails(params)
        val details = result.productDetailsList?.firstOrNull()
        productDetails = details
        _priceLabel.value = details?.oneTimePurchaseOfferDetails?.formattedPrice
    }

    /** Re-checks purchases with Play directly — call on app foreground, the safety net the docs recommend. */
    suspend fun refreshPurchases() {
        val params = QueryPurchasesParams.newBuilder().setProductType(ProductType.INAPP).build()
        val result = client.queryPurchasesAsync(params)
        // A failed query (billing disconnected, transient error, ...) must never look like "the
        // user owns nothing" -- that's precisely the "charged but shows not purchased" bug this
        // app exists to not repeat. Only trust an OK response; leave entitlement untouched otherwise.
        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            applyPurchases(result.purchasesList)
        }
    }

    private fun applyPurchases(purchases: List<Purchase>) {
        val records = purchases.flatMap { purchase ->
            purchase.products.map { productId ->
                PurchaseRecord(
                    productId = productId,
                    state = when (purchase.purchaseState) {
                        Purchase.PurchaseState.PURCHASED -> PurchaseState.PURCHASED
                        Purchase.PurchaseState.PENDING -> PurchaseState.PENDING
                        else -> PurchaseState.UNSPECIFIED
                    },
                    acknowledged = purchase.isAcknowledged,
                )
            }
        }
        // Unlocked the instant Play says PURCHASED — never gated on acknowledgement finishing first.
        _proUnlocked.value = Entitlement.isProUnlocked(records)

        if (Entitlement.needsAcknowledgement(records)) {
            purchases
                .filter { !it.isAcknowledged && it.purchaseState == Purchase.PurchaseState.PURCHASED }
                .forEach { purchase -> scope.launch { acknowledge(purchase) } }
        }
    }

    private suspend fun acknowledge(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
        client.acknowledgePurchase(params)
    }

    fun launchPurchase(activity: Activity) {
        val details = productDetails ?: return
        val paramsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(details).build(),
        )
        val flowParams = BillingFlowParams.newBuilder().setProductDetailsParamsList(paramsList).build()
        client.launchBillingFlow(activity, flowParams)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            applyPurchases(purchases)
        }
    }

    fun close() {
        client.endConnection()
    }
}
