package com.servify.app.feature.customer.domain.usecase

import com.servify.app.core.model.AIDiagnosis
import com.servify.app.feature.customer.data.Booking
import com.servify.app.feature.customer.data.ServiceCategory
import com.servify.app.feature.auth.data.AuthRepository
import com.servify.app.feature.customer.data.BookingRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import javax.inject.Inject

class CreateBookingUseCase @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        serviceCategory: String,
        issueDescription: String,
        aiDiagnosis: AIDiagnosis? = null,
        scheduledDate: String,
        scheduledTime: String,
        address: String,
        latitude: Double? = null,
        longitude: Double? = null,
        vendorId: String?,
        estimatedPrice: Double?,
        imageUrls: List<String> = emptyList()
    ): Result<Booking> {
        val userProfile = authRepository.getCurrentUser()
            ?: return Result.failure(Exception("User not logged in"))

        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val createdAt = dateFormat.format(Date())

        // Fetch services and resolve the category name to a service ID
        val services = bookingRepository.getServices()
            .getOrElse { return Result.failure(it) }
        
        var service = services.find { it.name.equals(serviceCategory, ignoreCase = true) }
        
        if (service == null) {
            // If not found in services, try resolving via categories
            val categories = bookingRepository.getCategories()
                .getOrElse { return Result.failure(it) }
            
            val category = categories.find { it.name.equals(serviceCategory, ignoreCase = true) }
            
            if (category != null) {
                // Find first service in this category
                service = services.find { it.categoryId == category.id }
            }
        }

        if (service == null) {
            return Result.failure(Exception("Service category not found or no services available for: $serviceCategory"))
        }

        val booking = Booking(
            id = UUID.randomUUID().toString(),
            customerId = userProfile.userId,
            vendorId = vendorId,
            serviceId = service.id, // Use the resolved service ID
            issueDescription = issueDescription,
            aiDiagnosis = aiDiagnosis,
            scheduledDate = scheduledDate,
            scheduledTime = scheduledTime,
            address = address,
            latitude = latitude,
            longitude = longitude,
            estimatedPrice = estimatedPrice,
            finalPrice = null,
            imageUrls = imageUrls,
            status = "PENDING",
            paymentStatus = "PENDING",
            createdAt = createdAt
        )

        return bookingRepository.createBooking(booking)
    }
}
