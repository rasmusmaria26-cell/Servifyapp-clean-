package com.servify.app.feature.customer.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.servify.app.designsystem.ServifyButton

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StepThreeContent(
    selectedDeviceType: String,
    selectedIssueCategory: String,
    description: String,
    onDescriptionChanged: (String) -> Unit,
    isSubmitting: Boolean,
    onSubmit: () -> Unit,
    onBack: () -> Unit
) {
    var selectedUris by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<List<android.net.Uri>>(emptyList()) }
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        selectedUris = selectedUris + uris
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(
                onClick = onBack,
                label = { Text(selectedDeviceType, fontSize = 10.sp) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = Color(0xFFEAF3DE),
                    labelColor = Color(0xFF3B6D11)
                ),
                border = BorderStroke(1.dp, Color(0xFFC0DD97))
            )
            AssistChip(
                onClick = onBack,
                label = { Text(selectedIssueCategory, fontSize = 10.sp) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = Color(0xFFEAF3DE),
                    labelColor = Color(0xFF3B6D11)
                ),
                border = BorderStroke(1.dp, Color(0xFFC0DD97))
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Tell us more",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChanged,
            placeholder = { Text("Describe what happened — the more detail the better") },
            modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
            minLines = 4,
            shape = RoundedCornerShape(12.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { launcher.launch("image/*") }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.Transparent)
                    .border(
                        BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("📷")
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text("Add photos (optional)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        
        if (selectedUris.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(selectedUris.size) { i ->
                    val uri = selectedUris[i]
                    coil.compose.AsyncImage(
                        model = uri,
                        contentDescription = null,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp))
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF3DE)),
            border = BorderStroke(0.5.dp, Color(0xFFC0DD97)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("✨", modifier = Modifier.padding(end = 8.dp))
                Text(
                    text = "Gemini AI will estimate cost & urgency after you submit",
                    fontSize = 10.sp, 
                    color = Color(0xFF3B6D11)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        ServifyButton(
            text = "Submit & Get AI Diagnosis →",
            onClick = onSubmit,
            isLoading = isSubmitting,
            enabled = description.isNotBlank(),
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
