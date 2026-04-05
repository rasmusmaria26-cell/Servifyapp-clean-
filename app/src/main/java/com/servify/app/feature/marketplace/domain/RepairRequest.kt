package com.servify.app.feature.marketplace.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant

// Enums mapping Supabase text checks
enum class RequestStatus {
    OPEN, AWAITING_PAYMENT, ASSIGNED, COMPLETED, EXPIRED
}

enum class RequestSeverity {
    LOW, MODERATE, CRITICAL
}

@Serializable
data class RepairRequest(
    @SerialName("id") val id: String,
    @SerialName("customer_id") val customerId: String,
    @SerialName("accepted_quote_id") val acceptedQuoteId: String? = null,
    @SerialName("device_type") val deviceType: String,
    @SerialName("brand") val brand: String,
    @SerialName("model_name") val modelName: String,
    @SerialName("issue_description") val issueDescription: String,
    @SerialName("severity") val severity: RequestSeverity,
    @SerialName("media_urls") val mediaUrls: List<String> = emptyList(),
    @SerialName("status") val status: RequestStatus = RequestStatus.OPEN,
    @SerialName("quote_deadline") val quoteDeadline: Instant,
    @SerialName("created_at") val createdAt: Instant,
    @SerialName("updated_at") val updatedAt: Instant
)
