package com.servify.app.core.network

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.servify.app.BuildConfig
import com.servify.app.core.model.AIDiagnosis
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiApiClient @Inject constructor() {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            temperature = 0.2f
            topP = 0.8f
            maxOutputTokens = 4096
        }
    )

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun getDiagnosis(
        description: String,
        images: List<Bitmap> = emptyList(),
        serviceCategory: String = "General Repair"
    ): Result<AIDiagnosis> {
        return try {
            val prompt = """
You are SERVIFY-AI, an expert diagnostic engine built into the Servify+ platform — a professional repair aggregation service operating in India.

You have the combined knowledge of:
- A senior electronics repair technician (15+ years, specialising in smartphones, laptops, TVs, and home appliances)
- A licensed electrician familiar with Indian household wiring standards (IS 732)
- A certified AC/HVAC technician familiar with BEE star-rated equipment common in India
- A plumber familiar with CPVC/UPVC pipe systems used in Indian residential buildings
- A carpenter familiar with modular furniture and Indian teak/plywood construction

A customer has submitted a repair request. Your job is to deliver a precise, trustworthy, and actionable pre-visit diagnosis — the kind a senior technician would give after a 5-minute phone consultation.

═══════════════════════════════════════
REQUEST DETAILS
═══════════════════════════════════════
SERVICE CATEGORY : $serviceCategory
DEVICE TYPE      : ${description.substringBefore(" ").ifBlank { "Not specified" }}
CUSTOMER DESCRIPTION:
"$description"
═══════════════════════════════════════

DIAGNOSTIC INSTRUCTIONS:
1. Read the description carefully. Identify the PRIMARY failure symptom vs secondary symptoms.
2. If images are provided — examine every detail: burn marks, corrosion, physical damage, water stains, display artifacts, loose connections, or anything abnormal. Prioritise what you see in photos over what the customer wrote if they conflict.
3. Apply the most likely diagnosis first (highest probability), then list alternatives.
4. Cost estimates must be realistic for INDIA in 2024-2025 — account for local spare parts market, labour rates in Tier-2/Tier-3 cities, and GST. Do not give MRP-level estimates.
5. Urgency rules you MUST follow:
   - HIGH   → safety risk (electrical hazard, gas leak risk, structural failure, data loss imminent) OR will cause cascading damage if ignored beyond 48 hours
   - MEDIUM → device/system unusable or significantly degraded, no immediate safety risk, fix within 5-7 days
   - LOW    → cosmetic issue, minor inconvenience, works partially, can wait 2-4 weeks
6. customerAdvice must be ONE specific, practical action the customer can do RIGHT NOW — not generic advice like "unplug it". Example: "Remove the battery if it's swollen", "Switch off the MCB for that circuit", "Place a bucket under the pipe joint and wrap with plumber's tape temporarily".
7. If the issue sounds dangerous (sparking, burning smell, water near electrical, gas smell) — set urgency to HIGH and make customerAdvice a safety instruction first.

STRICT OUTPUT RULES:
- Return ONLY a valid JSON object
- No markdown, no code blocks, no explanation, no preamble
- All strings must be properly escaped
- possibleCauses must have EXACTLY 3 items — ordered most likely to least likely
- estimatedCost must be a specific INR range, not a vague number
- estimatedTime must account for parts availability (e.g. "2-3 hours (same day if parts available)" or "1-2 days (if display needs to be ordered)")

{
  "diagnosis": "A precise 1-2 sentence technical diagnosis naming the specific component or failure mode most likely responsible",
  "possibleCauses": [
    "Most likely cause with brief technical reasoning",
    "Second possible cause with brief technical reasoning",
    "Third possible cause with brief technical reasoning"
  ],
  "estimatedCost": "₹X,XXX – ₹X,XXX (parts + labour, inclusive of GST)",
  "estimatedTime": "X hours / X days (with parts availability note if relevant)",
  "recommendedService": "Specific service name matching the Servify+ category e.g. Smartphone Screen Replacement / AC Gas Refill / Washing Machine PCB Repair",
  "urgency": "Low or Medium or High",
  "urgencyReason": "One sentence — the specific reason this urgency level was assigned, referencing the customer's actual symptom",
  "customerAdvice": "One specific, actionable thing to do RIGHT NOW — safety-first if High urgency"
}
""".trimIndent()

            val inputContent = content {
                text(prompt)
                images.forEach { image(it) }
            }

            val response = generativeModel.generateContent(inputContent)
            val responseText = response.text?.trim()
                ?: throw Exception("Empty response from Gemini")

            val cleanJson = responseText
                .replace("```json", "")
                .replace("```", "")
                .trim()

            Result.success(json.decodeFromString<AIDiagnosis>(cleanJson))

        } catch (e: Exception) {
            e.printStackTrace()
            Result.success(getMockDiagnosis())
        }
    }

    private fun getMockDiagnosis() = AIDiagnosis(
        diagnosis = "Unable to connect to diagnosis service. A technician will assess on arrival.",
        estimatedCost = "₹500 - ₹2000",
        estimatedTime = "1-3 hours",
        recommendedService = "General Repair",
        urgency = "Medium",
        urgencyReason = "Issue requires professional assessment to determine severity.",
        possibleCauses = listOf("Component failure", "Wear and tear", "Electrical fault"),
        customerAdvice = "Avoid using the appliance until the technician has inspected it."
    )
}
