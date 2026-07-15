package com.nextqr.scanner.presentation.generator

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextqr.scanner.data.qr.QrEncoder
import com.nextqr.scanner.domain.model.BarcodeType
import com.nextqr.scanner.domain.model.ErrorCorrection
import com.nextqr.scanner.domain.model.ModuleShape
import com.nextqr.scanner.domain.model.QrGenerationOptions
import com.nextqr.scanner.domain.repository.BillingRepository
import com.nextqr.scanner.domain.usecase.SaveScanUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class GeneratorUiState(
    val content: String = "",
    val options: QrGenerationOptions = QrGenerationOptions(content = ""),
    val bitmap: Bitmap? = null,
    val error: String? = null,
)

@HiltViewModel
class GeneratorViewModel @Inject constructor(
    private val encoder: QrEncoder,
    private val saveScan: SaveScanUseCase,
    billingRepository: BillingRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GeneratorUiState())
    val uiState: StateFlow<GeneratorUiState> = _uiState.asStateFlow()

    /** Customisation (colours, logo, module shape) is a premium feature. */
    val isPremium: StateFlow<Boolean> = billingRepository.premiumStatus
        .map { it.isPremium }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun onContentChange(text: String) {
        _uiState.value = _uiState.value.copy(
            content = text,
            options = _uiState.value.options.copy(content = text),
        )
        regenerate()
    }

    fun onForegroundColor(color: Int) = updateOptions { it.copy(foregroundColor = color) }
    fun onBackgroundColor(color: Int) = updateOptions { it.copy(backgroundColor = color) }
    fun onErrorCorrection(level: ErrorCorrection) = updateOptions { it.copy(errorCorrection = level) }
    fun onModuleShape(shape: ModuleShape) = updateOptions { it.copy(moduleShape = shape) }
    fun onLogo(bytes: ByteArray?) = updateOptions {
        // A logo needs high error correction to remain scannable.
        it.copy(logoPngBytes = bytes, errorCorrection = ErrorCorrection.HIGH)
    }

    private fun updateOptions(transform: (QrGenerationOptions) -> QrGenerationOptions) {
        _uiState.value = _uiState.value.copy(options = transform(_uiState.value.options))
        regenerate()
    }

    private fun regenerate() {
        val options = _uiState.value.options
        if (options.content.isBlank()) {
            _uiState.value = _uiState.value.copy(bitmap = null, error = null)
            return
        }
        viewModelScope.launch {
            runCatching { withContext(Dispatchers.Default) { encoder.encode(options) } }
                .onSuccess { _uiState.value = _uiState.value.copy(bitmap = it, error = null) }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message) }
        }
    }

    /** Persists the generated code into history. */
    fun saveToHistory() {
        val content = _uiState.value.options.content
        if (content.isBlank()) return
        viewModelScope.launch { saveScan(content, BarcodeType.QR_CODE, isGenerated = true) }
    }
}
