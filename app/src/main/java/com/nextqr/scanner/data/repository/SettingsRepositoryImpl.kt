package com.nextqr.scanner.data.repository

import android.content.SharedPreferences
import com.nextqr.scanner.data.security.DatabaseKeyProvider
import com.nextqr.scanner.domain.model.AppSettings
import com.nextqr.scanner.domain.model.ThemePreference
import com.nextqr.scanner.domain.repository.SettingsRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Preferences persisted in the same [EncryptedSharedPreferences] instance used
 * for the DB passphrase — everything sensitive stays keystore-sealed. Emits a
 * fresh [AppSettings] snapshot on every change via an OnSharedPreferenceChange
 * listener bridged into a Flow.
 */
@Singleton
class SettingsRepositoryImpl @Inject constructor(
    keyProvider: DatabaseKeyProvider,
) : SettingsRepository {

    private val prefs: SharedPreferences = keyProvider.securePrefs()

    override val settings: Flow<AppSettings> = callbackFlow {
        trySend(readSnapshot())
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            trySend(readSnapshot())
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }.distinctUntilChanged()

    private fun readSnapshot() = AppSettings(
        theme = ThemePreference.valueOf(
            prefs.getString(KEY_THEME, ThemePreference.SYSTEM.name)!!,
        ),
        useDynamicColor = prefs.getBoolean(KEY_DYNAMIC_COLOR, true),
        vibrateOnScan = prefs.getBoolean(KEY_VIBRATE, true),
        soundOnScan = prefs.getBoolean(KEY_SOUND, false),
        checkUrlSafety = prefs.getBoolean(KEY_URL_SAFETY, true),
        onboardingCompleted = prefs.getBoolean(KEY_ONBOARDING, false),
        adConsentGranted = prefs.getBoolean(KEY_AD_CONSENT, false),
        historyEnabled = prefs.getBoolean(KEY_HISTORY, true),
    )

    override suspend fun setTheme(theme: ThemePreference) =
        prefs.edit().putString(KEY_THEME, theme.name).apply()

    override suspend fun setDynamicColor(enabled: Boolean) =
        prefs.edit().putBoolean(KEY_DYNAMIC_COLOR, enabled).apply()

    override suspend fun setVibrateOnScan(enabled: Boolean) =
        prefs.edit().putBoolean(KEY_VIBRATE, enabled).apply()

    override suspend fun setSoundOnScan(enabled: Boolean) =
        prefs.edit().putBoolean(KEY_SOUND, enabled).apply()

    override suspend fun setCheckUrlSafety(enabled: Boolean) =
        prefs.edit().putBoolean(KEY_URL_SAFETY, enabled).apply()

    override suspend fun setOnboardingCompleted(completed: Boolean) =
        prefs.edit().putBoolean(KEY_ONBOARDING, completed).apply()

    override suspend fun setAdConsentGranted(granted: Boolean) =
        prefs.edit().putBoolean(KEY_AD_CONSENT, granted).apply()

    override suspend fun setHistoryEnabled(enabled: Boolean) =
        prefs.edit().putBoolean(KEY_HISTORY, enabled).apply()

    private companion object {
        const val KEY_THEME = "pref_theme"
        const val KEY_DYNAMIC_COLOR = "pref_dynamic_color"
        const val KEY_VIBRATE = "pref_vibrate"
        const val KEY_SOUND = "pref_sound"
        const val KEY_URL_SAFETY = "pref_url_safety"
        const val KEY_ONBOARDING = "pref_onboarding"
        const val KEY_AD_CONSENT = "pref_ad_consent"
        const val KEY_HISTORY = "pref_history"
    }
}
