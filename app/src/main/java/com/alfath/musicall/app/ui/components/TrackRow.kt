/*
 * Copyright (C) 2026 Nanas
 *
 * SPDX-License-Identifier: GPL-3.0-only
 */
package com.alfath.musicall.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.alfath.musicall.app.R
import com.alfath.musicall.app.data.model.NanzMusifyTrack
import com.alfath.musicall.app.data.model.formatDuration
import com.alfath.musicall.app.ui.theme.NanzMusifyTextSecondary
import com.alfath.musicall.app.ui.theme.NanzMusifyTextPrimary
import com.alfath.musicall.app.ui.theme.NanzMusifyLine
import com.alfath.musicall.app.ui.theme.NanzMusifySurface
import com.alfath.musicall.app.ui.theme.NanzMusifyCrimson
import com.alfath.musicall.app.ui.theme.NanzMusifyRose
import com.alfath.musicall.app.ui.theme.NanzMusifyTextMuted

/** Baris track — tap = putar; tombol hati & overflow di kanan. */
@Composable
fun TrackRow(
    track: NanzMusifyTrack,
    isActive: Boolean,
    isPlaying: Boolean,
    isLiked: Boolean,
    isDownloaded: Boolean,
    index: Int?,
    onPlay: () -> Unit,
    onLike: () -> Unit,
    onMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Statis, tanpa animasi per-baris (daftar panjang = hemat GPU)
    val borderColor = if (isActive) NanzMusifyCrimson.copy(alpha = 0.45f) else NanzMusifyLine
    val bgColor = if (isActive) NanzMusifySurface.copy(alpha = 0.7f) else NanzMusifySurface.copy(alpha = 0.35f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, borderColor)
            .background(bgColor)
            .clickable(onClick = onPlay)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (index != null) {
            Text(
                text = "%02d".format(index + 1),
                style = MaterialTheme.typography.labelMedium,
                color = if (isActive) NanzMusifyCrimson else NanzMusifyTextMuted,
                modifier = Modifier.width(32.dp),
            )
        } else {
            Spacer(Modifier.width(8.dp))
        }

        Box(
            modifier = Modifier
                .size(56.dp)
                .clickable(onClick = onPlay),
        ) {
            Artwork(url = track.thumbnailUrl, title = track.title, size = 56.dp)
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(NanzMusifySurface.copy(alpha = if (isActive) 0.35f else 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                NanzMusifyMiniPlay(
                    isPlaying = isActive && isPlaying,
                    onClick = onPlay,
                    size = 28.dp,
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.titleMedium,
                color = NanzMusifyTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = track.artist.ifBlank { track.album }.ifBlank { stringResource(R.string.common_youtube) },
                style = MaterialTheme.typography.bodySmall,
                color = NanzMusifyTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (isDownloaded) {
                Text(
                    text = stringResource(R.string.common_offline),
                    style = MaterialTheme.typography.labelSmall,
                    color = NanzMusifyRose,
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatDuration(track.durationSec),
                style = MaterialTheme.typography.labelSmall,
                color = NanzMusifyTextSecondary,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onLike, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = stringResource(R.string.np_like_add),
                        tint = if (isLiked) NanzMusifyCrimson else NanzMusifyTextSecondary,
                        modifier = Modifier.size(18.dp),
                    )
                }
                IconButton(onClick = onMore, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "Lainnya",
                        tint = NanzMusifyTextSecondary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}
