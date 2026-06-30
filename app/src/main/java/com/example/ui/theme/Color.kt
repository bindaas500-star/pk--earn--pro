package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Bento Grid Design Theme Colors
val BentoPrimary = Color(0xFF386A20)          // Forest Green
val BentoPrimaryDark = Color(0xFF254E15)      // Deep Forest Green
val BentoAccent = Color(0xFFB7F397)           // Lime Accent
val BentoGold = Color(0xFFBC9100)             // Gold for Spin Wheel / Gold Accent

// Dark Mode Colors (Bento Slate)
val DarkBg = Color(0xFF0F110E)                // Bento Background #0F110E
val DarkSurface = Color(0xFF1A1C19)           // Bento Surface #1A1C19
val DarkSurfaceVariant = Color(0xFF2E312E)    // Bento Border/SurfaceVariant #2E312E
val DarkText = Color(0xFFFCFDF6)              // Bento Text Primary #FCFDF6
val DarkTextSecondary = Color(0xFF979990)    // Bento Text Secondary #979990

// Legacy alias to prevent any compile-time missing symbols in other screens
val EmeraldPrimary = BentoPrimary
val EmeraldDark = BentoPrimaryDark
val GoldAccent = BentoAccent
val GoldLight = Color(0xFFBC9100)

// Light Mode Colors (Keep consistent or fallback to Dark Bento theme)
val LightBg = Color(0xFFF4F9F5)             // Light mint white
val LightSurface = Color(0xFFFFFFFF)        // Pure White
val LightSurfaceVariant = Color(0xFFE2EFE5) // Soft light-green accent
val LightText = Color(0xFF111827)           // Deep charcoal

