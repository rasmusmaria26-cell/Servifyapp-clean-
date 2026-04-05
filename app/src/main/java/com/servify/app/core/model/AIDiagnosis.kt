package com.servify.app.core.model

import kotlinx.serialization.Serializable

@Serializable
data class AIDiagnosis(
    val diagnosis: String,
    val estimatedCost: String,
    val estimatedTime: String,
    val recommendedService: String,
    val urgency: String,
    val urgencyReason: String = "",
    val possibleCauses: List<String>,
    val customerAdvice: String = ""
)
