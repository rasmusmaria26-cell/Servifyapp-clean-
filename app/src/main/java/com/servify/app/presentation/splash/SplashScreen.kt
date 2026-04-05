package com.servify.app.presentation.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.servify.app.designsystem.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    viewModel: SplashViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: (String) -> Unit
) {
    val navigationTarget by viewModel.navigationTarget.collectAsState()

    // Logo scale animation — starts small, bounces to full size
    val logoScale = remember { Animatable(0.6f) }
    // Logo alpha — fades in
    val logoAlpha = remember { Animatable(0f) }
    // Tagline alpha — fades in after logo
    val taglineAlpha = remember { Animatable(0f) }

    // Pulsing glow behind logo
    val infiniteTransition = rememberInfiniteTransition(label = "glow_pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    // Animation sequence
    LaunchedEffect(Unit) {
        // Phase 1: Logo appears
        launch { logoAlpha.animateTo(1f, tween(600)) }
        logoScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))

        // Phase 2: Tagline fades in
        delay(200)
        taglineAlpha.animateTo(1f, tween(500))

        // Phase 3: Wait then navigate
        delay(800)
    }

    // Handle navigation after auth check completes
    LaunchedEffect(navigationTarget) {
        when (navigationTarget) {
            "login" -> onNavigateToLogin()
            is String -> if (navigationTarget!!.startsWith("home/")) {
                onNavigateToHome(navigationTarget!!.removePrefix("home/"))
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        // Pulsing glow circle behind logo
        Box(
            modifier = Modifier
                .size(200.dp)
                .alpha(glowAlpha)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            ServifyBlue.copy(alpha = 0.4f),
                            Color.Transparent
                        ),
                        radius = 300f
                    )
                )
        )

        // Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Servify",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 52.sp,
                    letterSpacing = (-1.5).sp
                ),
                color = TextPrimary,
                modifier = Modifier
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Services at your fingertips",
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = Inter,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                modifier = Modifier.alpha(taglineAlpha.value)
            )
        }

        Text(
            text = "Made with ♥",
            style = MaterialTheme.typography.labelSmall,
            fontFamily = Inter,
            color = TextSecondary.copy(alpha = 0.5f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .alpha(taglineAlpha.value)
        )
    }
}
