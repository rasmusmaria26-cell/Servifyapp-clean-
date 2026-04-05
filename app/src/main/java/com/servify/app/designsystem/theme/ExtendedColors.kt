package com.servify.app.designsystem.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Extended semantic colours that cannot be mapped cleanly to Material 3 ColorScheme slots.
 *
 * DO NOT hijack M3 [tertiary] for utility colours — it has its own semantic meaning.
 * Access these via [LocalExtendedColors]:
 *
 *   val ext = LocalExtendedColors.current
 *   Text(color = ext.warning)
 */
data class ExtendedColors(
    val warning: Color,   // Amber — informational warnings
    val success: Color,   // Emerald — success states
    val destructive: Color // Rose — destructive actions (distinct from M3 error)
)

val ServifyLightExtendedColors = ExtendedColors(
    warning     = Color(0xFFF59E0B), // Amber 500
    success     = Color(0xFF10B981), // Emerald 500
    destructive = Color(0xFFF43F5E)  // Rose 500
)

val ServifyDarkExtendedColors = ExtendedColors(
    warning     = Color(0xFFFBBF24), // Amber 400 — slightly lighter for dark surfaces
    success     = Color(0xFF34D399), // Emerald 400
    destructive = Color(0xFFFB7185)  // Rose 400
)

/**
 * CompositionLocal providing [ExtendedColors] throughout the composition tree.
 * Set in [ServifyTheme]. Never provide this in individual composables.
 */
val LocalExtendedColors = staticCompositionLocalOf {
    ServifyLightExtendedColors
}
