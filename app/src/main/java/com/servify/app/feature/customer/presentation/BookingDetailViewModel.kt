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

    fun fetchBooking(bookingId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            bookingRepository.getBookingById(bookingId)
                .onSuccess { booking ->
                    _uiState.update { it.copy(isLoading = false, booking = booking) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message ?: "Failed to load booking") }
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
}

data class BookingDetailUiState(
    val booking: Booking? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)
