package com.servify.app.feature.customer.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.servify.app.feature.customer.data.Quote
import com.servify.app.feature.customer.data.RepairRequest
import com.servify.app.feature.vendor.domain.Vendor
import com.servify.app.feature.customer.data.RepairRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ActiveRepairUiState(
    val request: RepairRequest? = null,
    val acceptedQuote: Quote? = null,
    val vendor: Vendor? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showRatingDialog: Boolean = false,
    val ratingSubmitted: Boolean = false
)

@HiltViewModel
class ActiveRepairViewModel @Inject constructor(
    private val repairRepository: RepairRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActiveRepairUiState())
    val uiState: StateFlow<ActiveRepairUiState> = _uiState.asStateFlow()

    fun load(requestId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // Fetch the request
                val requestResult = repairRepository.getRequestById(requestId)
                val request = requestResult.getOrNull()
                if (request == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Request not found.") }
                    return@launch
                }

                // Fetch the accepted quote (with vendor details)
                val acceptedQuoteId = request.acceptedQuoteId
                if (acceptedQuoteId != null) {
                    val quotesResult = repairRepository.getQuotesForRequest(requestId)
                    val accepted = quotesResult.getOrNull()
                        ?.firstOrNull { it.id == acceptedQuoteId }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            request = request,
                            acceptedQuote = accepted,
                            vendor = accepted?.vendor
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, request = request) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    /** Polls the DB every 15 s for status changes (lightweight alternative to Realtime) */
    fun startPolling(requestId: String) {
        viewModelScope.launch {
            while (true) {
                delay(15_000)
                val result = repairRepository.getRequestById(requestId)
                result.getOrNull()?.let { updated ->
                    _uiState.update { it.copy(request = updated) }
                    // When vendor marks COMPLETED → prompt customer to rate
                    if (updated.status == "COMPLETED" && !it.showRatingDialog && !it.ratingSubmitted) {
                        _uiState.update { s -> s.copy(showRatingDialog = true) }
                    }
                }
            }
        }
    }

    private val it get() = _uiState.value

    fun dismissRatingDialog() = _uiState.update { it.copy(showRatingDialog = false) }
    fun onRatingSubmitted()   = _uiState.update { it.copy(showRatingDialog = false, ratingSubmitted = true) }
}
