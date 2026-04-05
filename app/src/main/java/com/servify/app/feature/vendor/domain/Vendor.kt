package com.servify.app.feature.vendor.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Vendor(
    val id: String,
    @SerialName("business_name")
    val businessName: String,
    @SerialName("service_categories")
    val serviceCategories: List<String>,
    @SerialName("hourly_rate")
    val hourlyRate: Double,
    val rating: Double,
    @SerialName("total_reviews")
    val totalReviews: Int,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("service_radius_km")
    val serviceRadiusKm: Double,
    @SerialName("is_verified")
    val isVerified: Boolean,
    @SerialName("is_available")
    val isAvailable: Boolean,
    val description: String? = null,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    val pincode: String? = null,
    @SerialName("kyc_status")
    val kycStatus: String? = null,
    @SerialName("total_jobs")
    val totalJobs: Int? = 0,
    val phone: String? = null,

    // ── Trust & Performance Metrics ──────────────────────────────────────
    @SerialName("completion_rate")
    val completionRate: Double? = null,    // 0.0–1.0 (e.g. 0.95 = 95%)

    @SerialName("response_rate")
    val responseRate: Double? = null,      // % of requests responded within window

    @SerialName("performance_score")
    val performanceScore: Double? = null,  // Composite score used for ranking (0–100)

    @SerialName("avg_response_minutes")
    val avgResponseMinutes: Int? = null    // Average quote submission time in minutes
)
