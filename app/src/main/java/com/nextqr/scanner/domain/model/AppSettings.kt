package com.nextqr.scanner.domain.model

/** User-facing preferences persisted in encrypted storage. */
data class AppSettings(
    val theme: ThemePreference = ThemePreference.SYSTEM,
    val useDynamicColor: Boolean = true,
    val vibrateOnScan: Boolean = true,
    val soundOnScan: Boolean = false,
    val checkUrlSafety: Boolean = true,
    val onboardingCompleted: Boolean = false,
    /** GDPR/TCF ad-personalisation consent. False until the user opts in. */
    val adConsentGranted: Boolean = false,
    /** Persist scan history at all (user can opt out entirely). */
    val historyEnabled: Boolean = true,
)

enum class ThemePreference { LIGHT, DARK, SYSTEM }
