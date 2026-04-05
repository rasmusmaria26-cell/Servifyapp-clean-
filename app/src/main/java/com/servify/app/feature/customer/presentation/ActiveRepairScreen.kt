package com.servify.app.feature.customer.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.servify.app.feature.customer.data.RepairRequest
import com.servify.app.feature.vendor.domain.Vendor
import com.servify.app.designsystem.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveRepairScreen(
    requestId: String,
    viewModel: ActiveRepairViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(requestId) {
        viewModel.load(requestId)
        viewModel.startPolling(requestId)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Repair Status", style = MaterialTheme.typography.titleLarge, fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { pad ->
        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            uiState.error != null -> Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                Text(text = uiState.error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge, fontFamily = Inter)
            }
            uiState.request != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(pad)
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(Modifier.height(4.dp))

                    // Status timeline
                    RepairStatusTimeline(status = uiState.request!!.status)

                    // Device info card
                    uiState.request?.let { DeviceInfoCard(it) }

                    // Vendor card
                    uiState.vendor?.let { VendorInfoCard(it, uiState.acceptedQuote?.price) }

                    // Quote details
                    uiState.acceptedQuote?.let { quote ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "Quote Details", 
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onBackground, 
                                    fontFamily = SpaceGrotesk, 
                                    fontWeight = FontWeight.Bold
                                )
                                QuoteDetailRow(Icons.Default.Schedule, "Est. Time", quote.estimatedTime)
                                if (quote.warrantyDays > 0) {
                                    QuoteDetailRow(Icons.Default.VerifiedUser, "Warranty", "${quote.warrantyDays} days")
                                }
                                if (quote.pickupAvailable) {
                                    QuoteDetailRow(Icons.Default.DirectionsCar, "Pickup", "Vendor will collect your device")
                                }
                                quote.vendorNote?.let { note ->
                                    QuoteDetailRow(Icons.Default.Message, "Note", note)
                                }
                            }
                        }
                    }

                    // Rating dialog trigger when completed
                    if (uiState.request?.status == "COMPLETED" && !uiState.ratingSubmitted) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50).copy(alpha = 0.12f)),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(36.dp))
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Repair Completed!", 
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onBackground, 
                                    fontFamily = SpaceGrotesk, 
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "How would you rate this vendor?", 
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant, 
                                    fontFamily = Inter
                                )
                                Spacer(Modifier.height(12.dp))
                                RatingBar(onRatingSelected = { viewModel.onRatingSubmitted() })
                            }
                        }
                    }

                    if (uiState.ratingSubmitted) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = ServifyBlue.copy(alpha = 0.10f)),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ServifyBlue.copy(alpha = 0.3f))
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(22.dp))
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = "Thanks for your feedback!", 
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground, 
                                    fontFamily = Inter, 
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

// ── Status Timeline ───────────────────────────────────────────────────────────

@Composable
private fun RepairStatusTimeline(status: String) {
    val stages = listOf(
        Triple("ACCEPTED",  Icons.Default.CheckCircle, "Quote Accepted"),
        Triple("IN_REPAIR", Icons.Default.Build,       "In Repair"),
        Triple("COMPLETED", Icons.Default.Done,        "Completed")
    )
    val currentIndex = stages.indexOfFirst { it.first == status }.let { if (it == -1) 0 else it }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            stages.forEachIndexed { idx, (_, icon, label) ->
                val done   = idx <= currentIndex
                val active = idx == currentIndex
                val tint   = when {
                    active -> MaterialTheme.colorScheme.primary
                    done   -> Color(0xFF4CAF50)
                    else   -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(tint.copy(alpha = 0.15f))
                            .border(2.dp, tint, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (active) {
                            val inf = rememberInfiniteTransition(label = "pulse")
                            val scale by inf.animateFloat(1f, 1.15f, infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "s")
                            Icon(icon, contentDescription = null, tint = tint,
                                modifier = Modifier.size(20.dp).then(Modifier))
                        } else {
                            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = label, 
                        color = if (done) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = Inter, 
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
                    )
                }
                if (idx < stages.lastIndex) {
                    Box(modifier = Modifier.weight(1f).height(2.dp).padding(horizontal = 4.dp)
                        .background(if (idx < currentIndex) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline))
                }
            }
        }
    }
}

// ── Device info card ──────────────────────────────────────────────────────────

@Composable
private fun DeviceInfoCard(request: RepairRequest) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Devices, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Your Device", 
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground, 
                    fontFamily = SpaceGrotesk, 
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "${request.deviceType} · ${request.brand} ${request.model}", 
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground, 
                fontFamily = Inter,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = request.issueCategory, 
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary, 
                fontFamily = Inter,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = request.description, 
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant, 
                fontFamily = Inter
            )
        }
    }
}

// ── Vendor info card ──────────────────────────────────────────────────────────

@Composable
private fun VendorInfoCard(vendor: Vendor, price: Double?) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ServifyBlue.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(ServifyBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center) {
                        Text(
                            text = vendor.businessName.first().uppercase(), 
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary, 
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = vendor.businessName, 
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground, 
                                fontFamily = SpaceGrotesk, 
                                fontWeight = FontWeight.Bold
                            )
                            if (vendor.isVerified) {
                                Spacer(Modifier.width(6.dp))
                                Icon(Icons.Default.Verified, contentDescription = "Verified", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(3.dp))
                            Text(
                                text = "${vendor.rating} (${vendor.totalReviews} reviews)", 
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant, 
                                fontFamily = Inter
                            )
                        }
                    }
                }
                price?.let {
                    Text(
                        text = "₹${it.toInt()}", 
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFeatureSettings = "tnum",
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            vendor.phone?.let { ph ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = ph, 
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, 
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

@Composable
private fun QuoteDetailRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp).padding(top = 1.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            text = "$label: ", 
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant, 
            fontFamily = Inter
        )
        Text(
            text = value, 
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground, 
            fontFamily = Inter, 
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun RatingBar(onRatingSelected: (Int) -> Unit) {
    var selected by remember { mutableIntStateOf(0) }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        (1..5).forEach { star ->
            IconButton(onClick = { selected = star; onRatingSelected(star) }) {
                Icon(
                    if (star <= selected) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "$star stars",
                    tint = if (star <= selected) Color(0xFFFFC107) else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
