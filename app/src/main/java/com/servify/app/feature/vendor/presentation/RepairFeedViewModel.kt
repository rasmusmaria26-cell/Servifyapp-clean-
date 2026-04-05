package com.servify.app.feature.vendor.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.servify.app.feature.customer.data.Quote
import com.servify.app.feature.customer.data.RepairRequest
import com.servify.app.feature.auth.data.AuthRepository
import com.servify.app.feature.customer.data.RepairRepository
import com.servify.app.feature.vendor.data.VendorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RepairFeedUiState(
    val requests: List<RepairRequest> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class SubmitQuoteUiState(
    val price: String = "",
    val estimatedTime: String = "",
    val warrantyDays: String = "30",
    val pickupAvailable: Boolean = false,
    val vendorNote: String = "",
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val submitted: Boolean = false
)

@HiltViewModel
class RepairFeedViewModel @Inject constructor(
    private val repairRepository: RepairRepository,
    private val vendorRepository: VendorRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    // ── Feed state ────────────────────────────────────────────────────────
    private val _feedState = MutableStateFlow(RepairFeedUiState())
    val feedState: StateFlow<RepairFeedUiState> = _feedState.asStateFlow()

    // ── Quote submission state ────────────────────────────────────────────
    private val _quoteState = MutableStateFlow(SubmitQuoteUiState())
    val quoteState: StateFlow<SubmitQuoteUiState> = _quoteState.asStateFlow()

    // The request the vendor is currently viewing / quoting
    var selectedRequest: RepairRequest? = null
        private set

    init { fetchOpenRequests() }

    // ── Feed ──────────────────────────────────────────────────────────────

    fun fetchOpenRequests() {
        viewModelScope.launch {
            _feedState.update { it.copy(isLoading = true, error = null) }
            repairRepository.getOpenRequests()
                .onSuccess { list ->
                    _feedState.update { it.copy(requests = list, isLoading = false) }
                }
                .onFailure { e ->
                    _feedState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun selectRequest(request: RepairRequest) {
        selectedRequest = request
        // Reset the quote form whenever a new request is selected
        _quoteState.value = SubmitQuoteUiState()
    }

    // ── Quote form mutators ───────────────────────────────────────────────

    fun onPrice(v: String)         = _quoteState.update { it.copy(price = v) }
    fun onEstimatedTime(v: String) = _quoteState.update { it.copy(estimatedTime = v) }
    fun onWarrantyDays(v: String)  = _quoteState.update { it.copy(warrantyDays = v) }
    fun onPickup(v: Boolean)       = _quoteState.update { it.copy(pickupAvailable = v) }
    fun onVendorNote(v: String)    = _quoteState.update { it.copy(vendorNote = v) }

    // ── Quote submission ──────────────────────────────────────────────────

    /** Resolves vendor ID from auth session automatically — no parameter needed. */
    fun submitQuote() {
        val req = selectedRequest ?: return
        val s = _quoteState.value
        val price = s.price.toDoubleOrNull()
        if (price == null || price <= 0) {
            _quoteState.update { it.copy(error = "Please enter a valid price.") }
            return
        }
        if (s.estimatedTime.isBlank()) {
            _quoteState.update { it.copy(error = "Please specify estimated repair time.") }
            return
        }

        viewModelScope.launch {
            _quoteState.update { it.copy(isSubmitting = true, error = null) }

            val authUserId = authRepository.getCurrentUser()?.userId
            if (authUserId == null) {
                _quoteState.update { it.copy(isSubmitting = false, error = "Not logged in.") }
                return@launch
            }

            // Auto-creates a vendors row if this vendor doesn't have one yet (satisfies FK constraint)
            val vendorId = vendorRepository.getOrCreateVendorProfile(authUserId)
                .getOrNull()?.id
            if (vendorId == null) {
                _quoteState.update { it.copy(isSubmitting = false, error = "Could not set up vendor profile. Try again.") }
                return@launch
            }

            val quote = Quote(
                requestId       = req.id,
                vendorId        = vendorId,     // vendors.id satisfying FK constraint
                price           = price,
                estimatedTime   = s.estimatedTime,
                warrantyDays    = s.warrantyDays.toIntOrNull() ?: 0,
                pickupAvailable = s.pickupAvailable,
                vendorNote      = s.vendorNote.takeIf { it.isNotBlank() },
                expiresAt       = req.quoteDeadline
            )
            repairRepository.submitQuote(quote)
                .onSuccess {
                    _quoteState.update { it.copy(isSubmitting = false, submitted = true) }
                }
                .onFailure { e ->
                    _quoteState.update { it.copy(isSubmitting = false, error = e.message ?: "Submission failed.") }
                }
        }
    }

    fun clearQuoteError() = _quoteState.update { it.copy(error = null) }
}
