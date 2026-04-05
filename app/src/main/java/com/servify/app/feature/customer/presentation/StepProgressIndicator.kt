package com.servify.app.feature.customer.presentation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun StepProgressIndicator(
    totalSteps: Int,
    currentStep: Int,
    completedSteps: Set<Int>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (step in 1..totalSteps) {
            val isCompleted = completedSteps.contains(step) || step < currentStep
            val isActive = step == currentStep
            
            val color = when {
                isCompleted -> Color(0xFF639922)
                isActive -> Color(0xFF378ADD)
                else -> MaterialTheme.colorScheme.outlineVariant
            }
            
            val width by animateDpAsState(
                targetValue = if (isActive) 18.dp else 8.dp,
                animationSpec = tween(200),
                label = "dotWidth"
            )
            
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(width = width, height = 8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}
