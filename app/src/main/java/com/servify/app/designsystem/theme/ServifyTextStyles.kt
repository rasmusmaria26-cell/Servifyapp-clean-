package com.servify.app.designsystem.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Pre-built TextStyle for financial values, currency amounts, and live metrics.
 *
 * MANDATORY for every Text composable rendering:
 *   - Currency values (₹, $, etc.)
 *   - Percentage metrics that update in real time
 *   - Any number column in a table or list
 *
 * The [fontFeatureSettings] = "tnum" enables tabular numerals, preventing
 * number columns from shifting width as values update. Without this, live
 * financial tables will visually jitter on every state change.
 *
 * Usage:
 *   Text(text = "₹1,24,999", style = ServifyTextStyles.financial)
 *   Text(text = "98.5%",     style = ServifyTextStyles.financialSmall)
 */
object ServifyTextStyles {

    val financial = TextStyle(
        fontFamily         = FontFamily.Monospace,
        fontWeight         = FontWeight.Medium,
        fontSize           = 16.sp,
        lineHeight         = 24.sp,
        letterSpacing      = 0.sp,
        fontFeatureSettings = "tnum"
    )

    val financialLarge = TextStyle(
        fontFamily         = FontFamily.Monospace,
        fontWeight         = FontWeight.Bold,
        fontSize           = 24.sp,
        lineHeight         = 32.sp,
        letterSpacing      = 0.sp,
        fontFeatureSettings = "tnum"
    )

    val financialSmall = TextStyle(
        fontFamily         = FontFamily.Monospace,
        fontWeight         = FontWeight.Normal,
        fontSize           = 12.sp,
        lineHeight         = 16.sp,
        letterSpacing      = 0.sp,
        fontFeatureSettings = "tnum"
    )
}
