package com.servify.app.feature.customer.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Severity levels for a repair request
enum class RepairSeverity { MINOR, MODERATE, SEVERE }

// Lifecycle of a repair request
// OPEN      → vendors can see and quote
// QUOTED    → at least one quote received
// ACCEPTED  → user accepted a quote, escrow funded
// IN_REPAIR → vendor has picked up / started work
// COMPLETED → user confirmed completion
// DISPUTED  → user raised a dispute
// CANCELLED → user cancelled before acceptance

@Serializable
data class RepairRequest(
    val id: String = "",

    @SerialName("customer_id")
    val customerId: String,

    @SerialName("device_type")
    val deviceType: String,           // e.g. "Smartphone", "Laptop", "AC"

    val brand: String,                // e.g. "Apple", "Samsung"
    val model: String,                // e.g. "iPhone 14", "Galaxy S23"

    @SerialName("issue_category")
    val issueCategory: String,        // e.g. "Screen", "Battery", "Software"

    val severity: String,             // RepairSeverity.name — MINOR / MODERATE / SEVERE

    @SerialName("media_urls")
    val mediaUrls: List<String> = emptyList(),   // Supabase Storage URLs

    val description: String,

    val status: String = "OPEN",      // See lifecycle above

    @SerialName("quote_deadline")
    val quoteDeadline: String? = null, // ISO-8601 timestamp, 60 min after creation

    @SerialName("accepted_quote_id")
    val acceptedQuoteId: String? = null,

    val latitude: Double? = null,
    val longitude: Double? = null,
    val address: String? = null,

    @SerialName("created_at")
    val createdAt: String = ""
)
