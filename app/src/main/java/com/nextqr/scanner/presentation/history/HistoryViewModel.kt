package com.nextqr.scanner.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextqr.scanner.domain.model.ScanContentType
import com.nextqr.scanner.domain.model.ScanResult
import com.nextqr.scanner.domain.repository.BillingRepository
import com.nextqr.scanner.domain.repository.ScanHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryFilter(
    val query: String = "",
    val type: ScanContentType? = null,
    val favoritesOnly: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: ScanHistoryRepository,
    billingRepository: BillingRepository,
) : ViewModel() {

    private val _filter = MutableStateFlow(HistoryFilter())
    val filter: StateFlow<HistoryFilter> = _filter.asStateFlow()

    val isPremium: StateFlow<Boolean> = billingRepository.premiumStatus
        .map { it.isPremium }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    /**
     * The free tier can browse everything but only the newest
     * [FREE_HISTORY_LIMIT] entries are retained/shown; premium is unlimited.
     */
    val items: StateFlow<List<ScanResult>> =
        combine(_filter, isPremium) { filter, premium -> filter to premium }
            .flatMapLatest { (filter, premium) ->
                repository.observeHistory(
                    query = filter.query,
                    typeFilter = filter.type,
                    favoritesOnly = filter.favoritesOnly,
                ).map { list -> if (premium) list else list.take(FREE_HISTORY_LIMIT) }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onQueryChange(query: String) {
        _filter.value = _filter.value.copy(query = query)
    }

    fun onTypeFilter(type: ScanContentType?) {
        _filter.value = _filter.value.copy(type = type)
    }

    fun onFavoritesToggle() {
        _filter.value = _filter.value.copy(favoritesOnly = !_filter.value.favoritesOnly)
    }

    fun toggleFavorite(item: ScanResult) {
        viewModelScope.launch { repository.setFavorite(item.id, !item.isFavorite) }
    }

    fun delete(item: ScanResult) {
        viewModelScope.launch { repository.delete(item.id) }
    }

    companion object {
        const val FREE_HISTORY_LIMIT = 50
    }
}
