package com.nextqr.scanner.data.billing

import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.nextqr.scanner.domain.model.PremiumProduct
import com.nextqr.scanner.domain.model.PremiumStatus
import com.nextqr.scanner.domain.repository.BillingRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Google Play Billing implementation. Owns the [BillingClient] connection,
 * exposes premium entitlement + product catalogue as flows, and drives the
 * purchase/acknowledge lifecycle. All purchases are acknowledged (required by
 * Play, or they auto-refund after 3 days).
 */
@Singleton
class BillingRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val activityHolder: CurrentActivityHolder,
) : BillingRepository, PurchasesUpdatedListener {

    private val scope = CoroutineScope(SupervisorJob())

    private val _premiumStatus = MutableStateFlow(PremiumStatus.FREE)
    override val premiumStatus = _premiumStatus.asStateFlow()

    private val _products = MutableStateFlow<List<PremiumProduct>>(emptyList())
    override val products = _products.asStateFlow()

    private val detailsCache = mutableMapOf<String, ProductDetails>()

    @Volatile
    private var rewardedUntil: Long = 0L

    private val client: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build(),
        )
        .build()

    init {
        connect()
    }

    private fun connect() {
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    scope.launch { refresh() }
                }
            }

            override fun onBillingServiceDisconnected() {
                // Reconnect lazily on next refresh/purchase.
            }
        })
    }

    override suspend fun refresh() {
        queryProducts()
        queryOwnedPurchases()
    }

    private suspend fun queryProducts() {
        val inAppParams = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    productQuery(PRODUCT_LIFETIME, BillingClient.ProductType.INAPP),
                ),
            ).build()
        val subParams = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    productQuery(PRODUCT_SUB_MONTHLY, BillingClient.ProductType.SUBS),
                    productQuery(PRODUCT_SUB_YEARLY, BillingClient.ProductType.SUBS),
                ),
            ).build()

        val collected = buildList {
            addAll(runCatching { client.queryProductDetails(inAppParams).productDetailsList.orEmpty() }.getOrDefault(emptyList()))
            addAll(runCatching { client.queryProductDetails(subParams).productDetailsList.orEmpty() }.getOrDefault(emptyList()))
        }

        collected.forEach { detailsCache[it.productId] = it }
        _products.value = collected.map { it.toPremiumProduct() }
    }

    private suspend fun queryOwnedPurchases() {
        val inApp = runCatching {
            client.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build(),
            ).purchasesList
        }.getOrDefault(emptyList())
        val subs = runCatching {
            client.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build(),
            ).purchasesList
        }.getOrDefault(emptyList())

        (inApp + subs).forEach { handlePurchase(it) }
        recomputeStatus(inApp, subs)
    }

    private fun recomputeStatus(inApp: List<Purchase>, subs: List<Purchase>) {
        val ownsLifetime = inApp.any {
            it.products.contains(PRODUCT_LIFETIME) &&
                it.purchaseState == Purchase.PurchaseState.PURCHASED
        }
        val ownsSub = subs.any { it.purchaseState == Purchase.PurchaseState.PURCHASED }
        _premiumStatus.value = when {
            ownsLifetime -> PremiumStatus(true, PremiumStatus.Source.LIFETIME)
            ownsSub -> PremiumStatus(true, PremiumStatus.Source.SUBSCRIPTION)
            System.currentTimeMillis() < rewardedUntil ->
                PremiumStatus(true, PremiumStatus.Source.REWARDED_TEMPORARY)
            else -> PremiumStatus.FREE
        }
    }

    override suspend fun purchase(productId: String): BillingRepository.PurchaseOutcome {
        val activity = activityHolder.activity
            ?: return BillingRepository.PurchaseOutcome.ERROR
        val details = detailsCache[productId]
            ?: return BillingRepository.PurchaseOutcome.UNAVAILABLE

        val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .apply {
                // Subscriptions require an offer token.
                details.subscriptionOfferDetails?.firstOrNull()?.let {
                    setOfferToken(it.offerToken)
                }
            }
            .build()

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productParams))
            .build()

        val result = client.launchBillingFlow(activity, flowParams)
        return if (result.responseCode == BillingClient.BillingResponseCode.OK) {
            BillingRepository.PurchaseOutcome.LAUNCHED
        } else {
            BillingRepository.PurchaseOutcome.ERROR
        }
    }

    override fun grantRewardedUnlock(durationMillis: Long) {
        rewardedUntil = System.currentTimeMillis() + durationMillis
        _premiumStatus.value = PremiumStatus(true, PremiumStatus.Source.REWARDED_TEMPORARY)
    }

    // --- PurchasesUpdatedListener ---
    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            scope.launch {
                purchases.forEach { handlePurchase(it) }
                queryOwnedPurchases()
            }
        }
    }

    private suspend fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return
        if (!purchase.isAcknowledged) {
            runCatching {
                client.acknowledgePurchase(
                    AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken).build(),
                )
            }.onFailure { Log.w(TAG, "Acknowledge failed") }
        }
    }

    private fun productQuery(id: String, type: String) =
        QueryProductDetailsParams.Product.newBuilder()
            .setProductId(id)
            .setProductType(type)
            .build()

    private fun ProductDetails.toPremiumProduct(): PremiumProduct {
        val kind = when (productId) {
            PRODUCT_LIFETIME -> PremiumProduct.Kind.LIFETIME
            PRODUCT_SUB_YEARLY -> PremiumProduct.Kind.SUBSCRIPTION_YEARLY
            else -> PremiumProduct.Kind.SUBSCRIPTION_MONTHLY
        }
        val price = oneTimePurchaseOfferDetails?.formattedPrice
            ?: subscriptionOfferDetails?.firstOrNull()
                ?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
            ?: ""
        return PremiumProduct(productId, name, description, price, kind)
    }

    companion object {
        const val PRODUCT_LIFETIME = "premium_lifetime"
        const val PRODUCT_SUB_MONTHLY = "premium_monthly"
        const val PRODUCT_SUB_YEARLY = "premium_yearly"
        private const val TAG = "Billing"
    }
}
