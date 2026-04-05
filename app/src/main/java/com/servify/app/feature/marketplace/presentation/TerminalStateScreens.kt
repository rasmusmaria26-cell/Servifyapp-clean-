package com.servify.app.feature.marketplace.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.servify.app.feature.marketplace.domain.BookingState

@Composable
fun AwaitingPaymentScreen(
    state: BookingState.AwaitingPayment,
    onCancel: () -> Unit,
    onProceedToPayment: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Quote Accepted!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E7D32)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Accepted Quote Information
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF0F4F8))
                .padding(16.dp)
        ) {
            Text(text = "Device: \${state.request.brand} \${state.request.deviceType}", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Final Price: $\${state.acceptedQuote.proposedPrice}", style = MaterialTheme.typography.titleLarge)
            Text(text = "Estimated Time: \${state.acceptedQuote.estimatedDurationMins} minutes")
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onProceedToPayment,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Proceed to Payment")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Text("Cancel Quote Acceptance")
        }
    }
}

@Composable
fun ExpiredScreen(
    onReturnHome: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Expired",
            tint = Color.Red,
            modifier = Modifier.fillMaxWidth(0.3f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Time's Up!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "The 60-minute bidding window has closed. You can repost your request to solicit new bids.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onReturnHome) {
            Text("Return Home")
        }
    }
}

@Composable
fun CompletedScreen(
    state: BookingState.Completed,
    onReturnHome: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Request Assigned & Confirmed",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E7D32)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Your repair professional is scheduled.")
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onReturnHome) {
            Text("Return to Dashboard")
        }
    }
}
