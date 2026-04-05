package com.servify.app.feature.customer.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Service(
    val id: String,
    @SerialName("category_id")
    val categoryId: String,
    val name: String,
    val description: String? = null,
    @SerialName("base_price")
    val basePrice: Double? = null
)
