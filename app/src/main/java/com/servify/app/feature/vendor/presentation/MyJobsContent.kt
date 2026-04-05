package com.servify.app.feature.vendor.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.servify.app.feature.customer.data.RepairRequest
import com.servify.app.designsystem.theme.*

@Composable
fun MyJobsContent(
    viewModel: MyJobsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadJobs() }

    if (uiState.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = ServifyBlue)
        }
        return
    }

    if (uiState.jobs.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.WorkOff, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(52.dp))
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "No active jobs yet", 
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary, 
                    fontFamily = SpaceGrotesk, 
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Accepted quotes will appear here", 
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary, 
                    fontFamily = Inter
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Text(
                text = "My Jobs", 
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary, 
                fontFamily = SpaceGrotesk, 
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Update status as you progress", 
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary, 
                fontFamily = Inter
            )
        }

        items(uiState.jobs, key = { it.id }) { job ->
            JobCard(
                job = job,
                isUpdating = uiState.updatingJobId == job.id,
                onAdvance = { viewModel.advanceStatus(job.id, job.status) }
            )
        }
    }
}

@Composable
private fun JobCard(
    job: RepairRequest,
    isUpdating: Boolean,
    onAdvance: () -> Unit
) {
    val statusColor = when (job.status) {
        "ACCEPTED"  -> ServifyBlue
        "IN_REPAIR" -> Color(0xFFFFC107)
        "COMPLETED" -> Color(0xFF4CAF50)
        else        -> TextSecondary
    }
    val nextLabel = when (job.status) {
        "ACCEPTED"  -> "Start Repair"
        "IN_REPAIR" -> "Mark Complete"
        else        -> null
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${job.deviceType} · ${job.brand} ${job.model}",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary, 
                        fontFamily = SpaceGrotesk, 
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = job.issueCategory, 
                        style = MaterialTheme.typography.labelSmall,
                        color = ServifyBlue, 
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold
                    )
                }
                // Status badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = job.status, 
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor, 
                        fontFamily = Inter, 
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(6.dp))
            Text(
                text = job.description, 
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary, 
                fontFamily = Inter, 
                maxLines = 2
            )

            if (nextLabel != null) {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onAdvance,
                    enabled = !isUpdating,
                    colors = ButtonDefaults.buttonColors(containerColor = statusColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isUpdating) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        val icon = if (job.status == "ACCEPTED") Icons.Default.Build else Icons.Default.DoneAll
                        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = nextLabel, 
                            style = MaterialTheme.typography.labelLarge,
                            fontFamily = SpaceGrotesk, 
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else if (job.status == "COMPLETED") {
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Repair completed", 
                        style = MaterialTheme.typography.bodySmall,
                        color = SuccessGreen, 
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
