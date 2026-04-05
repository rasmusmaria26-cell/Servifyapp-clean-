package com.servify.app.designsystem.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.servify.app.core.AppMode
import com.servify.app.core.RenderCapabilities

// ============================================================
// CUSTOMER MODE — Light ColorScheme
// Bright, inspiring, vibrant accent on white surfaces.
// ============================================================
private val ServifyCustomerScheme = lightColorScheme(
    primary          = ServifyAccentBlue,
    onPrimary        = Color.White,
    primaryContainer = ServifyAccentBlue.copy(alpha = 0.12f),
    onPrimaryContainer = ServifyAccentBlue,

    background       = CustomerBackground,
    onBackground     = CustomerOnBackground,
    surface          = CustomerSurface,
    onSurface        = CustomerOnSurface,
    surfaceVariant   = CustomerSurfaceVariant,
    onSurfaceVariant = CustomerOnSurfaceVariant,

    error            = ServifyError,
    onError          = ServifyOnError,
    outline          = CustomerOutline,
)

// ============================================================
// VENDOR MODE — Dark ColorScheme
// Dense, pro-tool, data-heavy. Dark Slate backgrounds.
// ============================================================
private val ServifyVendorScheme = darkColorScheme(
    primary          = ServifyAccentLime,
    onPrimary        = VendorBackground,
    primaryContainer = ServifyAccentLime.copy(alpha = 0.15f),
    onPrimaryContainer = ServifyAccentLime,

    background       = VendorBackground,
    onBackground     = VendorOnBackground,
    surface          = VendorSurface,
    onSurface        = VendorOnSurface,
    surfaceVariant   = VendorSurfaceVariant,
    onSurfaceVariant = VendorOnSurfaceVariant,

    error            = ServifyError,
    onError          = ServifyOnError,
    outline          = VendorOutline,
)

/**
 * Root theme composable.
 *
 * Collects [RenderCapabilities.appMode] reactively. When the user switches
 * between Customer and Vendor roles via [UserSession.switchMode], this composable
 * recomposes — swapping [MaterialTheme], status bar appearance, and
 * [LocalExtendedColors] — without requiring an Activity restart.
 *
 * This is the ONLY place [MaterialTheme] should be provided.
 */
@Composable
fun ServifyTheme(
    content: @Composable () -> Unit
) {
    val appMode by RenderCapabilities.appMode.collectAsStateWithLifecycle()

    val isCustomerTheme = appMode == AppMode.CUSTOMER
    val colorScheme = if (isCustomerTheme) ServifyCustomerScheme else ServifyVendorScheme
    val extendedColors = if (isCustomerTheme) ServifyLightExtendedColors else ServifyDarkExtendedColors

    // Edge-to-edge: let WindowCompat handle insets.
    // TopAppBar and BottomNavigationBar are responsible for their own WindowInsets padding.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = isCustomerTheme
                isAppearanceLightNavigationBars = isCustomerTheme
            }
            // Transparent system bars — content draws behind them.
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
        }
    }

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = Typography,
            content     = content
        )
    }
}