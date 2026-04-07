package com.servify.app.feature.payment

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.servify.app.core.payment.PaymentViewModel

@Composable
fun PaymentScreen(
    vendorName: String = "Servify Service",
    amount: Long = 50000, // ₹500 in paisa
    description: String = "Service Payment",
    onPaymentComplete: (paymentId: String?) -> Unit = {},
    viewModel: PaymentViewModel = viewModel()
) {
    val context = LocalContext.current
    val paymentState by viewModel.paymentState.collectAsState()

    var customerName by remember { mutableStateOf("") }
    var customerEmail by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }

    LaunchedEffect(paymentState) {
        when {
            paymentState.isSuccess -> {
                Toast.makeText(
                    context,
                    "Payment Successful! ID: ${paymentState.paymentId}",
                    Toast.LENGTH_LONG
                ).show()
                onPaymentComplete(paymentState.paymentId)
            }
            paymentState.errorMessage != null -> {
                Toast.makeText(context, paymentState.errorMessage, Toast.LENGTH_LONG).show()
            }
            paymentState.isDismissed -> {
                Toast.makeText(context, "Payment cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "Payment",
                fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Service Details Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Service Details",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(text = "Service: $vendorName", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Description: $description", style = MaterialTheme.typography.bodyMedium)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Amount",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "₹${amount / 100}",
                            fontWeight = FontWeight.Bold,
                            fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Customer Details Section
            Text(
                text = "Customer Details",
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 8.dp)
            )

            // Name Input
            OutlinedTextField(
                value = customerName,
                onValueChange = { customerName = it },
                label = { Text("Full Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                singleLine = true,
                enabled = !paymentState.isLoading
            )

            // Email Input
            OutlinedTextField(
                value = customerEmail,
                onValueChange = { customerEmail = it },
                label = { Text("Email Address") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                singleLine = true,
                enabled = !paymentState.isLoading
            )

            // Phone Input
            OutlinedTextField(
                value = customerPhone,
                onValueChange = { customerPhone = it },
                label = { Text("Phone Number") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true,
                enabled = !paymentState.isLoading
            )

            // Test Info Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "TEST MODE - No real payment",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Use test card: 4111 1111 1111 1111\nExpiry: Any future date\nCVV: Any 3 digits",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Status Display
            when {
                paymentState.isSuccess -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Success",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "Payment Successful!",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Text(
                                text = "Payment ID: ${paymentState.paymentId}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
                paymentState.errorMessage != null -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "Payment Failed",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp),
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = paymentState.errorMessage ?: "Unknown error",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
                paymentState.isDismissed -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Payment Cancelled",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "You can retry payment anytime.",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            // Pay Button
            Button(
                onClick = {
                    if (customerName.isBlank() || customerEmail.isBlank() || customerPhone.isBlank()) {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (!Patterns.EMAIL_ADDRESS.matcher(customerEmail.trim()).matches()) {
                        Toast.makeText(context, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val normalizedPhone = customerPhone.filter { it.isDigit() }
                    if (normalizedPhone.length !in 10..15) {
                        Toast.makeText(context, "Phone must contain 10 to 15 digits", Toast.LENGTH_SHORT)
                            .show()
                        return@Button
                    }

                    viewModel.startPaymentProcess(
                        context = context,
                        amount = amount,
                        description = description,
                        customerName = customerName,
                        customerEmail = customerEmail.trim(),
                        customerPhone = normalizedPhone
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !paymentState.isLoading && !paymentState.isSuccess
            ) {
                if (paymentState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(24.dp)
                            .padding(end = 8.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                }
                Text(
                    text = "Pay ₹${amount / 100}",
                    fontWeight = FontWeight.Bold
                )
            }

            // Reset Button (shown after payment)
            if (paymentState.isSuccess || paymentState.errorMessage != null || paymentState.isDismissed) {
                Button(
                    onClick = {
                        viewModel.resetPaymentState()
                        customerName = ""
                        customerEmail = ""
                        customerPhone = ""
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Start New Payment")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
