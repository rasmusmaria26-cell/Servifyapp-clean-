package com.servify.app.feature.customer.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.servify.app.designsystem.ServifyButton
import androidx.compose.ui.text.font.FontWeight

// Replaced by dynamic calculation

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StepTwoContent(
    selectedDeviceType: String,
    selectedIssueCategory: String,
    onIssueCategorySelected: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val issueCategories = androidx.compose.runtime.remember(selectedDeviceType) {
        when (selectedDeviceType) {
            "Electronics", "Smartphone", "Laptop", "Tablet", "Smartwatch", "TV", "Gaming Console", "Headphones" -> listOf(
                "Screen / Display", "Battery", "Charging Port",
                "Software / OS Crash", "Water Damage", "Speaker / Mic",
                "Keyboard / Trackpad", "Cooling / Fan", "Power (Won't turn on)",
                "Camera", "Other"
            )
            "Plumbing", "Faucet", "Toilet", "Pipe Leakage", "Sink", "Washing Machine" -> listOf(
                "Clogged Drain", "Pipe Leakage", "Dripping Faucet", "Installation",
                "Water Heater Issue", "Low Water Pressure", "Toilet Running", "Other"
            )
            "Electrical", "Wiring", "Switchboard", "Ceiling Fan", "Main Panel" -> listOf(
                "Power Outage", "Flickering Lights", "Tripped Breaker", "Installation",
                "Appliance Repair", "Wiring Issue", "Switchboard / Socket", "Other"
            )
            "AC / HVAC", "AC", "Split AC", "Window AC", "Central AC", "Heating", "Refrigerator" -> listOf(
                "Not Cooling", "Gas Leak", "Making Noise", "Installation",
                "Water Leaking", "Bad Odor", "General Service", "Other"
            )
            "Carpentry", "Furniture", "Doors", "Cabinets", "General Repair" -> listOf(
                "Furniture Assembly", "Door / Window Repair", "Lock Installation",
                "Wood Polishing", "Squeaky Hinges", "Custom Woodwork", "Other"
            )
            else -> listOf(
                "General Service", "Diagnosis", "Repair", "Installation",
                "Maintenance", "Other"
            )
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Green chip showing completed step 1 answer
        AssistChip(
            onClick = onBack,
            label = { Text(selectedDeviceType, fontSize = 10.sp) },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = Color(0xFFEAF3DE),
                labelColor = Color(0xFF3B6D11)
            ),
            border = BorderStroke(1.dp, Color(0xFFC0DD97))
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "What's the main issue?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            issueCategories.forEach { category ->
                val isSelected = selectedIssueCategory == category
                SuggestionChip(
                    onClick = { onIssueCategorySelected(category) },
                    label = { Text(category, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = if (isSelected) Color(0xFF378ADD) else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outlineVariant)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        ServifyButton(
            text = "Next →",
            onClick = onNext,
            enabled = selectedIssueCategory.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        TextButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("← Back")
        }
    }
}
