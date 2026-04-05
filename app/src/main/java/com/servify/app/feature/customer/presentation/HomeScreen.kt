package com.servify.app.feature.customer.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.servify.app.designsystem.theme.BackgroundColor
import com.servify.app.designsystem.theme.BlackPrimary
import com.servify.app.designsystem.theme.AmberAccent
import com.servify.app.designsystem.theme.ErrorRed
import com.servify.app.designsystem.theme.Gray50
import com.servify.app.designsystem.theme.Gray500

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    role: String,
    onLogout: () -> Unit = {}
) {
    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Good Morning,",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray500
                        )
                        Text(
                            text = role.replaceFirstChar { it.uppercase() }, // e.g. "Customer"
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = BlackPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { /* TODO */ },
                        modifier = Modifier
                            .background(Gray50, CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = BlackPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundColor
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar (Mock)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(Gray50, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Gray500
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "What can we help with?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray500
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Featured / Status Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = BlackPrimary
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                    Column(modifier = Modifier.align(Alignment.TopStart)) {
                        Text(
                            text = if (role == "vendor") "Earnings this week" else "Upcoming Booking",
                            style = MaterialTheme.typography.labelMedium,
                            color = Gray500
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (role == "vendor") "$450.00" else "AC Repair",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        if (role != "vendor") {
                            Text(
                                text = "Today, 4:00 PM",
                                style = MaterialTheme.typography.bodySmall,
                                color = AmberAccent
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Logout Button (Temporary location)
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(
                   containerColor = Color.White,
                   contentColor = ErrorRed
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Logout")
            }
        }
    }
}
