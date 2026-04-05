package com.servify.app.feature.customer.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.servify.app.core.model.AIDiagnosis
import com.servify.app.feature.customer.data.RepairRequest
import com.servify.app.core.network.GeminiApiClient
import com.servify.app.feature.auth.data.AuthRepository
import com.servify.app.feature.customer.data.RepairRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import javax.inject.Inject

data class PostRepairUiState(
    val currentStep: Int = 1,        // 1, 2, or 3
    val completedSteps: Set<Int> = emptySet(),
    
    // Form fields
    val deviceType: String = "",
    val brand: String = "",
    val model: String = "",
    val issueCategory: String = "",
    val severity: String = "MODERATE",
    val description: String = "",
    val mediaUris: List<Uri> = emptyList(),

    // Location
    val latitude: Double? = null,
    val longitude: Double? = null,
    val address: String = "",

    // Submission state
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val submittedRequestId: String? = null,   // non-null → success

    // AI Diagnosis state
    val diagnosis: AIDiagnosis? = null,
    val isDiagnosing: Boolean = false,
    val diagnosisError: String? = null
)

@HiltViewModel
class PostRepairRequestViewModel @Inject constructor(
    private val repairRepository: RepairRepository,
    private val authRepository: AuthRepository,
    private val geminiApiClient: GeminiApiClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(PostRepairUiState())
    val uiState: StateFlow<PostRepairUiState> = _uiState.asStateFlow()

    fun goToStep(step: Int) = _uiState.update { 
        it.copy(currentStep = step.coerceIn(1, 3)) 
    }

    fun nextStep() = _uiState.update {
        it.copy(
            currentStep = (it.currentStep + 1).coerceAtMost(3),
            completedSteps = it.completedSteps + it.currentStep
        )
    }

    fun prevStep() = _uiState.update {
        it.copy(currentStep = (it.currentStep - 1).coerceAtLeast(1))
    }

    fun initFromCategory(category: String?) {
        if (category == null || category == "More") {
            // Keep Step 1 logic
            return
        }
        // If we haven't advanced yet, immediately jump to step 2 with the selected category
        val current = _uiState.value
        if (current.currentStep <= 1 && current.deviceType.isBlank()) {
            _uiState.update {
                it.copy(
                    deviceType = category,
                    currentStep = 2,
                    completedSteps = setOf(1)
                )
            }
        }
    }

    fun isCurrentStepValid(): Boolean {
        val s = _uiState.value
        return when (s.currentStep) {
            1 -> s.deviceType.isNotBlank()
            2 -> s.issueCategory.isNotBlank()
            3 -> s.description.isNotBlank()
            else -> false
        }
    }

    // ── Form field mutators ───────────────────────────────────────────────

    fun onDeviceType(v: String)     = _uiState.update { it.copy(deviceType = v) }
    fun onBrand(v: String)          = _uiState.update { it.copy(brand = v) }
    fun onModel(v: String)          = _uiState.update { it.copy(model = v) }
    fun onIssueCategory(v: String)  = _uiState.update { it.copy(issueCategory = v) }
    fun onSeverity(v: String)       = _uiState.update { it.copy(severity = v) }
    fun onDescription(v: String)    = _uiState.update { it.copy(description = v) }
    fun onAddress(v: String)        = _uiState.update { it.copy(address = v) }
    fun onLocationSelected(lat: Double, lng: Double) = _uiState.update {
        it.copy(latitude = lat, longitude = lng)
    }

    fun addMediaUri(uri: Uri) = _uiState.update {
        it.copy(mediaUris = it.mediaUris + uri)
    }

    fun removeMediaUri(uri: Uri) = _uiState.update {
        it.copy(mediaUris = it.mediaUris - uri)
    }

    // ── Submission ────────────────────────────────────────────────────────

    fun submit() {
        val s = _uiState.value
        if (s.deviceType.isBlank() || s.issueCategory.isBlank() || s.description.isBlank()) {
            _uiState.update { it.copy(error = "Please fill in all required fields.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }

            // Resolve the current user id (auth UUID — must match auth.uid() for RLS)
            val userId = authRepository.getCurrentUser()?.userId
            if (userId == null) {
                _uiState.update { it.copy(isSubmitting = false, error = "Not logged in.") }
                return@launch
            }

            // Compute the 60-minute bidding deadline (ISO-8601 UTC)
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }
            val deadline = sdf.format(java.util.Date(System.currentTimeMillis() + 60 * 60 * 1000L))

            val request = RepairRequest(
                customerId    = userId,
                deviceType    = s.deviceType,
                brand         = s.brand,
                model         = s.model,
                issueCategory = s.issueCategory,
                severity      = s.severity,
                description   = s.description,
                quoteDeadline = deadline,
                latitude      = s.latitude ?: 28.6139,  // default if map unchanged
                longitude     = s.longitude ?: 77.2090,
                address       = s.address
                // mediaUrls populated separately after image upload
            )

            repairRepository.createRepairRequest(request)
                .onSuccess { created ->
                    // Set submittedRequestId immediately (navigation key) and start diagnosing
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            submittedRequestId = created.id,
                            isDiagnosing = true
                        )
                    }

                    // Run AI diagnosis against the submitted description & category
                    geminiApiClient.getDiagnosis(
                        description = s.description,
                        serviceCategory = s.issueCategory
                    )
                        .onSuccess { result ->
                            _uiState.update { it.copy(isDiagnosing = false, diagnosis = result) }
                        }
                        .onFailure { e ->
                            _uiState.update {
                                it.copy(
                                    isDiagnosing = false,
                                    diagnosisError = e.message ?: "Diagnosis unavailable."
                                )
                            }
                        }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isSubmitting = false, error = e.message ?: "Submission failed.") }
                }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
