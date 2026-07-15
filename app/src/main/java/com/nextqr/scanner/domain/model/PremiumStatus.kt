package com.nextqr.scanner.domain.model

/** Current entitlement state, derived from Play Billing purchases. */
data class PremiumStatus(
    val isPremium: Boolean,
    val source: Source,
) {
    enum class Source { NONE, LIFETIME, SUBSCRIPTION, REWARDED_TEMPORARY }

    companion object {
        val FREE = PremiumStatus(isPremium = false, source = Source.NONE)
    }
}

/** A purchasable product surfaced on the paywall. */
data class PremiumProduct(
    val productId: String,
    val title: String,
    val description: String,
    val formattedPrice: String,
    val kind: Kind,
) {
    enum class Kind { LIFETIME, SUBSCRIPTION_MONTHLY, SUBSCRIPTION_YEARLY }
}
