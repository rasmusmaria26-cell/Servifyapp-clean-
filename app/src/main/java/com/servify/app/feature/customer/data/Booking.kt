package com.servify.app.feature.customer.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.servify.app.feature.customer.data.Service
import com.servify.app.feature.auth.domain.ProfileDto
import com.servify.app.feature.vendor.domain.Vendor
import com.servify.app.core.model.AIDiagnosis

@Serializable
data class Booking(
    val id: String,
    @SerialName("customer_id")
    val customerId: String,
    @SerialName("vendor_id")
    val vendorId: String?,
    @SerialName("service_id")
    val serviceId: String,
    @SerialName("services")
    val service: Service? = null,
    @SerialName("issue_description")
    val issueDescription: String,
    @SerialName("ai_diagnosis")
    val aiDiagnosis: AIDiagnosis?,
    @SerialName("image_urls")
    val imageUrls: List<String> = emptyList(),
    @SerialName("scheduled_date")
    val scheduledDate: String,
    @SerialName("scheduled_time")
    val scheduledTime: String,
    val address: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("estimated_cost")
    val estimatedPrice: Double?,
    @SerialName("final_cost")
    val finalPrice: Double?,
    val status: String, // PENDING, ACCEPTED, etc.
    @SerialName("payment_status")
    val paymentStatus: String, // PENDING, PAID
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("vendor")
    val vendorProfile: ProfileDto? = null,
    @SerialName("vendor_details")
    val vendorDetails: Vendor? = null
)


