package com.servify.app.designsystem.theme

import androidx.compose.ui.graphics.Color

// ============================================================
// SERVIFY DESIGN SYSTEM — Color Palette
// Architecture: UI/UX Middle-Ground (Android / Jetpack Compose)
// ============================================================

// --- Brand Accent (Primary) ---
// Single vibrant accent. Used ONLY for primary FABs, active NavigationBar
// states, and main CTAs. Do not use for decorative purposes.
val ServifyAccentBlue  = Color(0xFF2563EB)  // Electric Blue — Customer primary
val ServifyAccentLime  = Color(0xFFA3E635)  // Vivid Lime — alt accent (dark backgrounds)
val ServifyAccentOnDark = ServifyAccentLime  // Use lime against dark vendor surfaces

// --- Customer Mode (Light) ---
val CustomerBackground      = Color(0xFFFFFFFF) // Pure White
val CustomerSurface         = Color(0xFFF8F9FA) // Off-White
val CustomerSurfaceVariant  = Color(0xFFF1F5F9) // Muted slate
val CustomerOnBackground    = Color(0xFF0F172A) // Near-black text (Slate 900)
val CustomerOnSurface       = Color(0xFF0F172A)
val CustomerOnSurfaceVariant = Color(0xFF64748B) // Slate 500 — secondary text
val CustomerOutline         = Color(0xFFE2E8F0) // Subtle divider (Slate 200)

// --- Vendor Mode (Dark) ---
val VendorBackground     = Color(0xFF0F172A) // Dark Slate
val VendorSurface        = Color(0xFF1E293B) // Slightly elevated slate
val VendorSurfaceVariant = Color(0xFF334155) // Slate 700
val VendorOnBackground   = Color(0xFFF8FAFC) // Near-white (Slate 50)
val VendorOnSurface      = Color(0xFFF8FAFC)
val VendorOnSurfaceVariant = Color(0xFF94A3B8) // Slate 400 — secondary text
val VendorOutline        = Color(0xFF334155) // Slate 700 border

// --- M3 Error slot (shared) ---
val ServifyError   = Color(0xFFEF4444) // Red 500
val ServifyOnError = Color(0xFFFFFFFF)

// --- Glass Layer Alpha Constants (Section 3 of Architecture Plan) ---
// Customer (Light) glass: Color.White.copy(alpha = GLASS_ALPHA_LIGHT)
// Customer (Light) fallback: Color.White.copy(alpha = SOLID_ALPHA_LIGHT)
// Vendor (Dark) glass: Color.Black.copy(alpha = GLASS_ALPHA_DARK)
// Vendor (Dark) fallback: Color.Black.copy(alpha = SOLID_ALPHA_DARK)
const val GLASS_ALPHA_LIGHT  = 0.70f
const val SOLID_ALPHA_LIGHT  = 0.95f
const val GLASS_ALPHA_DARK   = 0.60f
const val SOLID_ALPHA_DARK   = 0.95f

// --- Hero Image Scrim ---
// Mandatory minimum scrim before ANY text composable renders over a photo.
const val IMAGE_SCRIM_ALPHA  = 0.45f

// --- Disabled State Alphas (Section 4 — Component Contracts) ---
const val DISABLED_CONTENT_ALPHA    = 0.38f  // text/icons
const val DISABLED_CONTAINER_ALPHA  = 0.12f  // container backgrounds

// --- Legacy aliases preserved for backward compatibility ---
// These will be deprecated once all screens are migrated.
val DarkBackground    = VendorBackground
val DarkSurface       = VendorSurface
val DarkSurfaceLight  = VendorSurfaceVariant
val TextPrimary       = VendorOnBackground
val TextSecondary     = VendorOnSurfaceVariant
val ServifyBlue       = ServifyAccentBlue
val AmberAccent       = Color(0xFFF59E0B)
val ErrorRed          = ServifyError
val SuccessGreen      = Color(0xFF22C55E)
val DarkBorder        = VendorOutline

// Category colors (retained for existing category card components)
val ElectronicsBlue           = Color(0xFF0EA5E9)
val MechanicalAmber           = Color(0xFFF59E0B)
val HomeEmerald               = Color(0xFF10B981)
val ElectronicsGradientStart  = Color(0xFF0C2D48)
val MechanicalGradientStart   = Color(0xFF3D2E0A)
val HomeGradientStart         = Color(0xFF0A3D2E)
val BlackPrimary              = Color(0xFF111111)
val BackgroundColor           = DarkBackground
val Gray50                    = Color(0xFF1E1E32)
val Gray100                   = Color(0xFF252540)
val Gray200                   = Color(0xFF2E2E4A)
val Gray500                   = TextSecondary
val Gray900                   = Color(0xFF111827)