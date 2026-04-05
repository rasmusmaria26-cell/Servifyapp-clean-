package com.servify.app.feature.customer.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.servify.app.designsystem.theme.*
import com.servify.app.designsystem.AmbientGlow
import com.servify.app.designsystem.ServifySearchField
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CustomerDashboardScreen(
    selectedTab: Int,
    viewModel: CustomerDashboardViewModel = hiltViewModel(),
    onNavigateToBooking: (String?) -> Unit = {},
    onNavigateToRepairRequest: (String?) -> Unit = {},
    onNavigateToQuotes: (String) -> Unit = {},
    onNavigateToActiveRepair: (String) -> Unit = {},
    onNavigateToBookingDetail: (String) -> Unit = {},
    onSignOut: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    // Observe sign-out event
    val signedOut by viewModel.signedOut.collectAsState()
    LaunchedEffect(signedOut) {
        if (signedOut) onSignOut()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
    
        // Inline tab row (temporary — will be moved to ServifyBottomNavBar in later pass)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            when (selectedTab) {
                0 -> LightHomeContent(
                    uiState = uiState, 
                    onNavigateToBooking = onNavigateToBooking,
                    onNavigateToRepairRequest = onNavigateToRepairRequest,
                    onNavigateToQuotes = onNavigateToQuotes,
                    onNavigateToActiveRepair = onNavigateToActiveRepair
                )
                1 -> BookingsListContent(
                    uiState = uiState,
                    onBookingClick = { booking ->
                        viewModel.selectBooking(booking)
                        onNavigateToBookingDetail(booking.id)
                    },
                    onRefresh = { viewModel.fetchBookings() }
                )
                2 -> MyRepairRequestsContent(
                    viewModel = viewModel,
                    onNavigateToQuotes = onNavigateToQuotes,
                    onNavigateToActiveRepair = onNavigateToActiveRepair,
                    onPostRepair = { onNavigateToRepairRequest(null) }
                )
                3 -> ProfileScreenContent(viewModel)
            }
        }

        // FAB — scoped spring physics (FAB + CTA only, per architecture spec)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 20.dp, bottom = 88.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            AnimatedVisibility(
                visible = selectedTab == 0 || selectedTab == 2,
                enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                FloatingActionButton(
                    onClick = if (selectedTab == 2) { { onNavigateToRepairRequest(null) } } else { { onNavigateToBooking(null) } },
                    containerColor = MaterialTheme.colorScheme.onBackground, // High-Contrast Premium FAB
                    contentColor = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(18.dp),
                    elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp)
                ) {
                    Icon(
                        if (selectedTab == 2) Icons.Default.Build else Icons.Default.Add,
                        contentDescription = if (selectedTab == 2) "Post Repair Request" else "New Booking",
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}



@Composable
fun ProfileScreenContent(viewModel: CustomerDashboardViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val profile = uiState.userProfile

    // Fade-in
    val contentAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        contentAlpha.animateTo(1f, tween(400))
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = contentAlpha.value },
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Avatar + Name + Role
        item {
            Spacer(modifier = Modifier.height(32.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar circle with initials
                val initials = profile?.fullName?.split(" ")
                    ?.take(2)?.joinToString("") { it.take(1).uppercase() } ?: "?"
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.headlineMedium,
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = profile?.fullName ?: "Loading...",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Role badge
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = (profile?.role ?: "customer").replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Inter
                    )
                }
            }
        }

        // Stats Row
        item {
            Spacer(modifier = Modifier.height(28.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total Bookings
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${uiState.bookings.size}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = SpaceGrotesk
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Bookings",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Member Since
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = profile?.createdAt?.take(10) ?: "—",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontFamily = SpaceGrotesk
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Member Since",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Settings Section
        item {
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleMedium,
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Settings rows
        val settingsItems = listOf(
            Triple(Icons.Default.Notifications, "Notifications", "Manage alerts"),
            Triple(Icons.Default.CreditCard, "Payment Methods", "Cards & UPI"),
            Triple(Icons.Default.HelpOutline, "Help & Support", "FAQs & Contact"),
            Triple(Icons.Default.Info, "About Servify", "Version 1.0")
        )

        items(settingsItems.size) { index ->
            val (icon, title, subtitle) = settingsItems[index]
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* TODO */ },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = Inter
                        )
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                }
            }
            if (index < settingsItems.lastIndex) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Sign Out Button
        item {
            Spacer(modifier = Modifier.height(28.dp))
            OutlinedButton(
                onClick = { viewModel.signOut() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sign Out",
                    style = MaterialTheme.typography.labelLarge,
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

fun getStatusColor(status: String): Color {
    return when (status) {
        "PENDING" -> AmberAccent
        "ACCEPTED" -> Color(0xFF22C55E)
        "COMPLETED" -> ServifyBlue
        "CANCELLED" -> ErrorRed
        else -> TextSecondary
    }
}

data class ServiceCategory(
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val description: String
)

// ==========================================================
// Full-Width Horizontal Service Row (Option C layout)
// ==========================================================
@Composable
fun ServiceRow(
    service: ServiceCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "row_scale"
    )
    val pressAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = tween(100),
        label = "row_alpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = pressAlpha
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // High-Contrast Solid Container (NO PASTEL BLOBS)
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.onBackground)
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = service.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.background,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Title + Subtitle
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = service.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = service.description,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = Inter,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Chevron
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// ── My Repair Requests Tab ────────────────────────────────────────────────────

@Composable
fun MyRepairRequestsContent(
    viewModel: CustomerDashboardViewModel,
    onNavigateToQuotes: (String) -> Unit,
    onNavigateToActiveRepair: (String) -> Unit = {},
    onPostRepair: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.fetchRepairRequests() }

    if (uiState.repairRequests.isEmpty() && !uiState.isLoading) {
        // Empty state
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.3f, targetValue = 0.9f,
            animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse), label = "alpha"
        )
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Build,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = alpha),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "No repair requests yet",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Post a request to get expert quotes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = Inter
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "My Repair Requests",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Tap a request to view incoming vendor quotes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = Inter
                )
                Spacer(Modifier.height(8.dp))
            }
            if (uiState.isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            } else {
                itemsIndexed(uiState.repairRequests) { idx, request ->
                    val statusColor = when (request.status) {
                        "OPEN"      -> Color(0xFF4CAF50)
                        "ACCEPTED"  -> ServifyBlue
                        "IN_REPAIR" -> Color(0xFFFFC107)
                        "COMPLETED" -> Color(0xFF4CAF50)
                        else        -> TextSecondary
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                when (request.status) {
                                    "OPEN", "QUOTED" -> onNavigateToQuotes(request.id)
                                    else -> onNavigateToActiveRepair(request.id)
                                }
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${request.deviceType} · ${request.brand}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        fontFamily = SpaceGrotesk,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = request.issueCategory,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontFamily = Inter,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                // Status badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(statusColor.copy(alpha = 0.1f))
                                        .border(1.dp, statusColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = request.status,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = statusColor,
                                        fontFamily = Inter,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = request.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontFamily = Inter,
                                maxLines = 2
                            )
                            Spacer(Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val (ctaIcon, ctaText) = when (request.status) {
                                    "OPEN", "QUOTED" -> Icons.Default.ChevronRight to "View Quotes"
                                    "COMPLETED" -> Icons.Default.CheckCircle to "Completed"
                                    else -> Icons.Default.ChevronRight to "Track Repair"
                                }
                                Icon(ctaIcon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = ctaText,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
