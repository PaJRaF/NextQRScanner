package com.nextqr.scanner.presentation.components

import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.nextqr.scanner.BuildConfig
import com.nextqr.scanner.data.ads.AdsManager

/**
 * Adaptive anchored banner. Rendered only for non-premium users (the caller
 * gates on premium status). Uses Google's test unit id in debug builds.
 */
@Composable
fun BannerAd(modifier: Modifier = Modifier) {
    val configuration = LocalConfiguration.current
    val adWidth = configuration.screenWidthDp
    val unitId = if (BuildConfig.USE_TEST_ADS) AdsManager.TEST_BANNER else AdsManager.PROD_BANNER

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(
                    AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth),
                )
                adUnitId = unitId
                loadAd(AdRequest.Builder().build())
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                )
            }
        },
    )
}
