package com.nextqr.scanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nextqr.scanner.data.billing.CurrentActivityHolder
import com.nextqr.scanner.domain.model.ThemePreference
import com.nextqr.scanner.presentation.NextQrApp
import com.nextqr.scanner.presentation.theme.NextQrTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var activityHolder: CurrentActivityHolder

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Keep the splash on screen until settings (theme) are loaded.
        splash.setKeepOnScreenCondition { !viewModel.isReady.value }

        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            val darkTheme = when (settings.theme) {
                ThemePreference.LIGHT -> false
                ThemePreference.DARK -> true
                ThemePreference.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
            }
            NextQrTheme(
                darkTheme = darkTheme,
                dynamicColor = settings.useDynamicColor,
            ) {
                NextQrApp(
                    onboardingCompleted = settings.onboardingCompleted,
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        activityHolder.set(this)
    }

    override fun onPause() {
        activityHolder.set(null)
        super.onPause()
    }
}
