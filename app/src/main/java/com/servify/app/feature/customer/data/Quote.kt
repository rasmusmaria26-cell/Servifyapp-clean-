package com.servify.app.feature.customer.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.servify.app.feature.vendor.domain.Vendor

// Lifecycle of a vendor quote
// PENDING  → submitted, waiting for user decision
// ACCEPTED → user selected this quote
// REJECTED → user chose another vendor
// EXPIRED  → 60-minute window passed without user action

@Serializable
data class Quote(
    val id: String = "",

    @SerialName("request_id")
    val requestId: String,

    @SerialName("vendor_id")
    val vendorId: String,

    // The vendor profile, eagerly loaded from Supabase join
    val vendor: Vendor? = null,

    val price: Double,                     // Fixed bid price in INR

    @SerialName("estimated_time")
    val estimatedTime: String,             // e.g. "2 hours", "1-2 days"

    @SerialName("warranty_days")
    val warrantyDays: Int,                 // 0 = no warranty

    @SerialName("pickup_available")
    val pickupAvailable: Boolean = false,  // true if vendor can collect device

    @SerialName("vendor_note")
    val vendorNote: String? = null,        // optional message from vendor

    val status: String = "PENDING",        // See lifecycle above

    @SerialName("expires_at")
    val expiresAt: String? = null,         // ISO-8601, same as request.quoteDeadline

    @SerialName("created_at")
    val createdAt: String = ""
)
