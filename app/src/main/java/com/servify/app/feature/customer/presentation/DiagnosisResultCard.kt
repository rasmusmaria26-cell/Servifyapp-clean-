package com.servify.app.feature.customer.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.servify.app.core.model.AIDiagnosis
import com.servify.app.designsystem.theme.Inter
import com.servify.app.designsystem.theme.SpaceGrotesk

@Composable
fun DiagnosisResultCard(diagnosis: AIDiagnosis) {

    // Urgency color — defined locally per user specification; no Color.kt changes
    val urgencyColor = when (diagnosis.urgency.lowercase()) {
        "high"   -> MaterialTheme.colorScheme.error
        "medium" -> Color(0xFFF59E0B)
        else     -> Color(0xFF10B981)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // ── Header: title + urgency badge ────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AI Diagnosis",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                // Urgency badge
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = urgencyColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = diagnosis.urgency,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        color = urgencyColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            // ── Diagnosis text ───────────────────────────────────────────────
            Text(
                text = diagnosis.diagnosis,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = Inter,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // ── Possible Causes ──────────────────────────────────────────────
            if (diagnosis.possibleCauses.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Possible Causes",
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    diagnosis.possibleCauses.take(3).forEach { cause ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = cause,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = Inter,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // ── Cost + Time side by side ─────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                EstimateChip(
                    label = "Est. Cost",
                    value = diagnosis.estimatedCost,
                    modifier = Modifier.weight(1f)
                )
                EstimateChip(
                    label = "Est. Time",
                    value = diagnosis.estimatedTime,
                    modifier = Modifier.weight(1f)
                )
            }

            // ── Customer Advice ──────────────────────────────────────────────
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp).padding(top = 1.dp)
                    )
                    Text(
                        text = diagnosis.customerAdvice,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = Inter,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // ── Recommended Service chip ─────────────────────────────────────
            SuggestionChip(
                onClick = {},
                label = {
                    Text(
                        text = diagnosis.recommendedService,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = Inter
                    )
                },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            )
        }
    }
}

// ── Small sub-composable for cost/time cards ───────────────────────────────────

@Composable
private fun EstimateChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = Inter,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
