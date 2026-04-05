package com.servify.app.feature.vendor.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

data class MyJobsUiState(
    val jobs: List<RepairRequest> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val updatingJobId: String? = null   // job currently being status-updated
)

@HiltViewModel
class MyJobsViewModel @Inject constructor(
    private val repairRepository: RepairRepository,
    private val vendorRepository: VendorRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyJobsUiState())
    val uiState: StateFlow<MyJobsUiState> = _uiState.asStateFlow()

    fun loadJobs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val authUserId = authRepository.getCurrentUser()?.userId
            if (authUserId == null) {
                _uiState.update { it.copy(isLoading = false, error = "Not logged in.") }
                return@launch
            }
            // Auto-create vendor row if needed (same as submitQuote), then query by vendors.id
            val vendorId = vendorRepository.getOrCreateVendorProfile(authUserId).getOrNull()?.id
            if (vendorId == null) {
                _uiState.update { it.copy(isLoading = false, error = "Could not resolve vendor profile.") }
                return@launch
            }
            repairRepository.getVendorJobs(vendorId)
                .onSuccess { jobs ->
                    _uiState.update { it.copy(isLoading = false, jobs = jobs) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    /**
     * Called by vendor to advance the job status:
     *  ACCEPTED → IN_REPAIR → COMPLETED
     */
    fun advanceStatus(requestId: String, currentStatus: String) {
        val nextStatus = when (currentStatus) {
            "ACCEPTED"  -> "IN_REPAIR"
            "IN_REPAIR" -> "COMPLETED"
            else        -> return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(updatingJobId = requestId) }
            repairRepository.updateRequestStatus(requestId, nextStatus)
                .onSuccess { loadJobs() }
                .onFailure { e ->
                    _uiState.update { it.copy(updatingJobId = null, error = e.message) }
                }
            _uiState.update { it.copy(updatingJobId = null) }
        }
    }
}
