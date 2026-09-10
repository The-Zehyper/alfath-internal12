/*
 * Copyright (C) 2026 Nanas
 *
 * SPDX-License-Identifier: GPL-3.0-only
 */
package com.alfath.musicall.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// NANZMUSIFY — "Hear What Words Can't Say"
// Palet DINAMIS: token di bawah ini adalah getter @Composable yang membaca
// palette dari CompositionLocal — sehingga mode terang benar-benar berganti
// dan warna aksen bisa dipilih bebas oleh pengguna.

// ------------------------------------------------------------------
// Warna dasar (internal, dipakai membangun palette)
// ------------------------------------------------------------------

/** Crimson bawaan NANZMUSIFY (diambil dari logo). */
val NanzMusifyCrimsonDefault = Color(0xFFE94560)

private val DarkBackground = Color(0xFF0B0B10)
private val DarkSurface = Color(0xFF14141C)
private val DarkElevated = Color(0xFF1C1C26)
private val DarkTextPrimary = Color(0xFFF4F4F8)
private val DarkTextBright = Color(0xFFFFFFFF)
private val DarkTextSecondary = Color(0xFFA3A3AF)
private val DarkTextMuted = Color(0xFF71717D)
private val DarkLine = Color(0xFF282832)
private val DarkLineSoft = Color(0xFF22222C)
private val DarkScrim = Color(0xE60B0B10)

// Palet terang "alabaster hangat" — bukan kertas putih silau: redup, hangat,
// dan harmonis dengan crimson tanpa terasa mencolok.
private val LightBackground = Color(0xFFF0EBE4)
private val LightSurface = Color(0xFFF7F3ED)
private val LightElevated = Color(0xFFFCFAF6)
private val LightTextPrimary = Color(0xFF23201E)
private val LightTextBright = Color(0xFFFFFFFF)
private val LightTextSecondary = Color(0xFF565049)
private val LightTextMuted = Color(0xFF8A8178)
private val LightLine = Color(0xFFE0D7CB)
private val LightLineSoft = Color(0xFFEAE3D7)
private val LightScrim = Color(0x7A23201E)

// Palet HITAM (OLED): latar #000 murni untuk panel AMOLED, permukaan naik
// sangat tipis agar tetap terbedakan tanpa terasa "abu-abu".
private val BlackBackground = Color(0xFF000000)
private val BlackSurface = Color(0xFF0A0A0A)
private val BlackElevated = Color(0xFF141418)
private val BlackTextPrimary = Color(0xFFF2F2F5)
private val BlackTextSecondary = Color(0xFF9C9CA6)
private val BlackTextMuted = Color(0xFF6A6A74)
private val BlackLine = Color(0xFF1E1E24)
private val BlackLineSoft = Color(0xFF15151A)
private val BlackScrim = Color(0xF2000000)

/** Cerahkan warna aksen (untuk varian "rose"). */
internal fun lighten(c: Color, amount: Float): Color {
    val r = c.red + (1f - c.red) * amount
    val g = c.green + (1f - c.green) * amount
    val b = c.blue + (1f - c.blue) * amount
    return Color(red = r.coerceIn(0f, 1f), green = g.coerceIn(0f, 1f), blue = b.coerceIn(0f, 1f), alpha = c.alpha)
}

/** Gelapkan warna aksen sedikit agar kontras di latar terang. */
internal fun darken(c: Color, amount: Float): Color =
    Color(
        red = (c.red * (1f - amount)).coerceIn(0f, 1f),
        green = (c.green * (1f - amount)).coerceIn(0f, 1f),
        blue = (c.blue * (1f - amount)).coerceIn(0f, 1f),
        alpha = c.alpha,
    )

// ------------------------------------------------------------------
// Palette dinamis
// ------------------------------------------------------------------

data class NanzMusifyPalette(
    val background: Color,
    val surface: Color,
    val elevated: Color,
    val textPrimary: Color,
    val textBright: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val line: Color,
    val lineSoft: Color,
    val scrim: Color,
    val crimson: Color,
    val rose: Color,
    // ---- token "chrome" (gelombang tema baru, lihat notes/04) ----
    /** Permukaan semi-transparan untuk chrome: nav bar, mini player, lembar bawah. */
    val surfaceTranslucent: Color,
    /** Garis rambut (≈8% putih / 12% tinta) — pengganti border tebal. */
    val hairline: Color,
    /** Aksen sangat redup untuk latar item terpilih / chip aktif. */
    val accentSoft: Color,
    /** Scrim lembar bawah & dialog: lebih ringan dari scrim penuh. */
    val scrimSheet: Color,
)

fun nanzmusifyDarkPalette(accent: Color = NanzMusifyCrimsonDefault) = NanzMusifyPalette(
    background = DarkBackground,
    surface = DarkSurface,
    elevated = DarkElevated,
    textPrimary = DarkTextPrimary,
    textBright = DarkTextBright,
    textSecondary = DarkTextSecondary,
    textMuted = DarkTextMuted,
    line = DarkLine,
    lineSoft = DarkLineSoft,
    scrim = DarkScrim,
    crimson = accent,
    rose = lighten(accent, 0.25f),
    surfaceTranslucent = DarkSurface.copy(alpha = 0.72f),
    hairline = Color(0x14FFFFFF),
    accentSoft = accent.copy(alpha = 0.14f),
    scrimSheet = Color(0xB3000000),
)

fun nanzmusifyLightPalette(accent: Color = NanzMusifyCrimsonDefault): NanzMusifyPalette {
    val strong = darken(accent, 0.08f)
    return NanzMusifyPalette(
        background = LightBackground,
        surface = LightSurface,
        elevated = LightElevated,
        textPrimary = LightTextPrimary,
        textBright = LightTextBright,
        textSecondary = LightTextSecondary,
        textMuted = LightTextMuted,
        line = LightLine,
        lineSoft = LightLineSoft,
        scrim = LightScrim,
        crimson = strong,
        rose = accent,
        surfaceTranslucent = LightSurface.copy(alpha = 0.78f),
        hairline = Color(0x1F23201E),
        accentSoft = strong.copy(alpha = 0.12f),
        scrimSheet = Color(0x6623201E),
    )
}

/**
 * Palet HITAM murni (mode OLED). Dipakai saat [ThemeMode.BLACK]: latar #000000,
 * permukaan hanya naik tipis, garis rambut lebih redup supaya tidak menyilaukan.
 */
fun nanzmusifyBlackPalette(accent: Color = NanzMusifyCrimsonDefault) = NanzMusifyPalette(
    background = BlackBackground,
    surface = BlackSurface,
    elevated = BlackElevated,
    textPrimary = BlackTextPrimary,
    textBright = DarkTextBright,
    textSecondary = BlackTextSecondary,
    textMuted = BlackTextMuted,
    line = BlackLine,
    lineSoft = BlackLineSoft,
    scrim = BlackScrim,
    crimson = accent,
    rose = lighten(accent, 0.22f),
    surfaceTranslucent = BlackSurface.copy(alpha = 0.74f),
    hairline = Color(0x12FFFFFF),
    accentSoft = accent.copy(alpha = 0.16f),
    scrimSheet = Color(0xCC000000),
)

/**
 * Petakan skema Material 3 (mis. dari Material You / `dynamicDarkColorScheme`)
 * ke palet NANZMUSIFY sehingga token lama tetap jalan saat warna dinamis aktif.
 *
 * @param blackBase paksa latar #000 (mode HITAM + warna dinamis).
 */
fun nanzmusifyPaletteFromScheme(s: androidx.compose.material3.ColorScheme, accent: Color, blackBase: Boolean = false): NanzMusifyPalette {
    val bg = if (blackBase) Color.Black else s.background
    val surface = if (blackBase) BlackSurface else s.surface
    val elevated = if (blackBase) BlackElevated else s.surfaceContainerHigh
    return NanzMusifyPalette(
        background = bg,
        surface = surface,
        elevated = elevated,
        textPrimary = if (blackBase) BlackTextPrimary else s.onBackground,
        textBright = if (blackBase) Color.White else s.onSurface,
        textSecondary = s.onSurfaceVariant,
        textMuted = s.onSurfaceVariant.copy(alpha = 0.75f),
        line = s.outlineVariant,
        lineSoft = s.outlineVariant.copy(alpha = 0.55f),
        scrim = s.scrim.copy(alpha = 0.9f),
        crimson = s.primary,
        rose = s.tertiary,
        surfaceTranslucent = s.surfaceContainer.copy(alpha = 0.74f),
        hairline = s.outlineVariant.copy(alpha = 0.5f),
        accentSoft = s.primary.copy(alpha = 0.15f),
        scrimSheet = s.scrim.copy(alpha = 0.7f),
    )
}

/** Palette aktif — di-provide oleh NanzMusifyTheme. */
val LocalNanzMusifyPalette = staticCompositionLocalOf { nanzmusifyDarkPalette() }

// ------------------------------------------------------------------
// Token publik — nama dipertahankan agar semua layar langsung ikut tema
// ------------------------------------------------------------------

val NanzMusifyBackground: Color @Composable @ReadOnlyComposable get() = LocalNanzMusifyPalette.current.background
val NanzMusifySurface: Color @Composable @ReadOnlyComposable get() = LocalNanzMusifyPalette.current.surface
val NanzMusifyElevated: Color @Composable @ReadOnlyComposable get() = LocalNanzMusifyPalette.current.elevated
val NanzMusifyTextPrimary: Color @Composable @ReadOnlyComposable get() = LocalNanzMusifyPalette.current.textPrimary
val NanzMusifyTextBright: Color @Composable @ReadOnlyComposable get() = LocalNanzMusifyPalette.current.textBright
val NanzMusifyTextSecondary: Color @Composable @ReadOnlyComposable get() = LocalNanzMusifyPalette.current.textSecondary
val NanzMusifyTextMuted: Color @Composable @ReadOnlyComposable get() = LocalNanzMusifyPalette.current.textMuted
val NanzMusifyLine: Color @Composable @ReadOnlyComposable get() = LocalNanzMusifyPalette.current.line
val NanzMusifyLineSoft: Color @Composable @ReadOnlyComposable get() = LocalNanzMusifyPalette.current.lineSoft
val NanzMusifyScrim: Color @Composable @ReadOnlyComposable get() = LocalNanzMusifyPalette.current.scrim
val NanzMusifyCrimson: Color @Composable @ReadOnlyComposable get() = LocalNanzMusifyPalette.current.crimson
val NanzMusifyRose: Color @Composable @ReadOnlyComposable get() = LocalNanzMusifyPalette.current.rose
val NanzMusifySurfaceTranslucent: Color @Composable @ReadOnlyComposable get() = LocalNanzMusifyPalette.current.surfaceTranslucent
val NanzMusifyHairline: Color @Composable @ReadOnlyComposable get() = LocalNanzMusifyPalette.current.hairline
val NanzMusifyAccentSoft: Color @Composable @ReadOnlyComposable get() = LocalNanzMusifyPalette.current.accentSoft
val NanzMusifyScrimSheet: Color @Composable @ReadOnlyComposable get() = LocalNanzMusifyPalette.current.scrimSheet

// Token lama yang jarang dipakai — dipertahankan sebagai konstanta statis
val NanzMusifyTextSoft = Color(0xFFCDCDD6)
val NanzMusifyTextDim = Color(0xFF4E4E5A)
val NanzMusifyTextFaint = Color(0xFF7A7A86)
val NanzMusifySurfaceAlt = Color(0xFF12121A)
val NanzMusifyStroke = Color(0x1AFFFFFF)
val NanzMusifyOverlay = Color(0xCC0B0B10)
val NanzMusifyPaperBackground = Color(0xFFF0EBE4)
val NanzMusifyPaperSurface = Color(0xFFF7F3ED)
val NanzMusifyPaperText = Color(0xFF23201E)
