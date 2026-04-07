package com.servify.app.feature.customer.data

import android.util.Log
import com.servify.app.feature.customer.data.Booking
import com.servify.app.core.network.SupabaseClientProvider
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

import io.github.jan.supabase.postgrest.query.Order

@Singleton
class BookingRepository @Inject constructor(
    private val supabase: io.github.jan.supabase.SupabaseClient
) {

    suspend fun getServices(): Result<List<com.servify.app.feature.customer.data.Service>> = withContext(Dispatchers.IO) {
        try {
            val services = supabase.from("services").select().decodeList<com.servify.app.feature.customer.data.Service>()
            Result.success(services)
        } catch (e: Exception) {
            Log.e("BookingRepository", "Failed to fetch services", e)
            Result.failure(e)
        }
    }

    suspend fun getCategories(): Result<List<com.servify.app.feature.customer.data.ServiceCategory>> = withContext(Dispatchers.IO) {
        try {
            val categories = supabase.from("service_categories").select().decodeList<com.servify.app.feature.customer.data.ServiceCategory>()
            Result.success(categories)
        } catch (e: Exception) {
            Log.e("BookingRepository", "Failed to fetch categories", e)
            Result.failure(e)
        }
    }

    suspend fun createBooking(booking: Booking): Result<Booking> = withContext(Dispatchers.IO) {
        try {
            Log.d("BookingRepository", "Creating booking: $booking")
            
            // Insert the booking into the 'bookings' table
            // We ask Supabase to return the created row to confirm success and get any server-generated fields
            val createdBooking = supabase.from("bookings")
                .insert(booking) {
                    select() // Request return of the inserted row
                }
                .decodeSingle<Booking>()
                
            Log.d("BookingRepository", "Booking created successfully: ${createdBooking.id}")
            Result.success(createdBooking)
        } catch (e: Exception) {
            Log.e("BookingRepository", "Failed to create booking", e)
            Result.failure(e)
        }
    }

    suspend fun getCustomerBookings(customerId: String): Result<List<Booking>> = withContext(Dispatchers.IO) {
        try {
            Log.d("BookingRepository", "Fetching bookings for customer: $customerId")
            
            val bookings = supabase.from("bookings")
                .select(columns = io.github.jan.supabase.postgrest.query.Columns.raw("*, services(*)")) {
                    filter {
                        eq("customer_id", customerId)
                    }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<Booking>()
                
            Log.d("BookingRepository", "Fetched ${bookings.size} bookings")
            Result.success(bookings)
        } catch (e: Exception) {
            Log.e("BookingRepository", "Failed to fetch bookings", e)
            Result.failure(e)
        }
    }

    suspend fun getVendorBookings(vendorId: String): Result<List<Booking>> = withContext(Dispatchers.IO) {
        try {
            Log.d("BookingRepository", "Fetching bookings for vendor: $vendorId")
            
            val bookings = supabase.from("bookings")
                .select(columns = io.github.jan.supabase.postgrest.query.Columns.raw("*, services(*)")) {
                    filter {
                        eq("vendor_id", vendorId)
                    }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<Booking>()
                
            Log.d("BookingRepository", "Fetched ${bookings.size} vendor bookings")
            Result.success(bookings)
        } catch (e: Exception) {
            Log.e("BookingRepository", "Failed to fetch vendor bookings", e)
            Result.failure(e)
        }
    }

    suspend fun updateBookingStatus(bookingId: String, status: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d("BookingRepository", "Updating booking $bookingId to status: $status")
            
            supabase.from("bookings").update(
                buildJsonObject {
                    put("status", status)
                }
            ) {
                filter {
                    eq("id", bookingId)
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("BookingRepository", "Failed to update booking status", e)
            Result.failure(e)
        }
    }

    suspend fun getBookingById(bookingId: String): Result<Booking> = withContext(Dispatchers.IO) {
        try {
            val booking = supabase.from("bookings")
                .select(columns = io.github.jan.supabase.postgrest.query.Columns.raw("*, services(*)")) {
                    filter {
                        eq("id", bookingId)
                    }
                }
                .decodeSingle<Booking>()
                
            val vendorProfile = try {
                if (booking.vendorId != null) {
                    supabase.from("profiles").select { filter { eq("id", booking.vendorId) } }.decodeSingleOrNull<com.servify.app.feature.auth.domain.ProfileDto>()
                } else null
            } catch(e: Exception) { 
                null 
            }
            
            val vendorDetails = try {
                if (booking.vendorId != null) {
                    supabase.from("vendors").select { filter { eq("id", booking.vendorId) } }.decodeSingleOrNull<com.servify.app.feature.vendor.domain.Vendor>()
                } else null
            } catch(e: Exception) { 
                null 
            }

            Result.success(booking.copy(vendorProfile = vendorProfile, vendorDetails = vendorDetails))
        } catch (e: Exception) {
            Log.e("BookingRepository", "Failed to fetch booking by ID", e)
            Result.failure(e)
        }
    }

    suspend fun uploadBookingImage(bytes: ByteArray, fileName: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val path = "booking_${java.util.UUID.randomUUID()}_$fileName"
            supabase.storage.from("booking-images").upload(path, bytes)
            val url = supabase.storage.from("booking-images").publicUrl(path)
            Result.success(url)
        } catch (e: Exception) {
            Log.e("BookingRepository", "Failed to upload booking image", e)
            Result.failure(e)
        }
    }

    suspend fun proposePriceForBooking(bookingId: String, price: Double): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            supabase.from("bookings").update(
                buildJsonObject {
                    put("status", "PRICE_PROPOSED")
                    put("final_cost", price)
                }
            ) {
                filter { eq("id", bookingId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("BookingRepository", "Failed to propose price for booking", e)
            Result.failure(e)
        }
    }

    suspend fun confirmBookingPayment(bookingId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            supabase.from("bookings").update(
                buildJsonObject {
                    put("status", "ACCEPTED")
                    put("payment_status", "PAID")
                }
            ) {
                filter { eq("id", bookingId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("BookingRepository", "Failed to confirm booking payment", e)
            Result.failure(e)
        }
    }

    fun observeBooking(bookingId: String): Flow<Booking> = callbackFlow {
        val channel = supabase.realtime.channel("booking_$bookingId")
        val changes = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "bookings"
            filter = "id=eq.$bookingId"
        }

        val job = CoroutineScope(Dispatchers.IO).launch {
            // Initial fetch
            getBookingById(bookingId).onSuccess { trySend(it) }

            changes.collect { action ->
                if (action is PostgresAction.Update) {
                    getBookingById(bookingId).onSuccess { trySend(it) }
                }
            }
        }

        channel.subscribe()

        awaitClose {
            job.cancel()
            CoroutineScope(Dispatchers.IO).launch {
                channel.unsubscribe()
            }
        }
    }
}
