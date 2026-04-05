package com.servify.app.feature.customer.data

import android.util.Log
import com.servify.app.feature.customer.data.Quote
import com.servify.app.feature.customer.data.RepairRequest
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "RepairRepository"

@Singleton
class RepairRepository @Inject constructor(
    private val supabase: SupabaseClient
) {

    // ── Repair Requests ───────────────────────────────────────────────────

    /** Post a new repair request from the customer */
    suspend fun createRepairRequest(request: RepairRequest): Result<RepairRequest> =
        withContext(Dispatchers.IO) {
            try {
                val created = supabase.from("repair_requests")
                    .insert(request) { select() }
                    .decodeSingle<RepairRequest>()
                Log.d(TAG, "Created repair request: ${created.id}")
                Result.success(created)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create repair request", e)
                Result.failure(e)
            }
        }

    /** Fetch all repair requests for a specific customer */
    suspend fun getCustomerRequests(customerId: String): Result<List<RepairRequest>> =
        withContext(Dispatchers.IO) {
            try {
                val requests = supabase.from("repair_requests")
                    .select {
                        filter { eq("customer_id", customerId) }
                        order("created_at", Order.DESCENDING)
                    }
                    .decodeList<RepairRequest>()
                Result.success(requests)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch customer requests", e)
                Result.failure(e)
            }
        }

    /** Fetch open repair requests visible to a vendor (for the bidding feed) */
    suspend fun getOpenRequests(): Result<List<RepairRequest>> =
        withContext(Dispatchers.IO) {
            try {
                val requests = supabase.from("repair_requests")
                    .select {
                        filter { eq("status", "OPEN") }
                        order("created_at", Order.DESCENDING)
                    }
                    .decodeList<RepairRequest>()
                Result.success(requests)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch open requests", e)
                Result.failure(e)
            }
        }

    /** Update the status of a repair request */
    suspend fun updateRequestStatus(requestId: String, status: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                supabase.from("repair_requests").update(
                    buildJsonObject { put("status", status) }
                ) {
                    filter { eq("id", requestId) }
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update request status", e)
                Result.failure(e)
            }
        }

    // ── Media Upload (Supabase Storage) ───────────────────────────────────

    /**
     * Upload a single image/video byte array for a repair request.
     * Returns the public URL of the uploaded file.
     */
    suspend fun uploadMedia(
        requestId: String,
        fileName: String,
        bytes: ByteArray,
        mimeType: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val path = "repair_requests/$requestId/$fileName"
            supabase.storage.from("repair-media").upload(path, bytes)
            val url = supabase.storage.from("repair-media").publicUrl(path)
            Result.success(url)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload media", e)
            Result.failure(e)
        }
    }

    // ── Quotes ────────────────────────────────────────────────────────────

    /** Vendor submits a quote for a repair request */
    suspend fun submitQuote(quote: Quote): Result<Quote> =
        withContext(Dispatchers.IO) {
            try {
                val created = supabase.from("quotes")
                    .insert(quote) { select() }
                    .decodeSingle<Quote>()
                Log.d(TAG, "Quote submitted: ${created.id}")
                Result.success(created)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to submit quote", e)
                Result.failure(e)
            }
        }

    /** Fetch all quotes for a repair request (for the customer's comparison screen) */
    suspend fun getQuotesForRequest(requestId: String): Result<List<Quote>> =
        withContext(Dispatchers.IO) {
            try {
                val quotes = supabase.from("quotes")
                    .select(columns = Columns.raw("*, vendors(*)")) {
                        filter { eq("request_id", requestId) }
                        order("price", Order.ASCENDING)
                    }
                    .decodeList<Quote>()
                Result.success(quotes)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch quotes", e)
                Result.failure(e)
            }
        }

    /**
     * Customer accepts a quote.
     * Sets the quote status → ACCEPTED and updates the request:
     *   status → ACCEPTED, accepted_quote_id → quoteId.
     * All other quotes for this request are implicitly REJECTED.
     */
    suspend fun acceptQuote(requestId: String, quoteId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                // 1. Mark the chosen quote as ACCEPTED
                supabase.from("quotes").update(
                    buildJsonObject { put("status", "ACCEPTED") }
                ) {
                    filter { eq("id", quoteId) }
                }

                // 2. Reject all other quotes for this request
                supabase.from("quotes").update(
                    buildJsonObject { put("status", "REJECTED") }
                ) {
                    filter {
                        eq("request_id", requestId)
                        neq("id", quoteId)
                    }
                }

                // 3. Lock the repair request to ACCEPTED state
                supabase.from("repair_requests").update(
                    buildJsonObject {
                        put("status", "ACCEPTED")
                        put("accepted_quote_id", quoteId)
                    }
                ) {
                    filter { eq("id", requestId) }
                }

                Log.d(TAG, "Quote $quoteId accepted for request $requestId")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to accept quote", e)
                Result.failure(e)
            }
        }

    /** Fetch a single repair request by ID */
    suspend fun getRequestById(requestId: String): Result<RepairRequest> =
        withContext(Dispatchers.IO) {
            try {
                val request = supabase.from("repair_requests")
                    .select {
                        filter { eq("id", requestId) }
                        limit(1)
                    }
                    .decodeSingle<RepairRequest>()
                Result.success(request)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch request by id", e)
                Result.failure(e)
            }
        }

    /**
     * Fetch all jobs assigned to a specific vendor.
     * Step 1: find all ACCEPTED quotes for this vendor.
     * Step 2: fetch each corresponding repair_request by eq("id",...) in parallel.
     * Avoids the supabase-kt IN-filter DSL limitation.
     */
    suspend fun getVendorJobs(vendorId: String): Result<List<RepairRequest>> =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Fetching jobs for vendor: $vendorId")
                val acceptedQuotes = supabase.from("quotes")
                    .select {
                        filter {
                            eq("vendor_id", vendorId)
                            eq("status", "ACCEPTED")
                        }
                    }
                    .decodeList<Quote>()

                Log.d(TAG, "getVendorJobs: ${acceptedQuotes.size} accepted quotes found for vendor $vendorId")

                if (acceptedQuotes.isEmpty()) {
                    Log.d(TAG, "No accepted quotes found for this vendor.")
                    return@withContext Result.success(emptyList())
                }

                val requestIds = acceptedQuotes.map { it.requestId }.distinct()
                Log.d(TAG, "Found ${requestIds.size} unique request IDs to fetch: $requestIds")

                // Fetch each request individually (parallel) — avoids IN-filter DSL issues
                val jobs = requestIds.mapNotNull { rid ->
                    try {
                        val req = supabase.from("repair_requests")
                            .select { filter { eq("id", rid) } }
                            .decodeSingleOrNull<RepairRequest>()
                        if (req == null) Log.w(TAG, "Request $rid returned null (check RLS policies)")
                        req
                    } catch (e: Exception) {
                        Log.e(TAG, "Error fetching request $rid: ${e.message}")
                        null
                    }
                }.sortedByDescending { it.createdAt }

                Log.d(TAG, "getVendorJobs: successfully fetched ${jobs.size} repair requests")
                Result.success(jobs)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch vendor jobs", e)
                Result.failure(e)
            }
        }
}
