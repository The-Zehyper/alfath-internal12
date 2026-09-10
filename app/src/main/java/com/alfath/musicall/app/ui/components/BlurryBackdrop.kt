/*
 * Copyright (C) 2026 Nanas
 *
 * SPDX-License-Identifier: GPL-3.0-only
 */
package com.alfath.musicall.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.alfath.musicall.app.ui.theme.NanzMusifyBackground

/**
 * Backdrop statis ringan (tanpa crossfade/parallax penuh layar —
 * penyebab utama lag saat berpindah halaman).
 */
@Composable
fun BlurryBackdrop(
    @DrawableRes imageRes: Int,
    parallaxY: Float,
    modifier: Modifier = Modifier,
    dim: Float = 0.72f,
) {
    val desaturate = ColorFilter.colorMatrix(
        ColorMatrix().apply { setToSaturation(0.4f) },
    )

    // Warna tema dibaca di composable scope — token palet kini dinamis,
    // sehingga tidak boleh disentuh dari lambda draw (non-composable).
    val background = NanzMusifyBackground
    val scrim = Brush.verticalGradient(
        colors = listOf(
            background.copy(alpha = 0.55f),
            background.copy(alpha = dim),
            background,
        ),
    )

    Box(modifier = modifier.fillMaxSize().background(background)) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            colorFilter = desaturate,
            alpha = 0.45f,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind { drawRect(scrim) },
        )
    }
}
