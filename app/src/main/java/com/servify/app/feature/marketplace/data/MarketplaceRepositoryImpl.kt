package com.servify.app.feature.marketplace.data

import com.servify.app.feature.marketplace.domain.Quote
import com.servify.app.feature.marketplace.domain.RepairRequest
import com.servify.app.feature.marketplace.domain.MarketplaceRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarketplaceRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : MarketplaceRepository {

    override suspend fun submitRepairRequest(
        deviceType: String,
        brand: String,
        modelName: String,
        issueDescription: String,
        severity: String,
        mediaUrls: List<String>,
        locationLat: Double,
        locationLng: Double
    ): Result<String> = runCatching {
        // We use PostGIS, so we must insert the geometry point
        // For Android client, we can send a WKT string or construct the point backend
        // Assuming custom RPC or WKT parsing. For simplicity, we send raw data and let default 
        // behavior or a specific RPC handle the PostGIS point creation.
        val response = supabase.postgrest["repair_requests"].insert(
            buildJsonObject {
                put("device_type", deviceType)
                put("brand", brand)
                put("model_name", modelName)
                put("issue_description", issueDescription)
                put("severity", severity)
                // mediaUrls should be passed as a proper JSON array, omitted for brevity
                // Note: Actual PostGIS insertion usually requires an RPC to construct ST_SetSRID(ST_MakePoint(lng, lat), 4326)
            }
        ) {
            select()
        }.decodeSingle<RepairRequest>()
        
        response.id
    }

    override suspend fun submitQuote(
        requestId: String,
        proposedPrice: Double,
        estimatedDurationMins: Int,
        vendorNotes: String?
    ): Result<Unit> = runCatching {
        supabase.postgrest["quotes"].insert(
            buildJsonObject {
                put("request_id", requestId)
                put("proposed_price", proposedPrice)
                put("estimated_duration_mins", estimatedDurationMins)
                if (vendorNotes != null) put("vendor_notes", vendorNotes)
            }
        )
    }

    override suspend fun acceptQuote(quoteId: String): Result<Unit> = runCatching {
        supabase.postgrest.rpc(
            function = "accept_quote",
            parameters = buildJsonObject {
                put("p_quote_id", quoteId)
            }
        )
    }

    override suspend fun cancelPayment(requestId: String): Result<Unit> = runCatching {
        supabase.postgrest.rpc(
            function = "cancel_payment",
            parameters = buildJsonObject {
                put("p_request_id", requestId)
            }
        )
    }

    override fun observeActiveRequest(requestId: String): Flow<RepairRequest> = callbackFlow {
        // Initial fetch
        val initialRequest = supabase.postgrest["repair_requests"]
            .select { filter { eq("id", requestId) } }
            .decodeSingle<RepairRequest>()
        
        send(initialRequest)

        // Realtime subscription using callbackFlow to manage channel lifecycle correctly
        val channel = supabase.realtime.channel("request_$requestId")
        val changes = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "repair_requests"
            filter = "id=eq.$requestId"
        }
        
        val job = launch {
            changes.collect { action ->
                when (action) {
                    is PostgresAction.Update -> {
                        // Decode directly from payload thanks to REPLICA IDENTITY FULL
                        val updatedReq = action.decodeRecord<RepairRequest>()
                        send(updatedReq)
                    }
                    is PostgresAction.Insert -> {
                        val newReq = action.decodeRecord<RepairRequest>()
                        send(newReq)
                    }
                    else -> {}
                }
            }
        }
        
        channel.subscribe()
        
        awaitClose {
            job.cancel()
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                channel.unsubscribe()
            }
        }
    }.retryWhen { cause, attempt ->
        if (attempt < 5) { // Exponential backoff max 5 attempts
            delay(1000L * (1L shl attempt.toInt())) // 1s, 2s, 4s, 8s, 16s
            true
        } else {
            false
        }
    }

    override fun observeQuotesForRequest(requestId: String): Flow<List<Quote>> = callbackFlow {
        // Initial fetch
        val currentQuotes = supabase.postgrest["quotes"]
            .select { filter { eq("request_id", requestId) } }
            .decodeList<Quote>()
            .toMutableList()
            
        send(currentQuotes.toList())

        // Realtime subscription
        val channel = supabase.realtime.channel("quotes_$requestId")
        val changes = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "quotes"
            filter = "request_id=eq.$requestId"
        }
        
        val job = launch {
            changes.collect { action ->
                when (action) {
                    is PostgresAction.Insert -> {
                        val newQuote = action.decodeRecord<Quote>()
                        currentQuotes.add(newQuote)
                        send(currentQuotes.toList())
                    }
                    is PostgresAction.Update -> {
                        val updatedQuote = action.decodeRecord<Quote>()
                        val index = currentQuotes.indexOfFirst { it.id == updatedQuote.id }
                        if (index != -1) {
                            currentQuotes[index] = updatedQuote
                            send(currentQuotes.toList())
                        }
                    }
                    else -> {}
                }
            }
        }
        
        channel.subscribe()
        
        awaitClose {
            job.cancel()
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                channel.unsubscribe()
            }
        }
    }.retryWhen { cause, attempt ->
        if (attempt < 5) { 
            delay(1000L * (1L shl attempt.toInt()))
            true
        } else {
            false
        }
    }
}
