package com.nextqr.scanner.domain.repository

import com.nextqr.scanner.domain.model.PremiumProduct
import com.nextqr.scanner.domain.model.PremiumStatus
import kotlinx.coroutines.flow.Flow

/** Abstraction over Google Play Billing for premium entitlements. */
interface BillingRepository {
    val premiumStatus: Flow<PremiumStatus>

    /** Products available for purchase on the paywall. */
    val products: Flow<List<PremiumProduct>>

    /** Refreshes product details and re-queries owned purchases. */
    suspend fun refresh()

    /**
     * Launches the Play purchase flow. [activityProvider] returns the current
     * Activity required by BillingClient. Returns a coarse outcome; the actual
     * entitlement change is delivered through [premiumStatus].
     */
    suspend fun purchase(productId: String): PurchaseOutcome

    /** Grants a temporary premium unlock after a rewarded ad completes. */
    fun grantRewardedUnlock(durationMillis: Long)

    enum class PurchaseOutcome { LAUNCHED, ALREADY_OWNED, UNAVAILABLE, CANCELLED, ERROR }
}
