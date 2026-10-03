package com.example.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Booth Theme Color Palette
 *
 * Core loop visual identity:
 * - Booth: Deep dark background (#161310)
 * - Paper: Warm tactile text and accents (#EFE6D6)
 * - Ink: Deep text on light surfaces (#1A1410)
 * - Amber: Captured segments, focal actions, analog instrument glow (#C58A3A)
 * - Dim: Secondary metadata, timestamps, muted labels (#6B6258)
 * - Missed: Empty graphite ring, NEVER red (#453D36)
 */
val BoothBlack = Color(0xFF161310)
val BoothPaper = Color(0xFFEFE6D6)
val BoothInk = Color(0xFF1A1410)
val BoothAmber = Color(0xFFC58A3A)
val BoothDim = Color(0xFF6B6258)

// Component Surfaces & Borders
val BoothSurface = Color(0xFF1E1915)
val BoothSurfaceElevated = Color(0xFF26201B)
val BoothBorder = Color(0xFF322A23)
val BoothBorderSubtle = Color(0xFF251F19)

// Status Tones (Strictly no red for missed, no purple, no lime green)
val BoothMissedGraphite = Color(0xFF453D36)
val BoothMissedRing = Color(0xFF5C5349)
val BoothModified = Color(0xFFA67D44)
val BoothDone = Color(0xFFC58A3A)
val BoothSkip = Color(0xFF3F3730)

// Text variants on dark Booth
val TextPrimary = BoothPaper
val TextSecondary = BoothDim
val TextMuted = Color(0xFF524A42)
val TextOnAmber = BoothInk
val TextOnPaper = BoothInk
