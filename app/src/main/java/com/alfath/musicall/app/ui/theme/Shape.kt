/*
 * Copyright (C) 2026 Nanas
 *
 * SPDX-License-Identifier: GPL-3.0-only
 */
package com.alfath.musicall.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ------------------------------------------------------------------
// Skala radius NANZMUSIFY — lihat notes/04-spesifikasi-tema-baru.md §Radius
//
// Hanya sebagian elemen yang membulat: chrome (nav bar, mini player, lembar
// bawah) membulat besar, kartu sedang, kontrol kecil rapat. Jangan memakai
// nilai dp acak di layar — selalu ambil dari skala ini agar konsisten.
// ------------------------------------------------------------------

object NanzMusifyRadius {
    /** Kontrol kecil: chip, badge, tombol ikon. */
    val xs: Dp = 8.dp

    /** Baris/item & elemen mini player. */
    val sm: Dp = 12.dp

    /** Kartu standar (BrutalFrame). */
    val md: Dp = 16.dp

    /** Lembar bawah & dialog: sudut atas saja. */
    val lg: Dp = 24.dp

    /** Chrome melayang ala iOS: nav bar island. */
    val xl: Dp = 28.dp

    /** Pil penuh (tombol aksen, filter aktif). */
    val pill: Dp = 999.dp

    fun rounded(value: Dp): Shape = RoundedCornerShape(value)

    /** Sudat membulat hanya di atas — untuk lembar bawah. */
    fun top(value: Dp = lg): Shape = RoundedCornerShape(topStart = value, topEnd = value)
}

/** Skala radius yang diikat ke MaterialTheme.shapes agar komponen M3 ikut. */
val NanzMusifyShapes: Shapes = Shapes(
    extraSmall = RoundedCornerShape(NanzMusifyRadius.xs),
    small = RoundedCornerShape(NanzMusifyRadius.sm),
    medium = RoundedCornerShape(NanzMusifyRadius.md),
    large = RoundedCornerShape(NanzMusifyRadius.lg),
    extraLarge = RoundedCornerShape(NanzMusifyRadius.xl),
)

/** Bentuk chrome melayang (nav bar island, mini player). */
val NanzMusifyChromeShape: Shape = RoundedCornerShape(NanzMusifyRadius.xl)
