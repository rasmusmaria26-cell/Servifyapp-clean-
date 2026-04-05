package com.servify.app.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.servify.app.R

// ============================================================
// SERVIFY DESIGN SYSTEM — Typography
// Fonts are BUNDLED as static assets in /res/font.
// DO NOT use Downloadable Fonts — first-launch flash is unacceptable.
// ============================================================

// --- Tier 1: Display / Headers (The "Wow" factor) ---
// Space Grotesk — bold, characterful, modern startup personality.
// Used for: displayLarge/Medium/Small, headlineLarge/Medium
val SpaceGrotesk = FontFamily(
    Font(R.font.space_grotesk_regular,  FontWeight.Normal),
    Font(R.font.space_grotesk_medium,   FontWeight.Medium),
    Font(R.font.space_grotesk_semibold, FontWeight.SemiBold),
    Font(R.font.space_grotesk_bold,     FontWeight.Bold),
)

// --- Tier 2: Medium UI / Interactive (The "Action" factor) ---
// Space Grotesk Medium/SemiBold with tight tracking.
// Used for: labelLarge/Medium, titleLarge/Medium (buttons, nav items, tabs)
// (Reuses SpaceGrotesk family, see labelLarge/titleMedium styles below)

// --- Tier 3: Body / Data / Forms (The "Readable" factor) ---
// Inter — highly legible geometric sans-serif.
// Used for: bodyLarge/Medium/Small (lists, tables, text areas, filter chips)
val Inter = FontFamily(
    Font(R.font.inter_regular,   FontWeight.Normal),
    Font(R.font.inter_medium,    FontWeight.Medium),
    Font(R.font.inter_semibold,  FontWeight.SemiBold),
    Font(R.font.inter_bold,      FontWeight.Bold),
)

// --- Backward-Compatibility Aliases ---
// Existing screens reference these names. They now map to the new bundled fonts.
// These will be removed once all screens are fully migrated in later phases.
/** @deprecated Use [SpaceGrotesk] or MaterialTheme.typography directly. */
val Satoshi = SpaceGrotesk
/** @deprecated Use [Inter] directly. */
val JakartaSans = Inter

// --- Financial / Metrics ---
// FontFamily.Monospace + fontFeatureSettings = "tnum" (tabular numerals).
// Applied at the call site on every Text composable rendering currency or metrics.
// See: ServifyTextStyles.financial in ServifyTextStyles.kt

val Typography = Typography(

    // === DISPLAY (Space Grotesk — Hero moments only) ===
    displayLarge = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),

    // === HEADLINE (Space Grotesk) ===
    headlineLarge = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),

    // === TITLE (Space Grotesk — Medium UI / Interactive tier) ===
    titleLarge = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.5).sp  // Tight tracking for interactive elements
    ),
    titleMedium = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.3).sp
    ),
    titleSmall = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.2).sp
    ),

    // === BODY (Inter — Data / Forms / Readable tier) ===
    bodyLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.1.sp
    ),

    // === LABEL (Space Grotesk — Medium UI / Interactive tier) ===
    labelLarge = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.1).sp
    ),
    labelMedium = TextStyle(
        fontFamily = Inter,  // Inter for compact data labels (FilterChip, secondary labels)
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.1.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.1.sp
    )
)