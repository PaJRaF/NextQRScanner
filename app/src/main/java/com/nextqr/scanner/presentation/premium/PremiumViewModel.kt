package com.nextqr.scanner.presentation.premium

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextqr.scanner.domain.model.PremiumProduct
import com.nextqr.scanner.domain.model.PremiumStatus
import com.nextqr.scanner.domain.repository.BillingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val billingRepository: BillingRepository,
) : ViewModel() {

    val products: StateFlow<List<PremiumProduct>> = billingRepository.products
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val status: StateFlow<PremiumStatus> = billingRepository.premiumStatus
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PremiumStatus.FREE)

    init {
        viewModelScope.launch { billingRepository.refresh() }
    }

    fun purchase(product: PremiumProduct) = viewModelScope.launch {
        billingRepository.purchase(product.productId)
    }
}
