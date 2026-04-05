package com.servify.app.feature.marketplace.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.servify.app.feature.marketplace.domain.BookingState
import com.servify.app.feature.marketplace.domain.Quote

@Composable
fun ActiveBiddingScreen(
    state: BookingState.ActiveBidding,
    onAcceptQuote: (Quote) -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // App Bar Mockup
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onNavigateBack) {
                Text("Back")
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Bidding Open",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Header: Request Details & Ticker
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF0F4F8))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Expires In:", style = MaterialTheme.typography.labelLarge)
            
            // Dynamic Ticker Format (MM:SS) passed cleanly from ViewModel
            Text(
                text = state.remainingFormatted,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold,
                color = if (state.remainingFormatted.startsWith("00:") || state.remainingFormatted.startsWith("01:") || state.remainingFormatted.startsWith("02:") || state.remainingFormatted.startsWith("03:") || state.remainingFormatted.startsWith("04:")) {
                    Color.Red // Turn red in last 5 minutes (rough approximation for MVP)
                } else {
                    Color(0xFF2E7D32) // Green
                }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "\${state.request.brand} \${state.request.deviceType}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "\${state.request.issueDescription}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Body: List of Quotes
        if (state.quotes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Waiting for vendors to bid...", color = Color.Gray)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Typically you'd sort by estimatedDuration or price here or in the ViewModel
                items(state.quotes) { quote ->
                    QuoteCard(
                        quote = quote,
                        onAccept = { onAcceptQuote(quote) }
                    )
                }
            }
        }
    }
}

// Separate component for re-usability
@Composable
fun QuoteCard(
    quote: Quote,
    onAccept: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(), 
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Vendor \${quote.vendorId.take(5)}", 
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "$\${quote.proposedPrice}", 
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF1976D2)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Estimated Time: \${quote.estimatedDurationMins} mins",
                style = MaterialTheme.typography.bodyMedium
            )
            
            if (!quote.vendorNotes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Notes: \${quote.vendorNotes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onAccept,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Accept (\$\${quote.proposedPrice})")
            }
        }
    }
}

