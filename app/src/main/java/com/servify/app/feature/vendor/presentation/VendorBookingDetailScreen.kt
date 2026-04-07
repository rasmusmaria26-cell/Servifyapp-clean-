package com.servify.app.feature.vendor.presentation

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.servify.app.designsystem.theme.*

@Composable
fun VendorBookingDetailScreen(
    bookingId: String,
    onNavigateBack: () -> Unit,
    viewModel: VendorBookingDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(bookingId) {
        viewModel.loadBooking(bookingId)
    }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessages()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = ServifyBlue
            )
        } else {
            val booking = uiState.booking
            if (booking == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Booking not found.", color = TextSecondary, fontFamily = Inter)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Booking Details",
                            style = MaterialTheme.typography.titleLarge,
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    // Status badge
                    VendorDetailStatusBadge(status = booking.status)

                    Spacer(Modifier.height(16.dp))

                    // Customer Info Card
                    DarkCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            LabelText("Customer Issue")
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = booking.issueDescription,
                                color = Color.White,
                                fontFamily = Inter,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.height(12.dp))
                            Row {
                                Icon(Icons.Default.Event, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("${booking.scheduledDate}  ${booking.scheduledTime}", color = TextSecondary, fontFamily = Inter, style = MaterialTheme.typography.bodySmall)
                            }
                            Spacer(Modifier.height(4.dp))
                            Row {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(booking.address, color = TextSecondary, fontFamily = Inter, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    // AI Diagnosis card
                    booking.aiDiagnosis?.let { diag ->
                        Spacer(Modifier.height(12.dp))
                        DarkCard {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ServifyBlue, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("AI Diagnosis", color = ServifyBlue, fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(diag.diagnosis, color = TextSecondary, fontFamily = Inter, style = MaterialTheme.typography.bodySmall)
                                if (!diag.estimatedCost.isNullOrBlank()) {
                                    Spacer(Modifier.height(6.dp))
                                    Text("Estimated Range: ${diag.estimatedCost}", color = AmberAccent, fontFamily = Inter, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    // Photo viewer
                    if (booking.imageUrls.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        LabelText("Customer Photos")
                        Spacer(Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(booking.imageUrls) { url ->
                                AsyncImage(
                                    model = url,
                                    contentDescription = "Customer photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Action Section — status-aware
                    when (booking.status) {
                        "PENDING" -> {
                            // Propose price section
                            DarkCard {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        "Propose a Repair Price",
                                        color = Color.White,
                                        fontFamily = SpaceGrotesk,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "Set your final price for this job. The customer will review and authorize payment.",
                                        color = TextSecondary,
                                        fontFamily = Inter,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Spacer(Modifier.height(16.dp))
                                    OutlinedTextField(
                                        value = uiState.proposedPrice,
                                        onValueChange = viewModel::onProposedPriceChange,
                                        label = { Text("Your Price (₹)", color = TextSecondary) },
                                        modifier = Modifier.fillMaxWidth(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        leadingIcon = {
                                            Text("₹", color = ServifyBlue, fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = ServifyBlue,
                                            unfocusedBorderColor = DarkBorder,
                                            focusedContainerColor = DarkSurface,
                                            unfocusedContainerColor = DarkSurface,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        )
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    Button(
                                        onClick = viewModel::submitProposal,
                                        enabled = !uiState.isSubmitting && uiState.proposedPrice.isNotBlank(),
                                        modifier = Modifier.fillMaxWidth().height(52.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = ServifyBlue)
                                    ) {
                                        if (uiState.isSubmitting) {
                                            CircularProgressIndicator(
                                                color = Color.White,
                                                modifier = Modifier.size(20.dp),
                                                strokeWidth = 2.dp
                                            )
                                        } else {
                                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text("Send Proposal to Customer", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        "PRICE_PROPOSED" -> {
                            DarkCard {
                                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.HourglassTop, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(40.dp))
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "Awaiting Customer Approval",
                                        color = AmberAccent,
                                        fontFamily = SpaceGrotesk,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "You proposed ₹${booking.finalPrice?.toInt() ?: "—"}. The customer will be notified to pay.",
                                        color = TextSecondary,
                                        fontFamily = Inter,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }

                        "ACCEPTED" -> {
                            DarkCard {
                                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(40.dp))
                                    Spacer(Modifier.height(8.dp))
                                    Text("Customer Approved & Paid!", color = Color(0xFF4CAF50), fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Spacer(Modifier.height(4.dp))
                                    Text("Amount: ₹${booking.finalPrice?.toInt() ?: "—"}", color = Color.White, fontFamily = Inter, style = MaterialTheme.typography.bodyMedium)
                                    Spacer(Modifier.height(16.dp))
                                    Button(
                                        onClick = viewModel::markAsCompleted,
                                        modifier = Modifier.fillMaxWidth().height(52.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                                    ) {
                                        Icon(Icons.Default.Done, contentDescription = null)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Mark as Completed", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        "COMPLETED" -> {
                            DarkCard {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(32.dp))
                                    Spacer(Modifier.width(12.dp))
                                    Text("Job Completed!", color = Color.White, fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        else -> {
                            DarkCard {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Cancel, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(32.dp))
                                    Spacer(Modifier.width(12.dp))
                                    Text(booking.status.replace("_", " "), color = TextSecondary, fontFamily = Inter)
                                }
                            }
                        }
                    }

                    // Messages
                    uiState.successMessage?.let {
                        Spacer(Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50))
                                Spacer(Modifier.width(8.dp))
                                Text(it, color = Color.White, fontFamily = Inter, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    uiState.error?.let {
                        Spacer(Modifier.height(12.dp))
                        Text(it, color = ErrorRed, fontFamily = Inter, style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun DarkCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) { content() }
}

@Composable
private fun LabelText(text: String) {
    Text(text, color = TextSecondary, fontFamily = Inter, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun VendorDetailStatusBadge(status: String) {
    val (color, label) = when (status) {
        "PENDING"        -> Pair(AmberAccent, "Pending Review")
        "PRICE_PROPOSED" -> Pair(ServifyBlue, "Price Proposed")
        "ACCEPTED"       -> Pair(Color(0xFF4CAF50), "Accepted & Paid")
        "COMPLETED"      -> Pair(Color(0xFF4CAF50), "Completed")
        "CANCELLED"      -> Pair(ErrorRed, "Cancelled")
        else             -> Pair(TextSecondary, status)
    }
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(label, color = color, fontFamily = Inter, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
    }
}
