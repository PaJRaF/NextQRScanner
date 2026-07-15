package com.nextqr.scanner.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextqr.scanner.domain.model.AppSettings
import com.nextqr.scanner.domain.model.ThemePreference
import com.nextqr.scanner.domain.repository.ScanHistoryRepository
import com.nextqr.scanner.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val historyRepository: ScanHistoryRepository,
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    fun setTheme(theme: ThemePreference) = viewModelScope.launch {
        settingsRepository.setTheme(theme)
    }

    fun setDynamicColor(enabled: Boolean) = viewModelScope.launch {
        settingsRepository.setDynamicColor(enabled)
    }

    fun setVibrate(enabled: Boolean) = viewModelScope.launch {
        settingsRepository.setVibrateOnScan(enabled)
    }

    fun setSound(enabled: Boolean) = viewModelScope.launch {
        settingsRepository.setSoundOnScan(enabled)
    }

    fun setCheckUrlSafety(enabled: Boolean) = viewModelScope.launch {
        settingsRepository.setCheckUrlSafety(enabled)
    }

    fun setHistoryEnabled(enabled: Boolean) = viewModelScope.launch {
        settingsRepository.setHistoryEnabled(enabled)
    }

    fun setAdConsent(granted: Boolean) = viewModelScope.launch {
        settingsRepository.setAdConsentGranted(granted)
    }

    /** GDPR "erase my data": wipes the entire encrypted history. */
    fun deleteAllData() = viewModelScope.launch {
        historyRepository.clearAll()
    }
}
