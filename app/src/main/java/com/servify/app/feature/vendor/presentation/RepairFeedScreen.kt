package com.servify.app.feature.vendor.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.CircleShape
import androidx.hilt.navigation.compose.hiltViewModel
import com.servify.app.feature.customer.data.RepairRequest
import com.servify.app.designsystem.theme.*

import androidx.compose.ui.graphics.Brush

// Color helpers for severity
private fun severityColor(severity: String) = when (severity.uppercase()) {
    "SEVERE"   -> Color(0xFFF87171) // Red 400
    "MODERATE" -> Color(0xFFFACC15) // Amber 400
    else       -> Color(0xFF4ADE80) // Green 400
}

private fun statusColor(status: String) = when (status.uppercase()) {
    "OPEN"      -> Color(0xFF4ADE80)
    "QUOTED"    -> ServifyBlue
    "ACCEPTED"  -> Color(0xFFC084FC) // Purple 400
    "IN_REPAIR" -> Color(0xFFFACC15)
    "COMPLETED" -> Color(0xFF4ADE80)
    else        -> TextSecondary
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepairFeedScreen(
    viewModel: RepairFeedViewModel = hiltViewModel(),
    onNavigateToSubmitQuote: () -> Unit
) {
    val feedState by viewModel.feedState.collectAsState()

    val backgroundBrush = remember {
        Brush.radialGradient(
            colors = listOf(
                Color(0xFF1E293B), // Slate 800 core
                Color(0xFF020617)  // Slate 950 abyss
            ),
            radius = 1500f
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        modifier = Modifier.background(backgroundBrush)
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // ── Header ──────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Repair Radar",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                IconButton(
                    onClick = { viewModel.fetchOpenRequests() },
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.05f), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                        .size(42.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = ServifyBlue, modifier = Modifier.size(20.dp))
                }
            }

            when {
            feedState.isLoading -> {
                Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ServifyBlue)
                }
            }

            feedState.requests.isEmpty() -> {
                EmptyFeedState(Modifier.fillMaxSize().padding(paddingValues))
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    itemsIndexed(feedState.requests) { index, request ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(tween(200 + index * 60)) +
                                    slideInVertically(tween(200 + index * 60)) { it / 3 }
                        ) {
                            RepairRequestCard(
                                request = request,
                                onBidClick = {
                                    viewModel.selectRequest(request)
                                    onNavigateToSubmitQuote()
                                }
                            )
                        }
                    }
                }
            }
        }

        }

        // Error snackbar
        feedState.error?.let { error ->
            LaunchedEffect(error) { /* A snackbar host can be added here */ }
        }
    }
}

@Composable
private fun RepairRequestCard(
    request: RepairRequest,
    onBidClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "card_scale"
    )
    val sevColor = severityColor(request.severity)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(interactionSource = interactionSource, indication = null, onClick = onBidClick),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            // ── Header row ────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Device chip
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Devices,
                        contentDescription = null,
                        tint = ServifyBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "${request.deviceType} · ${request.brand}",
                        color = TextPrimary,
                        fontFamily = SpaceGrotesk,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Severity pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(sevColor.copy(alpha = 0.15f))
                        .border(1.dp, sevColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = request.severity,
                        color = sevColor,
                        fontFamily = Inter,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(6.dp))
            Text(
                text = request.model,
                color = TextSecondary,
                fontFamily = Inter,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )

            Divider(
                color = Color.White.copy(alpha = 0.08f),
                modifier = Modifier.padding(vertical = 14.dp)
            )

            // ── Issue category ────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.BugReport, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = request.issueCategory, 
                    color = ServifyBlue, 
                    fontFamily = Inter, 
                    style = MaterialTheme.typography.labelSmall, 
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── Description ───────────────────────────────────────────────
            Text(
                text = request.description,
                color = TextSecondary,
                fontFamily = Inter,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3
            )

            Spacer(Modifier.height(14.dp))

            // ── Footer: media count + bid button ─────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (request.mediaUrls.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "${request.mediaUrls.size} media attached", 
                            color = TextSecondary, 
                            fontFamily = Inter, 
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                } else {
                    Spacer(Modifier.width(1.dp))
                }

                Button(
                    onClick = onBidClick,
                    colors = ButtonDefaults.buttonColors(containerColor = ServifyBlue.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ServifyBlue.copy(alpha = 0.4f)),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Gavel, contentDescription = null, modifier = Modifier.size(16.dp), tint = ServifyBlue)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Place Bid", 
                        fontFamily = SpaceGrotesk, 
                        fontWeight = FontWeight.Bold, 
                        style = MaterialTheme.typography.labelMedium,
                        color = ServifyBlue
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyFeedState(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "empty_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label = "empty_alpha"
    )
    Box(modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Inbox,
                contentDescription = null,
                tint = ServifyBlue.copy(alpha = alpha),
                modifier = Modifier.size(56.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "No open repairs on radar", 
                color = Color.White, 
                fontFamily = SpaceGrotesk, 
                fontWeight = FontWeight.Bold, 
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "When customers request quotes, they appear here", 
                color = TextSecondary, 
                fontFamily = Inter, 
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
