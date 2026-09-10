/*
 * Copyright (C) 2026 Nanas
 *
 * SPDX-License-Identifier: GPL-3.0-only
 */
package com.alfath.musicall.app.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alfath.musicall.app.R
import com.alfath.musicall.app.data.model.NanzMusifyTrack
import com.alfath.musicall.app.player.PlayerManager
import com.alfath.musicall.app.ui.theme.NanzMusifyTextSecondary
import com.alfath.musicall.app.ui.theme.NanzMusifyTextPrimary
import com.alfath.musicall.app.ui.theme.NanzMusifyElevated
import com.alfath.musicall.app.ui.theme.NanzMusifySurface
import com.alfath.musicall.app.ui.theme.NanzMusifyCrimson
import com.alfath.musicall.app.ui.theme.NanzMusifyTextMuted
import androidx.compose.foundation.border
import com.alfath.musicall.app.ui.theme.NanzMusifyHairline
import com.alfath.musicall.app.ui.theme.NanzMusifyMotion
import com.alfath.musicall.app.ui.theme.NanzMusifyRadius
import com.alfath.musicall.app.ui.theme.nanzmusifyChromeEnter
import com.alfath.musicall.app.ui.theme.nanzmusifyChromeExit
import com.alfath.musicall.app.ui.theme.nanzmusifyTween

/** Sudut mini player: sedang, selaras kartu (skala radius tema). */
private val NanzMusifyMiniShape = RoundedCornerShape(NanzMusifyRadius.md)

@Composable
fun MiniPlayerBar(
    track: NanzMusifyTrack?,
    isPlaying: Boolean,
    isBuffering: Boolean,
    player: PlayerManager,
    visible: Boolean,
    onToggle: () -> Unit,
    onNext: () -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible && track != null,
        enter = nanzmusifyChromeEnter(),
        exit = nanzmusifyChromeExit(),
        modifier = modifier,
    ) {
        if (track == null) return@AnimatedVisibility

        // Progress dikoleksi LOKAL di widget ini — hanya bar kecil ini yang
        // recompose tiap 500ms, bukan seluruh layar di belakangnya.
        val pos by player.position.collectAsStateWithLifecycle()
        val progress = if (pos.durationMs > 0) pos.positionMs.toFloat() / pos.durationMs else 0f
        val p by animateFloatAsState(
            targetValue = progress.coerceIn(0f, 1f),
            animationSpec = nanzmusifyTween(NanzMusifyMotion.deliberate),
            label = "mini_prog",
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .clip(NanzMusifyMiniShape)
                .background(NanzMusifyElevated)
                .border(1.dp, NanzMusifyHairline, NanzMusifyMiniShape)
                .clickable(onClick = onOpen),
        ) {
            if (isBuffering) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().height(2.dp),
                    color = NanzMusifyCrimson,
                    trackColor = NanzMusifySurface,
                )
            } else {
                LinearProgressIndicator(
                    progress = { p },
                    modifier = Modifier.fillMaxWidth().height(2.dp),
                    color = NanzMusifyCrimson,
                    trackColor = NanzMusifySurface,
                    strokeCap = StrokeCap.Butt,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Artwork(url = track.thumbnailUrl, title = track.title, size = 44.dp)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = NanzMusifyTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = track.artist.ifBlank { stringResource(R.string.common_youtube_music) },
                        style = MaterialTheme.typography.labelSmall,
                        color = NanzMusifyTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                NanzMusifyMiniPlay(isPlaying = isPlaying, onClick = onToggle, size = 40.dp)
                IconButton(onClick = onNext) {
                    Icon(
                        imageVector = Icons.Filled.SkipNext,
                        contentDescription = stringResource(R.string.np_next),
                        tint = NanzMusifyTextPrimary,
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(NanzMusifyTextMuted.copy(alpha = 0.25f)),
            )
        }
    }
}
