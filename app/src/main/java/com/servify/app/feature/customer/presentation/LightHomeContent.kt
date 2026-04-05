package com.servify.app.feature.customer.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.servify.app.designsystem.theme.Inter
import com.servify.app.designsystem.theme.SpaceGrotesk
import java.util.Calendar

// --- Color Palette from HTML ---
val AppBg = Color(0xFFF4F7FB)
val HeaderBg = Color(0xFFE8EEF8)
val BorderLight = Color(0xFFD8E3F0)
val TextDark = Color(0xFF0F1D3A)
val TextMuted = Color(0xFF6B7A99)
val ServifyBlue = Color(0xFF1A56DB)

val BadgeAmberBg = Color(0xFFFEF3C7)
val BadgeAmberText = Color(0xFF92400E)
val BadgeBlueBg = Color(0xFFDBEAFE)
val BadgeBlueText = Color(0xFF1E40AF)
val BadgeGreenBg = Color(0xFFD1FAE5)
val BadgeGreenText = Color(0xFF065F46)

@Composable
fun LightHomeContent(
    uiState: CustomerDashboardUiState,
    onNavigateToBooking: (String?) -> Unit,
    onNavigateToRepairRequest: (String?) -> Unit,
    onNavigateToQuotes: (String) -> Unit,
    onNavigateToActiveRepair: (String) -> Unit
) {
    val activeRequests = uiState.repairRequests.filter { 
        it.status == "OPEN" || it.status == "QUOTED" || it.status == "IN_REPAIR"
    }
    
    Column(modifier = Modifier.fillMaxSize().background(AppBg)) {
        // --- HEADER ---
        Box(modifier = Modifier
            .fillMaxWidth()
            .background(
                color = HeaderBg, 
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            )
        ) {
            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 32.dp, bottom = 16.dp)) {
                // Greeting and Avatar
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                        val greeting = when (hour) {
                            in 0..11 -> "Good morning"
                            in 12..16 -> "Good afternoon"
                            else -> "Good evening"
                        }
                        Text(
                            text = greeting,
                            fontSize = 12.sp,
                            color = TextMuted,
                            fontFamily = Inter
                        )
                        Text(
                            text = uiState.userProfile?.fullName?.split(" ")?.firstOrNull() ?: "Customer",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark,
                            fontFamily = SpaceGrotesk
                        )
                    }
                    val initials = uiState.userProfile?.fullName?.split(" ")
                        ?.take(2)?.joinToString("") { it.take(1).uppercase() } ?: "C"
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(ServifyBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = initials, color = Color(0xFFE6F1FB), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
                
                // Stat Strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCell(value = "${activeRequests.size}", label = "Active", modifier = Modifier.weight(1f))
                    StatCell(value = "${activeRequests.count { it.status == "QUOTED" }}", label = "Quotes in", modifier = Modifier.weight(1f))
                    StatCell(value = "${uiState.repairRequests.count { it.status == "COMPLETED" }}", label = "Completed", modifier = Modifier.weight(1f))
                }
            }
        }

        // --- BODY ---
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp)
        ) {
            // ACTIVE SECTION
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active requests",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        fontFamily = SpaceGrotesk
                    )
                    if (activeRequests.isNotEmpty()) {
                        Text(
                            text = "See all",
                            fontSize = 11.sp,
                            color = ServifyBlue,
                            fontFamily = Inter,
                            modifier = Modifier.clickable { /* trigger tab change? */ }
                        )
                    }
                }
            }

            if (activeRequests.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(width = 1.5.dp, color = Color(0xFFC5D3E8), shape = RoundedCornerShape(14.dp))
                            .background(Color.Transparent)
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🔧", fontSize = 24.sp, modifier = Modifier.padding(bottom = 6.dp))
                            Text(
                                "No active repairs right now.\nPost a request to get quotes.",
                                fontSize = 12.sp,
                                color = TextMuted,
                                fontFamily = Inter,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "+ Post your first request →",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ServifyBlue,
                                fontFamily = Inter,
                                modifier = Modifier.clickable { onNavigateToRepairRequest(null) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            } else {
                items(activeRequests) { request ->
                    ActiveCard(
                        title = "${request.deviceType} · ${request.issueCategory}",
                        subtitle = "Posted locally", 
                        badgeText = if (request.status == "QUOTED") "Quotes in" else if (request.status == "IN_REPAIR") "In repair" else "Open",
                        badgeBg = if (request.status == "QUOTED") BadgeAmberBg else if (request.status == "IN_REPAIR") BadgeGreenBg else BadgeBlueBg,
                        badgeTextCol = if (request.status == "QUOTED") BadgeAmberText else if (request.status == "IN_REPAIR") BadgeGreenText else BadgeBlueText,
                        progress = if (request.status == "IN_REPAIR") 0.8f else 0.6f,
                        progressColor = if (request.status == "IN_REPAIR") Color(0xFF10B981) else Color(0xFFF59E0B),
                        progressLabel = if (request.status == "IN_REPAIR") "In progress" else "Waiting for bids",
                        onClick = {
                            if (request.status == "OPEN" || request.status == "QUOTED") onNavigateToQuotes(request.id)
                            else onNavigateToActiveRepair(request.id)
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }

            // BOOK A SERVICE SECTION
            item {
                Text(
                    text = "Book a service",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    fontFamily = SpaceGrotesk,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                // Grid of 6 categories
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CatTile("📱", "Electronics", Color(0xFFEFF6FF), Modifier.weight(1f)) { onNavigateToBooking("Electronics") }
                    CatTile("⚡", "Electrical", Color(0xFFFFFBEB), Modifier.weight(1f)) { onNavigateToBooking("Electrical") }
                    CatTile("🔧", "Plumbing", Color(0xFFF0FDF4), Modifier.weight(1f)) { onNavigateToBooking("Plumbing") }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CatTile("❄️", "AC / HVAC", Color(0xFFFFFBEB), Modifier.weight(1f)) { onNavigateToBooking("AC / HVAC") }
                    CatTile("🪵", "Carpentry", Color(0xFFFAF5FF), Modifier.weight(1f)) { onNavigateToBooking("Carpentry") }
                    CatTile("➕", "More", Color(0xFFF8FAFC), Modifier.weight(1f)) { onNavigateToBooking("More") }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun StatCell(value: String, label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(0.5.dp, BorderLight, RoundedCornerShape(12.dp))
            .padding(vertical = 10.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark, fontFamily = SpaceGrotesk)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = label, fontSize = 9.sp, color = TextMuted, fontFamily = Inter)
        }
    }
}

@Composable
fun ActiveCard(
    title: String,
    subtitle: String,
    badgeText: String,
    badgeBg: Color,
    badgeTextCol: Color,
    progress: Float,
    progressColor: Color,
    progressLabel: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(0.5.dp, BorderLight, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark, fontFamily = SpaceGrotesk)
                    Text(text = subtitle, fontSize = 10.sp, color = TextMuted, fontFamily = Inter)
                }
                Box(
                    modifier = Modifier.background(badgeBg, RoundedCornerShape(20.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = badgeText, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = badgeTextCol, fontFamily = Inter)
                }
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(HeaderBg)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = progress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(2.dp))
                            .background(progressColor)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = progressLabel, fontSize = 10.sp, color = TextMuted, fontFamily = Inter)
            }
        }
    }
}

@Composable
fun CatTile(emoji: String, name: String, bgTint: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(0.5.dp, BorderLight, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(bgTint),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = name, fontSize = 10.sp, color = Color(0xFF4B5E7A), fontFamily = Inter, textAlign = TextAlign.Center)
        }
    }
}
