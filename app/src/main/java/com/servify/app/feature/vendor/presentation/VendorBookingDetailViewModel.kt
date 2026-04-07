package com.servify.app.feature.vendor.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.servify.app.feature.customer.data.Booking
import com.servify.app.feature.customer.data.BookingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VendorBookingDetailUiState(
    val booking: Booking? = null,
    val isLoading: Boolean = false,
    val proposedPrice: String = "",
    val isSubmitting: Boolean = false,
    val successMessage: String? = null,
    val error: String? = null
)

@HiltViewModel
class VendorBookingDetailViewModel @Inject constructor(
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VendorBookingDetailUiState())
    val uiState: StateFlow<VendorBookingDetailUiState> = _uiState.asStateFlow()

    fun loadBooking(bookingId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            bookingRepository.getBookingById(bookingId)
                .onSuccess { booking ->
                    _uiState.update { it.copy(isLoading = false, booking = booking) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load booking: ${e.message}") }
                }
        }
    }

    fun onProposedPriceChange(price: String) {
        _uiState.update { it.copy(proposedPrice = price) }
    }

    fun submitProposal() {
        val state = _uiState.value
        val bookingId = state.booking?.id ?: return
        val price = state.proposedPrice.toDoubleOrNull()
        if (price == null || price <= 0) {
            _uiState.update { it.copy(error = "Please enter a valid price.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            bookingRepository.proposePriceForBooking(bookingId, price)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            successMessage = "Proposal sent! Waiting for customer approval.",
                            booking = it.booking?.copy(status = "PRICE_PROPOSED", finalPrice = price)
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isSubmitting = false, error = "Failed to submit: ${e.message}") }
                }
        }
    }

    fun markAsCompleted() {
        val bookingId = _uiState.value.booking?.id ?: return
        viewModelScope.launch {
            bookingRepository.updateBookingStatus(bookingId, "COMPLETED")
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            booking = it.booking?.copy(status = "COMPLETED"),
                            successMessage = "Booking marked as completed!"
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = "Failed: ${e.message}") }
                }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(successMessage = null, error = null) }
    }
}
