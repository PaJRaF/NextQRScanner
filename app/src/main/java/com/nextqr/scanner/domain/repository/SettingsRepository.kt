package com.nextqr.scanner.domain.repository

import com.nextqr.scanner.domain.model.AppSettings
import com.nextqr.scanner.domain.model.ThemePreference
import kotlinx.coroutines.flow.Flow

/** Reactive access to user preferences (backed by encrypted storage). */
interface SettingsRepository {
    val settings: Flow<AppSettings>

    suspend fun setTheme(theme: ThemePreference)
    suspend fun setDynamicColor(enabled: Boolean)
    suspend fun setVibrateOnScan(enabled: Boolean)
    suspend fun setSoundOnScan(enabled: Boolean)
    suspend fun setCheckUrlSafety(enabled: Boolean)
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun setAdConsentGranted(granted: Boolean)
    suspend fun setHistoryEnabled(enabled: Boolean)
}
