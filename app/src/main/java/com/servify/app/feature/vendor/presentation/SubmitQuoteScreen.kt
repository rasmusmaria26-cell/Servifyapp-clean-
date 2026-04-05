package com.servify.app.feature.vendor.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.shape.CircleShape
import androidx.hilt.navigation.compose.hiltViewModel
import com.servify.app.designsystem.ServifyButton
import com.servify.app.designsystem.theme.*

private val ESTIMATE_OPTIONS = listOf(
    "Under 1 hour", "1–2 hours", "Half a day",
    "1 day", "2–3 days", "1 week", "Custom"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitQuoteScreen(
    viewModel: RepairFeedViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onQuoteSubmitted: () -> Unit,
    onNavigateToMap: (String, Double, Double) -> Unit = { _, _, _ -> }
) {
    val quoteState by viewModel.quoteState.collectAsState()
    val request = viewModel.selectedRequest

    // Navigate back to feed when quote is submitted
    LaunchedEffect(quoteState.submitted) {
        if (quoteState.submitted) onQuoteSubmitted()
    }

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
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Header ──────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.05f), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                        .size(42.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Submit Quote",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            request?.let { req ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = ServifyBlue.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ServifyBlue.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Devices, contentDescription = null, tint = ServifyBlue, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "${req.deviceType} · ${req.brand} ${req.model}", 
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary, 
                                fontFamily = SpaceGrotesk, 
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = req.issueCategory, 
                            style = MaterialTheme.typography.labelSmall,
                            color = ServifyBlue, 
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = req.description, 
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary, 
                            fontFamily = Inter, 
                            maxLines = 2
                        )
                    }
                }

                // Show map button if customer has coordinates
                val lat = req.latitude
                val lng = req.longitude
                if (lat != null && lng != null) {
                    Spacer(Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = { onNavigateToMap("Customer Location", lat, lng) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ServifyBlue),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ServifyBlue.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("View Customer Location", fontFamily = Inter, fontWeight = FontWeight.Bold)
                    }
                }

                // Show attached photos
                if (req.mediaUrls.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Attached Photos",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(req.mediaUrls) { url ->
                            coil.compose.AsyncImage(
                                model = url,
                                contentDescription = "Attached photo",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        }
                    }
                }
            }

            // ── Quote pricing ─────────────────────────────────────────────
            QuoteSection(title = "Your Price", icon = Icons.Default.CurrencyRupee) {
                OutlinedTextField(
                    value = quoteState.price,
                    onValueChange = viewModel::onPrice,
                    label = { Text(text = "Bid Price (₹) *", style = MaterialTheme.typography.labelMedium, fontFamily = Inter, color = TextSecondary) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = { Text("₹", color = ServifyBlue, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(start = 4.dp)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = quoteFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, color = Color.White)
                )

                Spacer(Modifier.height(4.dp))
                Text(
                    text = "⚠️ Your price is locked once the customer accepts your quote.",
                    style = MaterialTheme.typography.labelSmall,
                    color = AmberAccent,
                    fontFamily = Inter,
                    fontWeight = FontWeight.Bold
                )
            }

            // ── Estimated time ────────────────────────────────────────────
            QuoteSection(title = "Estimated Time", icon = Icons.Default.Schedule) {
                var dropdownExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = quoteState.estimatedTime,
                        onValueChange = viewModel::onEstimatedTime,
                        label = { Text(text = "Repair Time *", style = MaterialTheme.typography.labelMedium, fontFamily = Inter, color = TextSecondary) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = quoteFieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = Inter, color = Color.White)
                    )
                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.background(Color(0xFF1E293B))
                    ) {
                        ESTIMATE_OPTIONS.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(text = opt, style = MaterialTheme.typography.bodyMedium, fontFamily = Inter, color = Color.White) },
                                onClick = {
                                    viewModel.onEstimatedTime(opt)
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // ── Warranty & pickup ─────────────────────────────────────────
            QuoteSection(title = "Terms & Logistics", icon = Icons.Default.VerifiedUser) {
                OutlinedTextField(
                    value = quoteState.warrantyDays,
                    onValueChange = viewModel::onWarrantyDays,
                    label = { Text(text = "Warranty (days) — 0 = none", style = MaterialTheme.typography.labelMedium, fontFamily = Inter, color = TextSecondary) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = quoteFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = Inter, color = Color.White)
                )
                Spacer(Modifier.height(12.dp))

                // Pickup toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.03f))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = ServifyBlue, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Pickup Available", 
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White, 
                                fontFamily = SpaceGrotesk, 
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "I can collect the device", 
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary, 
                                fontFamily = Inter
                            )
                        }
                    }
                    Switch(
                        checked = quoteState.pickupAvailable,
                        onCheckedChange = viewModel::onPickup,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ServifyBlue,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = DarkBorder
                        )
                    )
                }
            }

            // ── Optional note ─────────────────────────────────────────────
            QuoteSection(title = "Message to Customer", icon = Icons.Default.Message) {
                OutlinedTextField(
                    value = quoteState.vendorNote,
                    onValueChange = viewModel::onVendorNote,
                    placeholder = { 
                        Text(
                            text = "Optional — explain your approach, spare parts needed, etc.", 
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary, 
                            fontFamily = Inter
                        ) 
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                    minLines = 3,
                    colors = quoteFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = Inter, color = Color.White)
                )
            }

            // ── Error ──────────────────────────────────────────────────────
            AnimatedVisibility(visible = quoteState.error != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = quoteState.error ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = ErrorRed,
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            // ── CTA ───────────────────────────────────────────────────────
            ServifyButton(
                text = if (quoteState.isSubmitting) "Submitting…" else "Confirm & Send Quote",
                onClick = { viewModel.submitQuote() },
                isLoading = quoteState.isSubmitting,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ── Helpers ──────────────────────────────────────────────────────────────────

@Composable
private fun QuoteSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 14.dp)) {
                Icon(icon, contentDescription = null, tint = ServifyBlue, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = title, 
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary, 
                    fontFamily = SpaceGrotesk, 
                    fontWeight = FontWeight.Bold
                )
            }
            content()
        }
    }
}

@Composable
private fun quoteFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = ServifyBlue,
    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    cursorColor = ServifyBlue,
    focusedContainerColor = Color.White.copy(alpha = 0.05f),
    unfocusedContainerColor = Color.White.copy(alpha = 0.03f),
    focusedLabelColor = ServifyBlue,
    unfocusedLabelColor = TextSecondary
)
