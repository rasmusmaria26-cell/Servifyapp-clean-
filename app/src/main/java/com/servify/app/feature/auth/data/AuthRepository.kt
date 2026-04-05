package com.servify.app.feature.auth.data

import com.servify.app.feature.auth.domain.User
import com.servify.app.feature.auth.domain.UserProfile
import com.servify.app.core.network.SupabaseClientProvider
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log


@Singleton
class AuthRepository @Inject constructor(
    private val supabase: io.github.jan.supabase.SupabaseClient
) {
    
    suspend fun signIn(email: String, password: String): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            Log.d("AuthRepository", "Attempting sign in for: $email")
            // Sign in with Supabase Auth
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            Log.d("AuthRepository", "Sign in successful")
            
            val userId = supabase.auth.currentUserOrNull()?.id ?: throw Exception("User ID not found")
            Log.d("AuthRepository", "User ID: $userId")
            
            // Fetch user profile from 'profiles' table
            Log.d("AuthRepository", "Fetching profile...")
            val profileDto = supabase.from("profiles")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeSingle<com.servify.app.feature.auth.domain.ProfileDto>()
            Log.d("AuthRepository", "Profile fetched: $profileDto")
            
            // Fetch user role from 'user_roles' table
            Log.d("AuthRepository", "Fetching role...")
            val roleDto = supabase.from("user_roles")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeSingle<com.servify.app.feature.auth.domain.UserRoleDto>()
            Log.d("AuthRepository", "Role fetched: $roleDto")
            
            val profile = UserProfile(
                id = profileDto.id,
                userId = profileDto.userId,
                fullName = profileDto.fullName ?: "",
                phone = profileDto.phone ?: "",
                role = roleDto.role,
                createdAt = profileDto.createdAt
            )
            
            Result.success(profile)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Sign in failed: ${e.javaClass.simpleName} - ${e.message}", e)
            if (e.cause != null) {
                Log.e("AuthRepository", "Cause: ${e.cause?.javaClass?.simpleName} - ${e.cause?.message}", e.cause)
            }
            Result.failure(e)
        }
    }
    
    suspend fun signUp(
        email: String,
        password: String,
        fullName: String,
        phone: String,
        role: String
    ): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            // Sign up with Supabase Auth
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                data = buildJsonObject {
                    put("full_name", fullName)
                    put("phone", phone)
                    put("role", role)
                }
            }
            
            val userId = supabase.auth.currentUserOrNull()?.id ?: throw Exception("User ID not found")
            
            // Profile and Role should be created automatically by database trigger
            // However, the trigger might not capture 'phone' from metadata if generic
            // So we explicitly update the profile with the phone number
            supabase.from("profiles").update(
                buildJsonObject {
                    put("phone", phone)
                }
            ) {
                filter {
                    eq("user_id", userId)
                }
            }
            
            // Fetch the updated profile
             val profileDto = supabase.from("profiles")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeSingle<com.servify.app.feature.auth.domain.ProfileDto>()
                
            // Fetch user role
            val roleDto = supabase.from("user_roles")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeSingle<com.servify.app.feature.auth.domain.UserRoleDto>()
                
            val profile = UserProfile(
                id = profileDto.id,
                userId = profileDto.userId,
                fullName = profileDto.fullName ?: "",
                phone = profileDto.phone ?: "",
                role = roleDto.role,
                createdAt = profileDto.createdAt
            )
            
            Result.success(profile)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Sign up failed: ${e.javaClass.simpleName} - ${e.message}", e)
            if (e.cause != null) {
                Log.e("AuthRepository", "Cause: ${e.cause?.javaClass?.simpleName} - ${e.cause?.message}", e.cause)
            }
            Result.failure(e)
        }
    }
    
    suspend fun signOut(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            supabase.auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun resetPassword(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            supabase.auth.resetPasswordForEmail(email)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getCurrentUser(): UserProfile? = withContext(Dispatchers.IO) {
        try {
            val userId = supabase.auth.currentUserOrNull()?.id ?: return@withContext null
            
            val profileDto = supabase.from("profiles")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeSingle<com.servify.app.feature.auth.domain.ProfileDto>()
                
            val roleDto = supabase.from("user_roles")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeSingle<com.servify.app.feature.auth.domain.UserRoleDto>()
                
            UserProfile(
                id = profileDto.id,
                userId = profileDto.userId,
                fullName = profileDto.fullName ?: "",
                phone = profileDto.phone ?: "",
                role = roleDto.role,
                createdAt = profileDto.createdAt
            )
        } catch (e: Exception) {
            null
        }
    }
}
