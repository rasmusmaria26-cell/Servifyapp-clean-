package com.servify.app.feature.customer.data

import kotlinx.serialization.Serializable

@Serializable
data class ServiceCategory(
    val id: String,
    val name: String,
    val slug: String? = null
)
