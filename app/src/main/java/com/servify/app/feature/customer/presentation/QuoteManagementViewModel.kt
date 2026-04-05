package com.servify.app.feature.customer.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.servify.app.feature.customer.data.Quote
import com.servify.app.feature.customer.data.RepairRequest
import com.servify.app.feature.auth.data.AuthRepository
import com.servify.app.feature.customer.data.RepairRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class QuoteSortOrder { PRICE, RATING, TIME }

data class QuoteManagementUiState(
    val request: RepairRequest? = null,
    val quotes: List<Quote> = emptyList(),
    val sortOrder: QuoteSortOrder = QuoteSortOrder.PRICE,
    val isLoading: Boolean = false,
    val isAccepting: Boolean = false,
    val error: String? = null,
    val accepted: Boolean = false   // true → navigate user away on success
)

@HiltViewModel
class QuoteManagementViewModel @Inject constructor(
    private val repairRepository: RepairRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuoteManagementUiState())
    val uiState: StateFlow<QuoteManagementUiState> = _uiState.asStateFlow()

    fun load(requestId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Fetch quotes for this request
            repairRepository.getQuotesForRequest(requestId)
                .onSuccess { quotes ->
                    _uiState.update { it.copy(isLoading = false, quotes = sortedQuotes(quotes, it.sortOrder)) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun setSortOrder(order: QuoteSortOrder) {
        _uiState.update { it.copy(sortOrder = order, quotes = sortedQuotes(it.quotes, order)) }
    }

    fun acceptQuote(requestId: String, quoteId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAccepting = true, error = null) }
            repairRepository.acceptQuote(requestId, quoteId)
                .onSuccess {
                    _uiState.update { it.copy(isAccepting = false, accepted = true) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isAccepting = false, error = e.message ?: "Failed to accept quote.") }
                }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    private fun sortedQuotes(quotes: List<Quote>, order: QuoteSortOrder): List<Quote> = when (order) {
        QuoteSortOrder.PRICE  -> quotes.sortedBy { it.price }
        QuoteSortOrder.RATING -> quotes.sortedByDescending { it.vendor?.rating ?: 0.0 }
        QuoteSortOrder.TIME   -> quotes.sortedBy { it.estimatedTime }
    }
}
