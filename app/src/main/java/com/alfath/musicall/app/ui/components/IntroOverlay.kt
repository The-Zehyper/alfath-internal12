/*
 * Copyright (C) 2026 Nanas
 *
 * SPDX-License-Identifier: GPL-3.0-only
 */
package com.alfath.musicall.app.ui.components

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alfath.musicall.app.R
import com.alfath.musicall.app.ui.theme.NanzMusifyBackground
import com.alfath.musicall.app.ui.theme.NanzMusifyCrimson
import com.alfath.musicall.app.ui.theme.NanzMusifySurface
import com.alfath.musicall.app.ui.theme.NanzMusifyTextMuted
import com.alfath.musicall.app.ui.theme.NanzMusifyTextPrimary
import com.alfath.musicall.app.ui.theme.NanzMusifyMotion
import com.alfath.musicall.app.ui.theme.nanzmusifyTween
import com.alfath.musicall.app.ui.theme.reduceMotionEnabled
import kotlinx.coroutines.delay

/**
 * Intro saat membuka aplikasi — logo NANZMUSIFY membesar halus + strip loading
 * berwarna aksen tema, lalu memudar keluar. Murni Compose (stabil di semua API).
 * Ditampilkan singkat (~1,9 detik) agar tetap terasa instan ala iOS.
 */
@Composable
fun NanzMusifyIntroOverlay() {
    var visible by remember { mutableStateOf(true) }
    var entered by remember { mutableStateOf(false) }

    // Reduce motion: intro dipersingkat dan tanpa efek membesar.
    val reduced = reduceMotionEnabled
    LaunchedEffect(Unit) {
        entered = true
        delay(if (reduced) 550L else 1900L)
        visible = false
    }

    val scale by animateFloatAsState(
        targetValue = if (entered || reduced) 1f else 0.72f,
        animationSpec = nanzmusifyTween(NanzMusifyMotion.deliberate),
        label = "logo_scale",
    )
    val alpha by animateFloatAsState(
        targetValue = if (entered || reduced) 1f else 0f,
        animationSpec = nanzmusifyTween(NanzMusifyMotion.normal),
        label = "logo_alpha",
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(nanzmusifyTween(NanzMusifyMotion.normal)),
        exit = fadeOut(nanzmusifyTween(NanzMusifyMotion.normal)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NanzMusifyBackground),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(R.mipmap.ic_launcher),
                    contentDescription = null,
                    modifier = Modifier
                        .size(96.dp)
                        .scale(scale)
                        .alpha(alpha)
                        .clip(RoundedCornerShape(26.dp)),
                )
                Spacer(Modifier.height(20.dp))
                Text(
                    stringResource(R.string.app_name).uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = NanzMusifyTextPrimary.copy(alpha = alpha),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    stringResource(R.string.tagline),
                    style = MaterialTheme.typography.labelSmall,
                    color = NanzMusifyTextMuted.copy(alpha = alpha),
                )
                Spacer(Modifier.height(26.dp))
                LinearProgressIndicator(
                    modifier = Modifier
                        .width(120.dp)
                        .height(3.dp)
                        .alpha(alpha),
                    color = NanzMusifyCrimson,
                    trackColor = NanzMusifySurface,
                )
            }
        }
    }
}
