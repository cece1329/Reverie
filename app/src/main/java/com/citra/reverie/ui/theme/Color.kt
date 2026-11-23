package com.citra.reverie.ui.theme

import androidx.compose.ui.graphics.Color

// ── Reverie Dark Palette (Navy / Midnight Purple) ──────────────────────────
val ReverieNavy         = Color(0xFF0D0B1E)   // Background utama gelap
val ReverieNavyDeep     = Color(0xFF080617)   // Latar paling dalam
val ReverieSurface      = Color(0xFF1A1535)   // Surface card
val ReverieCardBg       = Color(0xFF211D3A)   // Warna card
val ReverieGlassDark    = Color(0xCC1A1535)   // Frosted glass gelap
val ReverieBorderDark   = Color(0xFF2E2850)   // Border subtle
val ReverieLavender     = Color(0xFF9B8EC4)   // Accent utama lavender
val ReverieLightLav     = Color(0xFFC5BFDE)   // Lavender muda
val ReveriePurpleHigh   = Color(0xFFB4A8D8)   // Highlight purple
val DarkTextPrimary     = Color(0xFFFFFFFF)   // Teks putih
val DarkTextSecondary   = Color(0xFFB0A8CC)   // Teks muted lavender-grey

// ── Reverie Light Palette ──────────────────────────────────────────────────
val LightBgPrimary      = Color(0xFFF5F3FF)   // Background putih keunguan lembut
val LightBgSecondary    = Color(0xFFEDE9FF)   // Surface light
val CardLightBg         = Color(0xFFE4DFFF)   // Card light
val LightSurfaceGlass   = Color(0xCCFFFFFF)   // Frosted white glass
val LightSurfaceBorder  = Color(0xFFCFC8EF)   // Border light
val LightTextPrimary    = Color(0xFF13102A)   // Teks gelap
val LightTextSecondary  = Color(0xFF5A527A)   // Teks muted
val LightAccentPrimary  = Color(0xFF6B4FC8)   // Accent purple terang untuk light mode

// Legacy aliases (agar tidak ada error di file lain yang masih pakai nama lama)
val DarkBgPrimary       = ReverieNavy
val DarkBgSecondary     = ReverieSurface
val CardDarkBg          = ReverieCardBg
val DarkSurfaceGlass    = ReverieGlassDark
val DarkSurfaceBorder   = ReverieBorderDark
val PastelPinkAccent    = ReverieLavender
val LightPinkAccent     = ReverieLightLav
val LightPinkPrimary    = LightAccentPrimary
