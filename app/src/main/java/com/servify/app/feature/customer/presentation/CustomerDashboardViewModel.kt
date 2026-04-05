package com.servify.app.feature.customer.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.servify.app.feature.customer.data.Booking
import com.servify.app.feature.customer.data.RepairRequest
import com.servify.app.feature.auth.domain.UserProfile
import com.servify.app.feature.auth.data.AuthRepository
import com.servify.app.feature.customer.data.BookingRepository
import com.servify.app.feature.customer.data.RepairRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerDashboardViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val authRepository: AuthRepository,
    private val repairRepository: RepairRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerDashboardUiState())
    val uiState: StateFlow<CustomerDashboardUiState> = _uiState.asStateFlow()

    // Sign-out event
    private val _signedOut = MutableStateFlow(false)
    val signedOut: StateFlow<Boolean> = _signedOut.asStateFlow()

    // Selected booking for detail screen
    var selectedBooking: Booking? = null
        private set

    init {
        fetchBookings()
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        viewModelScope.launch {
            try {
                val user = authRepository.getCurrentUser()
                _uiState.update { it.copy(userProfile = user) }
            } catch (_: Exception) { }
        }
    }

    fun fetchBookings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val user = authRepository.getCurrentUser()
                if (user != null) {
                    bookingRepository.getCustomerBookings(user.userId)
                        .onSuccess { bookings ->
                            _uiState.update { 
                                it.copy(
                                    isLoading = false, 
                                    bookings = bookings,
                                    userProfile = user
                                ) 
                            }
                        }
                        .onFailure { error ->
                            _uiState.update { 
                                it.copy(
                                    isLoading = false, 
                                    error = error.message ?: "Failed to fetch bookings"
                                ) 
                            }
                        }
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = "User not logged in"
                        ) 
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = e.message ?: "An unexpected error occurred"
                    ) 
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _signedOut.value = true
        }
    }

    fun selectBooking(booking: Booking) {
        selectedBooking = booking
    }

    fun fetchRepairRequests() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val user = authRepository.getCurrentUser() ?: return@launch
            repairRepository.getCustomerRequests(user.userId)
                .onSuccess { requests ->
                    _uiState.update { it.copy(isLoading = false, repairRequests = requests) }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoading = false) }
                }
        }
    }
}

data class CustomerDashboardUiState(
    val bookings: List<Booking> = emptyList(),
    val repairRequests: List<RepairRequest> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val userProfile: UserProfile? = null
)
