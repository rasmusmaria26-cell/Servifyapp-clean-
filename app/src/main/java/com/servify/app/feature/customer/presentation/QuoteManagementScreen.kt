package com.servify.app.feature.customer.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.servify.app.feature.customer.data.Quote
import com.servify.app.designsystem.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteManagementScreen(
    requestId: String,
    viewModel: QuoteManagementViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onQuoteAccepted: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var confirmQuote by remember { mutableStateOf<Quote?>(null) }

    LaunchedEffect(requestId) { viewModel.load(requestId) }
    LaunchedEffect(uiState.accepted) { if (uiState.accepted) onQuoteAccepted() }

    // ── Confirmation dialog ───────────────────────────────────────────────
    confirmQuote?.let { quote ->
        AcceptConfirmDialog(
            quote = quote,
            isLoading = uiState.isAccepting,
            onConfirm = {
                viewModel.acceptQuote(requestId, quote.id)
                confirmQuote = null
            },
            onDismiss = { confirmQuote = null }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Quotes Received", style = MaterialTheme.typography.titleLarge, fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // ── Sort bar ──────────────────────────────────────────────────
            SortBar(
                current = uiState.sortOrder,
                onSortChange = viewModel::setSortOrder,
                quoteCount = uiState.quotes.size
            )

            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                uiState.quotes.isEmpty() -> EmptyQuotesState(Modifier.fillMaxSize())

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        itemsIndexed(uiState.quotes) { idx, quote ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(tween(150 + idx * 60)) +
                                        slideInVertically(tween(150 + idx * 60)) { it / 4 }
                            ) {
                                QuoteCard(
                                    quote = quote,
                                    rank = idx + 1,
                                    onAccept = { confirmQuote = quote }
                                )
                            }
                        }
                    }
                }
            }

            // Error
            uiState.error?.let { err ->
                LaunchedEffect(err) { /* snackbar could go here */ }
            }
        }
    }
}

// ── Sort Bar ──────────────────────────────────────────────────────────────────

@Composable
private fun SortBar(
    current: QuoteSortOrder,
    onSortChange: (QuoteSortOrder) -> Unit,
    quoteCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$quoteCount quote${if (quoteCount != 1) "s" else ""}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = Inter
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SortChip("Price", current == QuoteSortOrder.PRICE)  { onSortChange(QuoteSortOrder.PRICE) }
            SortChip("Rating", current == QuoteSortOrder.RATING) { onSortChange(QuoteSortOrder.RATING) }
            SortChip("Time", current == QuoteSortOrder.TIME)    { onSortChange(QuoteSortOrder.TIME) }
        }
    }
}

@Composable
private fun SortChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) ServifyBlue else MaterialTheme.colorScheme.surface)
            .border(1.dp, if (selected) ServifyBlue else DarkBorder, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) Color.White else TextSecondary,
            fontFamily = Inter,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

// ── Quote Card ────────────────────────────────────────────────────────────────

@Composable
private fun QuoteCard(quote: Quote, rank: Int, onAccept: () -> Unit) {
    val rankColor = when (rank) {
        1 -> Color(0xFFFFD700)   // gold
        2 -> Color(0xFFC0C0C0)   // silver
        3 -> Color(0xFFCD7F32)   // bronze
        else -> TextSecondary
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (rank == 1) ServifyBlue.copy(alpha = 0.35f) else DarkBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Header ───────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Rank badge
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(rankColor.copy(alpha = 0.18f))
                            .border(1.dp, rankColor.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Text(text = "#$rank", color = rankColor, style = MaterialTheme.typography.labelSmall, fontFamily = Inter, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = quote.vendor?.businessName ?: "Vendor",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.Bold
                        )
                        quote.vendor?.let { v ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(12.dp))
                                Spacer(Modifier.width(3.dp))
                                Text(
                                    text = "${v.rating} (${v.totalReviews})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontFamily = Inter
                                )
                                if (v.isVerified) {
                                    Spacer(Modifier.width(6.dp))
                                    Icon(Icons.Default.Verified, contentDescription = "Verified", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(13.dp))
                                }
                            }
                        }
                    }
                }

                // Price highlight
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${String.format("%.0f", quote.price)}",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFeatureSettings = "tnum",
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (rank == 1) {
                        Text(
                            text = "Best price", 
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF4CAF50), 
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Divider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(vertical = 12.dp))

            // ── Details grid ─────────────────────────────────────────────
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuoteDetail(Icons.Default.Schedule, "Est. Time", quote.estimatedTime, Modifier.weight(1f))
                QuoteDetail(Icons.Default.Shield,   "Warranty",  "${quote.warrantyDays}d",   Modifier.weight(1f))
                if (quote.pickupAvailable) {
                    QuoteDetail(Icons.Default.DirectionsCar, "Pickup", "Available", Modifier.weight(1f))
                }
            }

            // Vendor note
            quote.vendorNote?.let { note ->
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "\"$note\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = Inter,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }

            Spacer(Modifier.height(14.dp))

            // ── Accept CTA ────────────────────────────────────────────────
            if (quote.status == "PENDING") {
                Button(
                    onClick = onAccept,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = if (rank == 1) ServifyBlue else MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(10.dp),
                    border = if (rank != 1) BorderStroke(1.dp, ServifyBlue) else null
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp),
                        tint = if (rank == 1) Color.White else ServifyBlue)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Accept Quote",
                        style = MaterialTheme.typography.labelLarge,
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        color = if (rank == 1) Color.White else ServifyBlue
                    )
                }
            } else {
                // Show accepted / rejected badge
                val statusColor = if (quote.status == "ACCEPTED") Color(0xFF4CAF50) else ErrorRed
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(statusColor.copy(alpha = 0.12f))
                        .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = quote.status, 
                        style = MaterialTheme.typography.labelLarge,
                        color = statusColor, 
                        fontFamily = SpaceGrotesk, 
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun QuoteDetail(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.Start) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(13.dp))
            Spacer(Modifier.width(4.dp))
            Text(
                text = label, 
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant, 
                fontFamily = Inter
            )
        }
        Text(
            text = value, 
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground, 
            fontFamily = Inter, 
            fontWeight = FontWeight.Bold
        )
    }
}

// ── Confirmation Dialog ───────────────────────────────────────────────────────

@Composable
private fun AcceptConfirmDialog(
    quote: Quote,
    isLoading: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Gavel, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Accept this quote?", 
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground, 
                    fontFamily = SpaceGrotesk, 
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "₹${String.format("%.0f", quote.price)} from ${quote.vendor?.businessName ?: "this vendor"}.\nAll other quotes will be rejected.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFeatureSettings = "tnum"
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = Inter,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text(
                            text = "Cancel", 
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, 
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = ServifyBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        else Text(
                            text = "Confirm", 
                            style = MaterialTheme.typography.labelLarge,
                            fontFamily = SpaceGrotesk, 
                            fontWeight = FontWeight.Bold, 
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// ── Empty State ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyQuotesState(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse), label = "alpha"
    )
    Box(modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = alpha), modifier = Modifier.size(56.dp))
            Spacer(Modifier.height(16.dp))
            Text(
                text = "No quotes yet", 
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground, 
                fontFamily = SpaceGrotesk, 
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Vendors are being notified. Check back soon.", 
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant, 
                fontFamily = Inter
            )
        }
    }
}
