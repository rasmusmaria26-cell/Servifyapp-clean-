package com.servify.app.feature.marketplace.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant

enum class QuoteStatus {
    PENDING, ACCEPTED, REJECTED
}

@Serializable
data class Quote(
    @SerialName("id") val id: String,
    @SerialName("request_id") val requestId: String,
    @SerialName("vendor_id") val vendorId: String,
    @SerialName("proposed_price") val proposedPrice: Double,
    @SerialName("estimated_duration_mins") val estimatedDurationMins: Int,
    @SerialName("vendor_notes") val vendorNotes: String? = null,
    @SerialName("status") val status: QuoteStatus = QuoteStatus.PENDING,
    @SerialName("created_at") val createdAt: Instant,
    @SerialName("updated_at") val updatedAt: Instant
)
