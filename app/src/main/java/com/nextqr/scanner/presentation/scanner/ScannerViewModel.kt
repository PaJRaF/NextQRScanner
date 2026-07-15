package com.nextqr.scanner.presentation.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextqr.scanner.data.ads.AdsManager
import com.nextqr.scanner.domain.model.BarcodeType
import com.nextqr.scanner.domain.model.ParsedContent
import com.nextqr.scanner.domain.model.UrlSafety
import com.nextqr.scanner.domain.repository.SettingsRepository
import com.nextqr.scanner.domain.usecase.ClassifyContentUseCase
import com.nextqr.scanner.domain.usecase.SaveScanUseCase
import com.nextqr.scanner.domain.usecase.VerifyUrlUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Immutable UI state for the scanner screen. */
data class ScannerUiState(
    val isProcessing: Boolean = false,
    val batchMode: Boolean = false,
    val torchOn: Boolean = false,
    val lastResult: ScanUiResult? = null,
    val batchResults: List<ScanUiResult> = emptyList(),
)

/** A resolved scan ready to present (classification + optional URL verdict). */
data class ScanUiResult(
    val savedId: Long?,
    val rawValue: String,
    val barcodeType: BarcodeType,
    val parsed: ParsedContent,
    val urlSafety: UrlSafety? = null,
)

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val classifyContent: ClassifyContentUseCase,
    private val verifyUrl: VerifyUrlUseCase,
    private val saveScan: SaveScanUseCase,
    private val adsManager: AdsManager,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    val vibrateEnabled: StateFlow<Boolean> = settingsRepository.settings
        .map { it.vibrateOnScan }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    private val adConsent: StateFlow<Boolean> = settingsRepository.settings
        .map { it.adConsentGranted }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    init {
        // Initialise the ads SDK once; personalised requests are gated on consent.
        adsManager.initialize(personalizedAllowed = adConsent.value)
    }

    // Debounce identical decodes so a barcode held in frame is handled once.
    @Volatile
    private var lastHandledValue: String? = null

    fun onBarcodeDetected(rawValue: String, type: BarcodeType) {
        if (_uiState.value.isProcessing) return
        if (rawValue == lastHandledValue && !_uiState.value.batchMode) return
        lastHandledValue = rawValue

        _uiState.value = _uiState.value.copy(isProcessing = true)
        viewModelScope.launch {
            val parsed = classifyContent(rawValue)
            val urlSafety = (parsed as? ParsedContent.Url)?.let { verifyUrl(it.url) }
            val savedId = saveScan(rawValue, type)

            val result = ScanUiResult(savedId, rawValue, type, parsed, urlSafety)
            _uiState.value = if (_uiState.value.batchMode) {
                _uiState.value.copy(
                    isProcessing = false,
                    batchResults = _uiState.value.batchResults + result,
                )
            } else {
                _uiState.value.copy(isProcessing = false, lastResult = result)
            }
            adsManager.onScanCompleted(adConsent.value)
        }
    }

    fun toggleTorch() {
        _uiState.value = _uiState.value.copy(torchOn = !_uiState.value.torchOn)
    }

    fun toggleBatchMode() {
        _uiState.value = _uiState.value.copy(
            batchMode = !_uiState.value.batchMode,
            batchResults = emptyList(),
        )
    }

    fun dismissResult() {
        lastHandledValue = null
        _uiState.value = _uiState.value.copy(lastResult = null)
    }
}
