package com.servify.app.feature.customer.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.servify.app.feature.customer.data.Booking
import com.servify.app.designsystem.ServifyButton
import com.servify.app.designsystem.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailScreen(
    viewModel: BookingDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToMap: (String, Double, Double) -> Unit = { _, _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsState()
    val booking = uiState.booking

    // Fade-in animation
    val contentAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        contentAlpha.animateTo(1f, tween(400))
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Booking Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading && booking == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (uiState.error != null && booking == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
            }
        } else if (booking != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState())
                    .graphicsLayer { alpha = contentAlpha.value }
            ) {
                // Status Header Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Status badge
                        val statusColor = getStatusColor(booking.status)
                        Box(
                            modifier = Modifier
                                .background(statusColor.copy(alpha = 0.12f), RoundedCornerShape(24.dp))
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = booking.status,
                                style = MaterialTheme.typography.labelLarge,
                                color = statusColor,
                                fontFamily = Inter,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = booking.service?.name ?: "Service",
                            style = MaterialTheme.typography.headlineSmall,
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Booking #${booking.id.take(8).uppercase()}",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = Inter,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                
                // Vendor Info
                if (booking.vendorProfile != null || booking.vendorDetails != null) {
                    SectionCard(
                        title = "Vendor Info",
                        icon = Icons.Default.Person
                    ) {
                        val vendorName = booking.vendorProfile?.fullName ?: booking.vendorDetails?.businessName ?: "Unknown Vendor"
                        val businessName = booking.vendorDetails?.businessName
                        
                        DetailRow(Icons.Default.Person, "Name", vendorName)
                        
                        if (businessName != null && businessName != vendorName) {
                            Spacer(modifier = Modifier.height(12.dp))
                            DetailRow(Icons.Default.Store, "Business", businessName)
                        }

                        if (booking.vendorDetails?.phone != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            DetailRow(Icons.Default.Phone, "Phone", booking.vendorDetails.phone)
                        }

                        val lat = booking.vendorDetails?.latitude
                        val lng = booking.vendorDetails?.longitude
                        if (lat != null && lng != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = { onNavigateToMap(vendorName, lat, lng) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Map, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("View on Map", fontFamily = Inter, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Issue Description
                SectionCard(
                    title = "Issue Description",
                    icon = Icons.Default.Description
                ) {
                    Text(
                        text = booking.issueDescription,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = Inter,
                        color = MaterialTheme.colorScheme.onBackground,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Schedule & Location
                SectionCard(
                    title = "Schedule & Location",
                    icon = Icons.Default.CalendarToday
                ) {
                    DetailRow(Icons.Default.Event, "Date", booking.scheduledDate)
                    Spacer(modifier = Modifier.height(12.dp))
                    DetailRow(Icons.Default.Schedule, "Time", booking.scheduledTime)
                    Spacer(modifier = Modifier.height(12.dp))
                    DetailRow(Icons.Default.LocationOn, "Address", booking.address)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Cost Breakdown
                SectionCard(
                    title = "Cost",
                    icon = Icons.Default.CurrencyRupee
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Estimated", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = if (booking.estimatedPrice != null) "₹${booking.estimatedPrice}" else "—",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFeatureSettings = "tnum",
                                fontFamily = Inter,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    if (booking.finalPrice != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Final", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
                            Text(
                                text = "₹${booking.finalPrice}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFeatureSettings = "tnum",
                                    fontFamily = SpaceGrotesk,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Payment Status
                SectionCard(
                    title = "Payment",
                    icon = Icons.Default.Payment
                ) {
                    val paymentColor = if (booking.paymentStatus == "PAID") Color(0xFF22C55E) else AmberAccent
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Status", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Box(
                            modifier = Modifier
                                .background(paymentColor.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = booking.paymentStatus,
                                style = MaterialTheme.typography.labelMedium,
                                color = paymentColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // AI Diagnosis (if available)
                if (booking.aiDiagnosis != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    SectionCard(
                        title = "AI Diagnosis",
                        icon = Icons.Default.AutoAwesome
                    ) {
                        Text(
                            text = booking.aiDiagnosis.diagnosis,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (booking.aiDiagnosis.possibleCauses.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Possible Causes",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            booking.aiDiagnosis.possibleCauses.forEach { cause ->
                                Text(
                                    text = "• $cause",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                        if (booking.aiDiagnosis.customerAdvice.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(AmberAccent.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(text = "Tip", style = MaterialTheme.typography.labelSmall, color = AmberAccent, fontWeight = FontWeight.Bold)
                                Text(text = booking.aiDiagnosis.customerAdvice, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                    }
                }

                // Rating Section (if completed)
                if (booking.status == "COMPLETED") {
                    Spacer(modifier = Modifier.height(12.dp))
                    SectionCard(
                        title = "Rate Service",
                        icon = Icons.Default.Star
                    ) {
                        var rating by remember { mutableIntStateOf(0) }
                        val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            for (i in 1..5) {
                                Icon(
                                    imageVector = if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Star $i",
                                    tint = if (i <= rating) AmberAccent else TextSecondary,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clickable {
                                            rating = i
                                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                        }
                                )
                            }
                        }
                        if (rating > 0) {
                            Spacer(modifier = Modifier.height(16.dp))
                            ServifyButton(
                                text = "Submit Rating",
                                onClick = { /* TODO */ },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                if (booking.status == "PENDING") {
                    Spacer(modifier = Modifier.height(24.dp))
                    OutlinedButton(
                        onClick = { viewModel.cancelBooking() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        enabled = !uiState.isLoading
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState.isLoading) "Cancelling..." else "Cancel Booking",
                            style = MaterialTheme.typography.labelLarge,
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// ── Section Card Wrapper ──
@Composable
private fun SectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

// ── Detail Row ──
@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label, 
                style = MaterialTheme.typography.bodySmall, 
                fontFamily = Inter,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value, 
                style = MaterialTheme.typography.bodyMedium, 
                fontFamily = Inter,
                color = MaterialTheme.colorScheme.onBackground, 
                fontWeight = FontWeight.Bold
            )
        }
    }
}

