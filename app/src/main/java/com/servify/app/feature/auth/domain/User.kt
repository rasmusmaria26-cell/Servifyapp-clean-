package com.servify.app.feature.auth.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val email: String,
    @SerialName("full_name")
    val fullName: String,
    val phone: String,
    val role: String
)

@Serializable
data class UserProfile(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("full_name")
    val fullName: String,
    val phone: String?, // Phone can be null in DB
    val role: String,
    @SerialName("created_at")
    val createdAt: String
)

@Serializable
data class ProfileDto(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    val email: String? = null,
    @SerialName("full_name")
    val fullName: String?,
    val phone: String?,
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    val pincode: String? = null,
    @SerialName("language_preference")
    val languagePreference: String? = null,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

@Serializable
data class UserRoleDto(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    val role: String,
    @SerialName("created_at")
    val createdAt: String
)
