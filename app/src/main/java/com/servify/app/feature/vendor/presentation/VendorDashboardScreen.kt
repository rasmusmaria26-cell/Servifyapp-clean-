package com.servify.app.feature.vendor.presentation

import androidx.compose.animation.core.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.servify.app.feature.customer.data.Booking
import com.servify.app.designsystem.theme.*
import com.servify.app.designsystem.AmbientGlow
import com.servify.app.core.UserSession
import com.servify.app.core.AppMode

import androidx.compose.ui.graphics.Brush

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorDashboardScreen(
    viewModel: VendorDashboardViewModel = hiltViewModel(),
    onNavigateToRepairFeed: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onNavigateToMap: (String, Double, Double) -> Unit = { _, _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsState()
    val signedOut by viewModel.signedOut.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    LaunchedEffect(signedOut) {
        if (signedOut) onSignOut()
    }

    // Deep void gradient background
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
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // ── Hero Header ──────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Professional Portal",
                            style = MaterialTheme.typography.labelSmall,
                            color = ServifyBlue,
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = uiState.vendor?.businessName ?: "Service Provider",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontFamily = SpaceGrotesk
                        )
                    }

                    Row {
                        IconButton(
                            onClick = { UserSession.switchMode(AppMode.CUSTOMER) },
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.05f), CircleShape)
                                .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                                .size(42.dp)
                        ) {
                            Icon(Icons.Default.SwapHoriz, "Switch to Customer", tint = TextSecondary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        IconButton(
                            onClick = onNavigateToRepairFeed,
                            modifier = Modifier
                                .background(ServifyBlue.copy(alpha = 0.15f), CircleShape)
                                .border(1.dp, ServifyBlue.copy(alpha = 0.3f), CircleShape)
                                .size(42.dp)
                        ) {
                            Icon(Icons.Default.Build, "Repair Requests", tint = ServifyBlue, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        IconButton(
                            onClick = { viewModel.signOut() },
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.05f), CircleShape)
                                .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                                .size(42.dp)
                        ) {
                            Icon(Icons.Default.Logout, "Sign Out", tint = ErrorRed.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))

                // ── Earnings & Active Jobs ──────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "Total Earnings",
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = Inter,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "₹${String.format("%.2f", uiState.totalEarnings)}",
                            style = MaterialTheme.typography.displayMedium,
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Active Jobs",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = Inter,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .background(ServifyBlue.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .border(1.dp, ServifyBlue.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = uiState.bookings.count { it.status == "PENDING" || it.status == "ACCEPTED" }.toString(),
                                style = MaterialTheme.typography.titleLarge,
                                fontFamily = SpaceGrotesk,
                                fontWeight = FontWeight.Bold,
                                color = ServifyBlue
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // ── Segmented Tabs ───────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val tabs = listOf("Active", "History", "My Jobs")
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedTab == index
                        val bgColor by animateColorAsState(if (isSelected) Color.White.copy(alpha = 0.1f) else Color.Transparent)
                        val textColor by animateColorAsState(if (isSelected) Color.White else TextSecondary)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .background(bgColor, RoundedCornerShape(20.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                        selectedTab = index 
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                title,
                                style = MaterialTheme.typography.labelMedium,
                                fontFamily = SpaceGrotesk,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = textColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── Lists ────────────────────────────────────────────────────
                when (selectedTab) {
                    2 -> MyJobsContent()
                    else -> {
                        if (uiState.isLoading && uiState.bookings.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = ServifyBlue)
                            }
                        } else if (uiState.bookings.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No bookings found.", style = MaterialTheme.typography.bodyLarge, fontFamily = Inter, color = TextSecondary)
                            }
                        } else {
                            val filteredBookings = if (selectedTab == 0) {
                                uiState.bookings.filter { booking -> booking.status == "PENDING" || booking.status == "ACCEPTED" }
                            } else {
                                uiState.bookings.filter { booking -> booking.status == "COMPLETED" || booking.status == "CANCELLED" }
                            }

                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(filteredBookings) { booking ->
                                    VendorBookingCard(
                                        booking = booking,
                                        onStatusUpdate = { status ->
                                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                            viewModel.updateBookingStatus(booking.id, status)
                                        },
                                        onNavigateToMap = onNavigateToMap
                                    )
                                }
                                item { Spacer(modifier = Modifier.height(32.dp)) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VendorBookingCard(
    booking: Booking,
    onStatusUpdate: (String) -> Unit,
    onNavigateToMap: (String, Double, Double) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "vendor_card_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { /* Navigate to details */ }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = booking.service?.name ?: "Service Job",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontFamily = SpaceGrotesk
                )
                StatusBadge(status = booking.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                color = Color.White.copy(alpha = 0.03f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${booking.scheduledDate} at ${booking.scheduledTime}",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = booking.address,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = Inter,
                color = TextSecondary,
                maxLines = 2
            )

            val lat = booking.latitude
            val lng = booking.longitude
            if (lat != null && lng != null) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { onNavigateToMap("Customer Location", lat, lng) },
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ServifyBlue),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ServifyBlue.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("View Customer Location", fontFamily = Inter, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                }
            }

            if (booking.status == "PENDING" || booking.status == "ACCEPTED") {
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (booking.status == "PENDING") {
                        Button(
                            onClick = { onStatusUpdate("ACCEPTED") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ServifyBlue)
                        ) {
                            Text("Accept", style = MaterialTheme.typography.labelLarge, fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { onStatusUpdate("CANCELLED") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed.copy(alpha = 0.5f))
                        ) {
                            Text("Reject", style = MaterialTheme.typography.labelLarge, fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold)
                        }
                    } else if (booking.status == "ACCEPTED") {
                        Button(
                            onClick = { onStatusUpdate("COMPLETED") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Mark as Completed", style = MaterialTheme.typography.labelLarge, fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val backgroundColor = when (status) {
        "PENDING" -> AmberAccent.copy(alpha = 0.15f)
        "ACCEPTED" -> ServifyBlue.copy(alpha = 0.15f)
        "COMPLETED" -> SuccessGreen.copy(alpha = 0.15f)
        "CANCELLED" -> ErrorRed.copy(alpha = 0.15f)
        else -> Color.White.copy(alpha = 0.1f)
    }
    val borderColor = when (status) {
        "PENDING" -> AmberAccent.copy(alpha = 0.4f)
        "ACCEPTED" -> ServifyBlue.copy(alpha = 0.4f)
        "COMPLETED" -> SuccessGreen.copy(alpha = 0.4f)
        "CANCELLED" -> ErrorRed.copy(alpha = 0.4f)
        else -> Color.White.copy(alpha = 0.2f)
    }
    val textColor = when (status) {
        "PENDING" -> AmberAccent
        "ACCEPTED" -> ServifyBlue
        "COMPLETED" -> SuccessGreen
        "CANCELLED" -> ErrorRed
        else -> TextSecondary
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontFamily = Inter,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}
