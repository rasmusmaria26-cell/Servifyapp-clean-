package com.servify.app.feature.customer.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.servify.app.designsystem.ServifyButton

data class DeviceOption(val name: String, val emoji: String, val tint: Color)

val devices = listOf(
    DeviceOption("Smartphone",     "📱", Color(0xFFE6F1FB)),
    DeviceOption("Laptop",         "💻", Color(0xFFE6F1FB)),
    DeviceOption("AC",             "❄️", Color(0xFFFAEEDA)),
    DeviceOption("Washing Machine","🫧", Color(0xFFFAEEDA)),
    DeviceOption("Refrigerator",   "🧊", Color(0xFFE1F5EE)),
    DeviceOption("TV",             "📺", Color(0xFFE6F1FB)),
    DeviceOption("Plumbing",       "🔧", Color(0xFFE1F5EE)),
    DeviceOption("Electrical",     "⚡", Color(0xFFFAEEDA)),
    DeviceOption("Other",          "➕", Color(0xFFF1EFE8)),
)

@Composable
fun StepOneContent(
    categoryFilter: String? = null,
    selectedDeviceType: String,
    onDeviceTypeSelected: (String) -> Unit,
    onNext: () -> Unit
) {
    val displayDevices = androidx.compose.runtime.remember(categoryFilter) {
        when (categoryFilter) {
            "Electronics" -> listOf(
                DeviceOption("Smartphone",     "📱", Color(0xFFEFF6FF)),
                DeviceOption("Laptop",         "💻", Color(0xFFEFF6FF)),
                DeviceOption("Smartwatch",     "⌚", Color(0xFFEFF6FF)),
                DeviceOption("Tablet",         "📱", Color(0xFFEFF6FF)),
                DeviceOption("TV",             "📺", Color(0xFFEFF6FF)),
                DeviceOption("Other",          "➕", Color(0xFFF8FAFC))
            )
            "Electrical" -> listOf(
                DeviceOption("Wiring",         "🔌", Color(0xFFFFFBEB)),
                DeviceOption("Switchboard",    "🔘", Color(0xFFFFFBEB)),
                DeviceOption("Ceiling Fan",    "🚁", Color(0xFFFFFBEB)),
                DeviceOption("Main Panel",     "⚡", Color(0xFFFFFBEB)),
                DeviceOption("Other",          "➕", Color(0xFFF8FAFC))
            )
            "Plumbing" -> listOf(
                DeviceOption("Faucet",         "🚰", Color(0xFFF0FDF4)),
                DeviceOption("Toilet",         "🚽", Color(0xFFF0FDF4)),
                DeviceOption("Pipe Leakage",   "🔧", Color(0xFFF0FDF4)),
                DeviceOption("Sink",           "🚰", Color(0xFFF0FDF4)),
                DeviceOption("Other",          "➕", Color(0xFFF8FAFC))
            )
            "AC / HVAC" -> listOf(
                DeviceOption("Split AC",       "❄️", Color(0xFFFFFBEB)),
                DeviceOption("Window AC",      "❄️", Color(0xFFFFFBEB)),
                DeviceOption("Central AC",     "❄️", Color(0xFFFFFBEB)),
                DeviceOption("Heating",        "🔥", Color(0xFFFFFBEB)),
                DeviceOption("Other",          "➕", Color(0xFFF8FAFC))
            )
            "Carpentry" -> listOf(
                DeviceOption("Furniture",      "🪑", Color(0xFFFAF5FF)),
                DeviceOption("Doors",          "🚪", Color(0xFFFAF5FF)),
                DeviceOption("Cabinets",       "🗄️", Color(0xFFFAF5FF)),
                DeviceOption("General Repair", "🪵", Color(0xFFFAF5FF)),
                DeviceOption("Other",          "➕", Color(0xFFF8FAFC))
            )
            else -> devices
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "What needs to be fixed?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            displayDevices.forEach { device ->
                val isSelected = selectedDeviceType == device.name
                SuggestionChip(
                    onClick = { onDeviceTypeSelected(device.name) },
                    label = { Text("${device.emoji}  ${device.name}", color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = if (isSelected) Color(0xFF378ADD) else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outlineVariant)
                )
            }
        }
                
        
        Spacer(modifier = Modifier.height(24.dp))
        
        ServifyButton(
            text = "Next →",
            onClick = onNext,
            enabled = selectedDeviceType.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
