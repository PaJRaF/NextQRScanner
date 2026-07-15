package com.nextqr.scanner.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

object Routes {
    const val ONBOARDING = "onboarding"
    const val SCANNER = "scanner"
    const val GENERATOR = "generator"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    const val PREMIUM = "premium"
    const val DETAIL = "detail/{scanId}"

    fun detail(scanId: Long) = "detail/$scanId"
}

/** Bottom-navigation destinations. */
enum class TopLevelDestination(
    val route: String,
    val icon: ImageVector,
    val labelRes: Int,
) {
    SCANNER(Routes.SCANNER, Icons.Outlined.QrCodeScanner, com.nextqr.scanner.R.string.nav_scan),
    GENERATOR(Routes.GENERATOR, Icons.Outlined.QrCode2, com.nextqr.scanner.R.string.nav_generate),
    HISTORY(Routes.HISTORY, Icons.Outlined.History, com.nextqr.scanner.R.string.nav_history),
    SETTINGS(Routes.SETTINGS, Icons.Outlined.Settings, com.nextqr.scanner.R.string.nav_settings),
}
