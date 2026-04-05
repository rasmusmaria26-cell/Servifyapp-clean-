package com.servify.app.feature.vendor.data

import android.util.Log
import com.servify.app.feature.vendor.domain.Vendor
import com.servify.app.core.network.SupabaseClientProvider
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VendorRepository @Inject constructor(
    private val supabase: io.github.jan.supabase.SupabaseClient
) {

    suspend fun getVendors(categoryName: String): Result<List<Vendor>> = withContext(Dispatchers.IO) {
        try {
            Log.d("VendorRepository", "Fetching vendors for category: $categoryName")
            
            // 1. If "General", return all verified vendors
            if (categoryName.equals("General", ignoreCase = true)) {
                 val vendors = supabase.from("vendors")
                    .select {
                        filter {
                            eq("is_verified", true)
                        }
                    }
                    .decodeList<Vendor>()
                 Log.d("VendorRepository", "General query returned ${vendors.size} vendors")
                 return@withContext Result.success(vendors)
            }

            // 2. Resolve Category Name to ID
            val categoryId = try {
                val category = supabase.from("service_categories")
                    .select {
                        filter {
                            eq("name", categoryName) 
                        }
                    }
                    .decodeSingleOrNull<com.servify.app.feature.customer.data.ServiceCategory>()
                category?.id
            } catch (e: Exception) {
                Log.e("VendorRepository", "Failed to resolve category name: $categoryName", e)
                null
            }

            if (categoryId == null) {
                Log.w("VendorRepository", "Category '$categoryName' not found in DB or resolution failed")
                return@withContext Result.success(emptyList()) 
            }

            Log.d("VendorRepository", "Resolved '$categoryName' to ID: $categoryId")

            // 3. Fetch Vendors using native array filtering
            val filteredVendors = supabase.from("vendors")
                .select {
                    filter {
                        eq("is_verified", true)
                        // Use contains filter for the service_categories array
                        // Postgrest syntax: service_categories=cs.{uuid}
                        contains("service_categories", listOf(categoryId))
                    }
                }
                .decodeList<Vendor>()

            Log.d("VendorRepository", "Query returned ${filteredVendors.size} vendors for $categoryName ($categoryId)")
            
            Result.success(filteredVendors)
        } catch (e: Exception) {
            Log.e("VendorRepository", "Failed to fetch vendors", e)
            Result.failure(e)
        }
    }

    suspend fun getVendorByUserId(userId: String): Result<Vendor?> = withContext(Dispatchers.IO) {
        try {
            Log.d("VendorRepository", "Fetching vendor for user: $userId")
            val vendor = supabase.from("vendors")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeSingleOrNull<Vendor>()
            
            Log.d("VendorRepository", "Vendor fetch result: ${vendor?.businessName}")
            Result.success(vendor)
        } catch (e: Exception) {
            Log.e("VendorRepository", "Failed to fetch vendor for user: $userId", e)
            Result.failure(e)
        }
    }

    /**
     * Returns the existing vendor row for this user, or creates a minimal one if none exists.
     * This satisfies the FK constraint on quotes.vendor_id without a manual onboarding step.
     */
    suspend fun getOrCreateVendorProfile(userId: String, displayName: String = "Vendor"): Result<Vendor> =
        withContext(Dispatchers.IO) {
            try {
                // Check for existing vendor row
                val existing = supabase.from("vendors")
                    .select { filter { eq("user_id", userId) } }
                    .decodeSingleOrNull<Vendor>()

                if (existing != null) {
                    Log.d("VendorRepository", "Found existing vendor: ${existing.id}")
                    return@withContext Result.success(existing)
                }

                // None found — insert a minimal vendor row
                Log.d("VendorRepository", "No vendor profile found for $userId — auto-creating one")
                val created = supabase.from("vendors")
                    .insert(
                        buildJsonObject {
                            put("user_id", userId)
                            put("business_name", displayName)
                            put("is_verified", false)
                            put("is_available", true)
                            put("rating", 0.0)
                            put("total_reviews", 0)
                            put("total_jobs", 0)
                            put("completion_rate", 0.0)
                            put("response_rate", 0.0)
                            put("performance_score", 0.0)
                            put("avg_response_minutes", 0)
                            put("kyc_status", "PENDING")
                        }
                    ) {
                        select()
                    }
                    .decodeSingle<Vendor>()

                Log.d("VendorRepository", "Auto-created vendor: ${created.id}")
                Result.success(created)
            } catch (e: Exception) {
                Log.e("VendorRepository", "Failed to get/create vendor profile", e)
                Result.failure(e)
            }
        }
}
