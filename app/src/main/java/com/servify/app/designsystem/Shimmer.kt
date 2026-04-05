package com.servify.app.designsystem

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.servify.app.designsystem.theme.Gray100
import com.servify.app.designsystem.theme.Gray200

@Composable
fun ShimmerBrush(): Brush {
    val shimmerColors = listOf(
        Gray100,
        Gray200.copy(alpha = 0.6f),
        Gray100,
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_rotation"
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )
}

@Composable
fun ShimmerItem(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp)
) {
    Box(
        modifier = modifier
            .background(ShimmerBrush(), shape)
    )
}

@Composable
fun CategorySkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(8.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ShimmerItem(
            modifier = Modifier
                .size(40.dp)
                .background(Color.Transparent, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        ShimmerItem(
            modifier = Modifier
                .width(60.dp)
                .height(12.dp)
        )
    }
}

@Composable
fun JobCardSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ShimmerItem(modifier = Modifier.width(120.dp).height(20.dp))
            ShimmerItem(modifier = Modifier.width(60.dp).height(24.dp), shape = RoundedCornerShape(8.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
        ShimmerItem(modifier = Modifier.width(180.dp).height(16.dp))
        Spacer(modifier = Modifier.height(8.dp))
        ShimmerItem(modifier = Modifier.fillMaxWidth().height(40.dp))
    }
}
