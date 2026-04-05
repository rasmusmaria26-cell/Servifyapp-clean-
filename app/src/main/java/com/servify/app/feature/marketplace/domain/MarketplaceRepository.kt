package com.servify.app.feature.marketplace.domain

import com.servify.app.feature.marketplace.domain.Quote
import com.servify.app.feature.marketplace.domain.RepairRequest
import kotlinx.coroutines.flow.Flow

interface MarketplaceRepository {

    /**
     * Submits a new repair request based on user input.
     * Returns a Result containing the new Request ID on success.
     */
    suspend fun submitRepairRequest(
        deviceType: String,
        brand: String,
        modelName: String,
        issueDescription: String,
        severity: String,
        mediaUrls: List<String>,
        locationLat: Double,
        locationLng: Double
    ): Result<String>

    /**
     * Submits a quote bid for a specific request.
     */
    suspend fun submitQuote(
        requestId: String,
        proposedPrice: Double,
        estimatedDurationMins: Int,
        vendorNotes: String?
    ): Result<Unit>

    /**
     * Calls the Supabase RPC to atomically accept a quote and reject competing bids.
     */
    suspend fun acceptQuote(quoteId: String): Result<Unit>

    /**
     * Reverses a quote acceptance due to payment webhook failure via RPC.
     */
    suspend fun cancelPayment(requestId: String): Result<Unit>

    /**
     * Connects to Supabase Realtime to stream the state of a single customer Repair Request.
     */
    fun observeActiveRequest(requestId: String): Flow<RepairRequest>

    /**
     * Connects to Supabase Realtime to stream all quotes associated with a Request.
     */
    fun observeQuotesForRequest(requestId: String): Flow<List<Quote>>
}
