package com.nextqr.scanner.data.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.nextqr.scanner.BuildConfig
import com.nextqr.scanner.data.billing.CurrentActivityHolder
import com.nextqr.scanner.domain.repository.BillingRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interstitial ad cadence manager.
 *
 * Rules that keep us inside Google Play Ads Policy:
 *  - No ads at all for premium users.
 *  - Interstitials only after every [SCANS_PER_INTERSTITIAL]th completed scan,
 *    never interrupting an in-progress action.
 *  - Test ad unit ids are always used in debug builds.
 *
 * Banner ads are handled in Compose via [com.nextqr.scanner.presentation.components.BannerAd].
 */
@Singleton
class AdsManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val billingRepository: BillingRepository,
    private val activityHolder: CurrentActivityHolder,
) {
    private val scope = CoroutineScope(SupervisorJob())
    private val scanCounter = AtomicInteger(0)
    private var interstitial: InterstitialAd? = null

    /** Set true only after consent (or non-personalised fallback) is resolved. */
    val initialized = MutableStateFlow(false)

    fun initialize(personalizedAllowed: Boolean) {
        // Keep child-directed/again-consent conservative: limit to test devices
        // in debug and disable personalised ads unless consent granted.
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setTagForChildDirectedTreatment(
                    RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_UNSPECIFIED,
                )
                .build(),
        )
        MobileAds.initialize(context) {
            initialized.value = true
            preloadInterstitial(personalizedAllowed)
        }
    }

    private fun preloadInterstitial(personalizedAllowed: Boolean) {
        val request = buildRequest(personalizedAllowed)
        InterstitialAd.load(
            context,
            interstitialUnitId(),
            request,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitial = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "Interstitial failed to load: ${error.code}")
                    interstitial = null
                }
            },
        )
    }

    /**
     * Called after each successful scan. Shows an interstitial when the cadence
     * threshold is reached and the user is not premium.
     */
    fun onScanCompleted(personalizedAllowed: Boolean) {
        scope.launch {
            if (billingRepository.premiumStatus.first().isPremium) return@launch
            val count = scanCounter.incrementAndGet()
            if (count % SCANS_PER_INTERSTITIAL != 0) return@launch
            val activity: Activity = activityHolder.activity ?: return@launch
            val ad = interstitial
            if (ad != null) {
                ad.show(activity)
                interstitial = null
                preloadInterstitial(personalizedAllowed)
            } else {
                preloadInterstitial(personalizedAllowed)
            }
        }
    }

    private fun buildRequest(personalizedAllowed: Boolean): AdRequest {
        val builder = AdRequest.Builder()
        if (!personalizedAllowed) {
            val extras = android.os.Bundle().apply { putString("npa", "1") }
            builder.addNetworkExtrasBundle(
                com.google.ads.mediation.admob.AdMobAdapter::class.java, extras,
            )
        }
        return builder.build()
    }

    private fun interstitialUnitId(): String =
        if (BuildConfig.USE_TEST_ADS) TEST_INTERSTITIAL else PROD_INTERSTITIAL

    companion object {
        const val SCANS_PER_INTERSTITIAL = 5

        // Google's official test unit ids.
        const val TEST_INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"
        const val TEST_BANNER = "ca-app-pub-3940256099942544/6300978111"
        const val TEST_REWARDED = "ca-app-pub-3940256099942544/5224354917"

        // TODO: replace with real production unit ids before release.
        const val PROD_INTERSTITIAL = "ca-app-pub-0000000000000000/0000000000"
        const val PROD_BANNER = "ca-app-pub-0000000000000000/0000000001"
        const val PROD_REWARDED = "ca-app-pub-0000000000000000/0000000002"

        private const val TAG = "AdsManager"
    }
}
