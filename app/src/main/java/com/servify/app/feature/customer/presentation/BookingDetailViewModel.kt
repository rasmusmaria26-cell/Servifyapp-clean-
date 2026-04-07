package com.servify.app.feature.customer.presentation

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

@HiltViewModel
class BookingDetailViewModel @Inject constructor(
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookingDetailUiState())
    val uiState: StateFlow<BookingDetailUiState> = _uiState.asStateFlow()

    private var observeJob: kotlinx.coroutines.Job? = null

    fun fetchBooking(bookingId: String) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                bookingRepository.observeBooking(bookingId).collect { booking ->
                    _uiState.update { it.copy(isLoading = false, booking = booking) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Failed to load booking: ${e.message}") }
            }
        }
    }

    fun cancelBooking() {
        val currentBookingId = _uiState.value.booking?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            bookingRepository.updateBookingStatus(currentBookingId, "CANCELLED")
                .onSuccess {
                    fetchBooking(currentBookingId)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = "Failed to cancel: ${error.message}") }
                }
        }
    }

    fun markBookingPaid(bookingId: String) {
        viewModelScope.launch {
            bookingRepository.confirmBookingPayment(bookingId)
                .onSuccess {
                    fetchBooking(bookingId)
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(error = "Failed to update payment status: ${error.message}")
                    }
                }
        }
    }
}

data class BookingDetailUiState(
    val booking: Booking? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)
