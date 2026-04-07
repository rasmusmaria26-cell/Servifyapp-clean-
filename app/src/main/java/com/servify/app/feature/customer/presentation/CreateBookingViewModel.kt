package com.servify.app.feature.customer.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.graphics.Bitmap
import com.servify.app.core.model.AIDiagnosis
import com.servify.app.core.network.GeminiApiClient
import com.servify.app.feature.customer.data.Booking
import com.servify.app.feature.customer.data.BookingRepository
import com.servify.app.feature.customer.domain.usecase.CreateBookingUseCase
import com.servify.app.feature.vendor.domain.Vendor
import com.servify.app.feature.vendor.domain.usecase.GetMatchedVendorsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class CreateBookingViewModel @Inject constructor(
    private val getMatchedVendorsUseCase: GetMatchedVendorsUseCase,
    private val createBookingUseCase: CreateBookingUseCase,
    private val bookingRepository: BookingRepository,
    private val geminiApiClient: GeminiApiClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateBookingUiState())
    val uiState: StateFlow<CreateBookingUiState> = _uiState.asStateFlow()


    fun onDescriptionChange(description: String) {
        _uiState.update { it.copy(issueDescription = description) }
    }

    fun onImagesSelected(bitmaps: List<Bitmap>) {
        _uiState.update { it.copy(selectedBitmaps = bitmaps) }
    }

    fun onServiceCategorySelected(category: String) {
        _uiState.update { it.copy(selectedServiceCategory = category) }
    }

    fun onVendorSelected(vendor: Vendor) {
        _uiState.update { it.copy(selectedVendor = vendor) }
    }

    fun onDateSelected(date: String) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun onTimeSelected(time: String) {
        _uiState.update { it.copy(selectedTime = time) }
    }

    fun onAddressChange(address: String) {
        _uiState.update { it.copy(address = address) }
    }
    
    fun onLocationSelected(lat: Double, lng: Double) {
        _uiState.update { it.copy(latitude = lat, longitude = lng) }
    }

    fun onNextStep() {
        val nextStep = _uiState.value.currentStep + 1
        _uiState.update { it.copy(currentStep = nextStep) }
        
        when (nextStep) {
            2 -> {
                fetchVendors()
                fetchDiagnosis()
            }
        }
    }

    private fun fetchVendors() {
        viewModelScope.launch {
            val category = _uiState.value.selectedServiceCategory ?: "Electronics"
            _uiState.update { it.copy(isLoadingVendors = true, error = null) }
            
            getMatchedVendorsUseCase(category)
                .onSuccess { vendors ->
                    _uiState.update { 
                        it.copy(
                            isLoadingVendors = false, 
                            matchedVendors = vendors
                        ) 
                    }
                }
                .onFailure { error ->
                    _uiState.update { 
                        it.copy(
                            isLoadingVendors = false, 
                            error = "Failed to load vendors: ${error.message}"
                        ) 
                    }
                }
        }
    }

    private fun fetchDiagnosis() {
        val state = _uiState.value
        val description = state.issueDescription
        val category = state.selectedServiceCategory ?: "General Repair"

        if (description.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingDiagnosis = true, diagnosisError = null) }

            geminiApiClient.getDiagnosis(
                description = description,
                images = _uiState.value.selectedBitmaps,
                serviceCategory = category
            ).onSuccess { diagnosis ->
                _uiState.update {
                    it.copy(
                        isLoadingDiagnosis = false,
                        aiDiagnosis = diagnosis
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoadingDiagnosis = false,
                        diagnosisError = "Diagnosis unavailable — a technician will assess on arrival."
                    )
                }
            }
        }
    }

    fun onConfirmBooking() {
        val currentState = _uiState.value
        if (currentState.isCreatingBooking || currentState.bookingCreated) return

        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingBooking = true, error = null) }

            // Upload bitmaps to Supabase storage and collect URLs
            val imageUrls = mutableListOf<String>()
            currentState.selectedBitmaps.forEachIndexed { index, bitmap ->
                val out = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
                bookingRepository.uploadBookingImage(
                    bytes = out.toByteArray(),
                    fileName = "photo_$index.jpg"
                ).onSuccess { url -> imageUrls.add(url) }
            }

            createBookingUseCase(
                serviceCategory = currentState.selectedServiceCategory ?: "AC Repair",
                issueDescription = currentState.issueDescription,
                aiDiagnosis = currentState.aiDiagnosis,
                scheduledDate = currentState.selectedDate,
                scheduledTime = currentState.selectedTime,
                address = currentState.address,
                latitude = currentState.latitude ?: 28.6139,
                longitude = currentState.longitude ?: 77.2090,
                vendorId = currentState.selectedVendor?.id,
                estimatedPrice = currentState.aiDiagnosis?.estimatedCost
                    ?.replace(Regex("[^0-9]"), "")
                    ?.split("-")
                    ?.firstOrNull()
                    ?.trim()
                    ?.toDoubleOrNull(),
                imageUrls = imageUrls
            ).onSuccess { booking ->
                _uiState.update {
                    it.copy(
                        isCreatingBooking = false,
                        bookingCreated = true
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isCreatingBooking = false,
                        error = "Booking failed: ${error.message}"
                    )
                }
            }
        }
    }
}

data class CreateBookingUiState(
    val currentStep: Int = 1,
    val issueDescription: String = "",
    val selectedServiceCategory: String? = null,
    val selectedBitmaps: List<Bitmap> = emptyList(),
    
    val isLoadingDiagnosis: Boolean = false,
    val aiDiagnosis: AIDiagnosis? = null,
    val diagnosisError: String? = null,
    
    val isLoadingVendors: Boolean = false,
    val matchedVendors: List<Vendor> = emptyList(),
    val selectedVendor: Vendor? = null,
    
    val selectedDate: String = "",
    val selectedTime: String = "",
    val address: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    
    val isCreatingBooking: Boolean = false,
    val bookingCreated: Boolean = false,
    val error: String? = null
)
