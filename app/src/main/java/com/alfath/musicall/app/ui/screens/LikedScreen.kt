/*
 * Copyright (C) 2026 Nanas
 *
 * SPDX-License-Identifier: GPL-3.0-only
 */
package com.alfath.musicall.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alfath.musicall.app.R
import com.alfath.musicall.app.data.model.NanzMusifyTrack
import com.alfath.musicall.app.player.PlayerUiState
import com.alfath.musicall.app.ui.components.TrackRow
import com.alfath.musicall.app.ui.theme.NanzMusifyBackground
import com.alfath.musicall.app.ui.theme.NanzMusifyCrimson
import com.alfath.musicall.app.ui.theme.NanzMusifySurface
import com.alfath.musicall.app.ui.theme.NanzMusifyTextMuted
import com.alfath.musicall.app.ui.theme.NanzMusifyTextPrimary
import com.alfath.musicall.app.ui.theme.NanzMusifyTextSecondary
import com.alfath.musicall.app.ui.vm.LibraryViewModel

/**
 * Layar "Liked Songs" tersendiri (bukan sub-tab Library lagi) — sesuai desain
 * referensi: judul besar + lencana hati di kanan atas, daftar lagu disukai,
 * dan status kosong berupa hati bulat di tengah.
 */
@Composable
fun LikedScreen(
    vm: LibraryViewModel,
    playerState: PlayerUiState,
    onPlayQueue: (List<NanzMusifyTrack>, Int) -> Unit,
    onTrackMore: (NanzMusifyTrack) -> Unit,
    onLike: (NanzMusifyTrack) -> Unit,
    likedIds: Set<String>,
    downloadedIds: Set<String>,
    modifier: Modifier = Modifier,
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val liked = state.liked

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NanzMusifyBackground),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.liked_title),
                style = MaterialTheme.typography.headlineMedium,
                color = NanzMusifyTextPrimary,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .background(NanzMusifySurface)
                    .padding(10.dp),
            ) {
                Icon(Icons.Filled.Favorite, contentDescription = null, tint = NanzMusifyCrimson)
            }
        }

        if (liked.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .border(1.dp, com.alfath.musicall.app.ui.theme.NanzMusifyLine)
                    .padding(vertical = 40.dp, horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .background(NanzMusifySurface)
                        .padding(20.dp),
                ) {
                    Icon(
                        Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = NanzMusifyTextSecondary,
                        modifier = Modifier.height(32.dp),
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    stringResource(R.string.liked_empty_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = NanzMusifyTextPrimary,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.liked_empty_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = NanzMusifyTextMuted,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp),
            ) {
                itemsIndexed(liked, key = { _, t -> t.videoId }) { index, track ->
                    TrackRow(
                        track = track,
                        isActive = playerState.currentTrack?.videoId == track.videoId,
                        isPlaying = playerState.isPlaying,
                        isLiked = likedIds.contains(track.videoId),
                        isDownloaded = downloadedIds.contains(track.videoId),
                        index = index,
                        onPlay = { onPlayQueue(liked, index) },
                        onLike = { onLike(track) },
                        onMore = { onTrackMore(track) },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}
