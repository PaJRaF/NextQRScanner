package com.nextqr.scanner.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextqr.scanner.domain.model.ParsedContent
import com.nextqr.scanner.domain.model.ScanResult
import com.nextqr.scanner.domain.repository.ScanHistoryRepository
import com.nextqr.scanner.domain.usecase.ClassifyContentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScanDetailUiState(
    val result: ScanResult? = null,
    val parsed: ParsedContent? = null,
)

@HiltViewModel
class ScanDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ScanHistoryRepository,
    private val classify: ClassifyContentUseCase,
) : ViewModel() {

    private val scanId: Long = savedStateHandle.get<Long>("scanId") ?: -1L

    private val _uiState = MutableStateFlow(ScanDetailUiState())
    val uiState: StateFlow<ScanDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getById(scanId)?.let { result ->
                _uiState.value = ScanDetailUiState(result, classify(result.rawValue))
            }
        }
    }

    fun toggleFavorite() {
        val current = _uiState.value.result ?: return
        viewModelScope.launch {
            repository.setFavorite(current.id, !current.isFavorite)
            _uiState.value = _uiState.value.copy(
                result = current.copy(isFavorite = !current.isFavorite),
            )
        }
    }
}
