package com.servify.app.feature.vendor.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.servify.app.feature.customer.data.Booking
import com.servify.app.feature.vendor.domain.Vendor
import com.servify.app.feature.auth.data.AuthRepository
import com.servify.app.feature.customer.data.BookingRepository
import com.servify.app.feature.vendor.data.VendorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VendorDashboardViewModel @Inject constructor(
    private val vendorRepository: VendorRepository,
    private val bookingRepository: BookingRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VendorDashboardUiState())
    val uiState: StateFlow<VendorDashboardUiState> = _uiState.asStateFlow()

    private val _signedOut = MutableStateFlow(false)
    val signedOut: StateFlow<Boolean> = _signedOut.asStateFlow()

    init {
        loadVendorData()
    }

    fun loadVendorData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val user = authRepository.getCurrentUser()
                if (user != null) {
                    vendorRepository.getVendorByUserId(user.userId).onSuccess { vendor ->
                        if (vendor != null) {
                            _uiState.update { it.copy(vendor = vendor) }
                            fetchBookings(vendor.id)
                        } else {
                            _uiState.update { 
                                it.copy(isLoading = false, error = "Vendor profile not found for this account.") 
                            }
                        }
                    }.onFailure { error ->
                        _uiState.update { 
                            it.copy(isLoading = false, error = error.message ?: "Failed to fetch vendor profile") 
                        }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "User not logged in") }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isLoading = false, error = e.message ?: "An unexpected error occurred") 
                }
            }
        }
    }

    private fun fetchBookings(vendorId: String) {
        viewModelScope.launch {
            bookingRepository.getVendorBookings(vendorId)
                .onSuccess { bookings ->
                    val earnings = bookings.filter { it.status == "COMPLETED" }
                        .sumOf { it.finalPrice ?: it.estimatedPrice ?: 0.0 }
                    
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            bookings = bookings,
                            totalEarnings = earnings
                        ) 
                    }
                }
                .onFailure { error ->
                    _uiState.update { 
                        it.copy(isLoading = false, error = error.message ?: "Failed to fetch bookings") 
                    }
                }
        }
    }

    fun updateBookingStatus(bookingId: String, status: String) {
        viewModelScope.launch {
            bookingRepository.updateBookingStatus(bookingId, status)
                .onSuccess {
                    uiState.value.vendor?.let { fetchBookings(it.id) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(error = "Update failed: ${error.message}") }
                }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _signedOut.value = true
        }
    }
}

data class VendorDashboardUiState(
    val vendor: Vendor? = null,
    val bookings: List<Booking> = emptyList(),
    val totalEarnings: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null
)
