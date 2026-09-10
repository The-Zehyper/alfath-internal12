/*
 * Copyright (C) 2026 Nanas
 *
 * SPDX-License-Identifier: GPL-3.0-only
 */
package com.alfath.musicall.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.alfath.musicall.app.R
import com.alfath.musicall.app.data.db.DownloadEntity
import com.alfath.musicall.app.data.db.DownloadState
import com.alfath.musicall.app.data.model.NanzMusifyTrack
import com.alfath.musicall.app.player.PlayerUiState
import com.alfath.musicall.app.ui.components.Artwork
import com.alfath.musicall.app.ui.components.NanzMusifyMiniPlay
import com.alfath.musicall.app.ui.theme.NanzMusifyTextSecondary
import com.alfath.musicall.app.ui.theme.NanzMusifyTextPrimary
import com.alfath.musicall.app.ui.theme.NanzMusifyLine
import com.alfath.musicall.app.ui.theme.NanzMusifySurface
import com.alfath.musicall.app.ui.theme.NanzMusifyBackground
import com.alfath.musicall.app.ui.theme.NanzMusifyCrimson
import com.alfath.musicall.app.ui.theme.NanzMusifyRose
import com.alfath.musicall.app.ui.theme.NanzMusifyTextMuted
import com.alfath.musicall.app.ui.vm.DownloadsViewModel

@Composable
fun DownloadsScreen(
    vm: DownloadsViewModel,
    playerState: PlayerUiState,
    onBack: () -> Unit,
    onPlayQueue: (List<NanzMusifyTrack>, Int) -> Unit,
    modifier: Modifier = Modifier,
    asRootTab: Boolean = false,
) {
    val downloads by vm.downloads.collectAsStateWithLifecycle()
    val done = downloads.filter { it.state == DownloadState.DONE }
    val active = downloads.filter { it.state == DownloadState.QUEUED || it.state == DownloadState.DOWNLOADING }
    val failed = downloads.filter { it.state == DownloadState.ERROR || it.state == DownloadState.CANCELED }

    val context = LocalContext.current
    var isOnline by remember { mutableStateOf(true) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        val cm = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val network = cm?.activeNetwork
        val caps = network?.let { cm.getNetworkCapabilities(it) }
        isOnline = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NanzMusifyBackground),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (asRootTab) 20.dp else 8.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!asRootTab) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back), tint = NanzMusifyTextPrimary)
                }
            }
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.dl_kicker),
                        style = MaterialTheme.typography.headlineSmall,
                        color = NanzMusifyTextPrimary,
                    )
                    Spacer(Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .border(1.dp, NanzMusifyLine)
                            .padding(horizontal = 10.dp, vertical = 3.dp),
                    ) {
                        Text(
                            if (isOnline) stringResource(R.string.status_online) else stringResource(R.string.status_offline),
                            style = MaterialTheme.typography.labelSmall,
                            color = NanzMusifyTextSecondary,
                        )
                    }
                }
                Text(stringResource(R.string.downloads_title), style = MaterialTheme.typography.labelSmall, color = NanzMusifyTextMuted)
            }
            Box(
                modifier = Modifier
                    .background(NanzMusifySurface)
                    .padding(8.dp),
            ) {
                Icon(Icons.Filled.CloudOff, contentDescription = null, tint = NanzMusifyTextSecondary)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp),
        ) {
            if (done.isNotEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp)
                            .border(1.dp, NanzMusifyCrimson)
                            .background(NanzMusifyCrimson.copy(alpha = 0.15f))
                            .clickable {
                                onPlayQueue(done.map { it.toTrack() }, 0)
                            }
                            .padding(14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            stringResource(R.string.dl_play_all_offline, done.size),
                            style = MaterialTheme.typography.labelMedium,
                            color = NanzMusifyTextPrimary,
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(10.dp))
                Text(
                    stringResource(R.string.dl_storage_note),
                    style = MaterialTheme.typography.labelSmall,
                    color = NanzMusifyTextMuted,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
                Spacer(Modifier.height(10.dp))
            }

            if (downloads.isEmpty()) {
                item {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 24.dp)
                            .border(1.dp, NanzMusifyLine)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier
                                .background(NanzMusifySurface)
                                .padding(16.dp),
                        ) {
                            Icon(Icons.Filled.CloudOff, contentDescription = null, tint = NanzMusifyTextSecondary)
                        }
                        Spacer(Modifier.height(14.dp))
                        Text(stringResource(R.string.downloads_empty), style = MaterialTheme.typography.titleSmall, color = NanzMusifyTextPrimary)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Ketuk menu Lainnya pada lagu, lalu pilih UNDUH (OFFLINE). Lagu tersimpan sebagai audio-only dan otomatis diputar offline saat sinyal buruk.",
                            style = MaterialTheme.typography.bodySmall,
                            color = NanzMusifyTextMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                    }
                }
            }

            if (active.isNotEmpty()) {
                item { DownloadHeader("SEDANG DIPROSES · ${active.size}") }
                items(active, key = { it.videoId }) { entry ->
                    DownloadRow(
                        entry = entry,
                        onCancel = { vm.cancel(entry.videoId) },
                        onDelete = { vm.remove(entry.videoId) },
                        onRetry = { vm.retry(entry.videoId) },
                        onPlay = null,
                    )
                }
            }

            if (done.isNotEmpty()) {
                item { DownloadHeader("TERSIMPAN · ${done.size}") }
                items(done, key = { it.videoId }) { entry ->
                    val activePlaying = playerState.currentTrack?.videoId == entry.videoId
                    DownloadRow(
                        entry = entry,
                        onCancel = null,
                        onDelete = { vm.remove(entry.videoId) },
                        onRetry = null,
                        onPlay = { onPlayQueue(done.map { it.toTrack() }, done.indexOf(entry)) },
                        activePlaying = activePlaying,
                        isPlaying = playerState.isPlaying,
                    )
                }
            }

            if (failed.isNotEmpty()) {
                item { DownloadHeader("PERLU TINDAKAN · ${failed.size}") }
                items(failed, key = { it.videoId }) { entry ->
                    DownloadRow(
                        entry = entry,
                        onCancel = null,
                        onDelete = { vm.remove(entry.videoId) },
                        onRetry = { vm.retry(entry.videoId) },
                        onPlay = null,
                    )
                }
            }
        }
    }
}

private fun DownloadEntity.toTrack() =
    NanzMusifyTrack(videoId, title, artist, album, durationSec, thumbnailUrl)

@Composable
private fun DownloadHeader(label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 18.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = NanzMusifyTextSecondary)
        Box(Modifier.weight(1f).height(1.dp).background(NanzMusifyLine))
    }
}

@Composable
private fun DownloadRow(
    entry: DownloadEntity,
    onCancel: (() -> Unit)?,
    onDelete: () -> Unit,
    onRetry: (() -> Unit)?,
    onPlay: (() -> Unit)?,
    activePlaying: Boolean = false,
    isPlaying: Boolean = false,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .border(1.dp, if (activePlaying) NanzMusifyCrimson else NanzMusifyLine)
            .background(NanzMusifySurface.copy(alpha = 0.3f))
            .clickable(enabled = onPlay != null) { onPlay?.invoke() }
            .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Artwork(url = entry.thumbnailUrl, title = entry.title, size = 48.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    entry.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = NanzMusifyTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    entry.artist.ifBlank { stringResource(R.string.common_youtube) },
                    style = MaterialTheme.typography.labelSmall,
                    color = NanzMusifyTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    when (entry.state) {
                        DownloadState.DONE -> "OFFLINE · ${(entry.bytesTotal / 1024 / 1024)} MB"
                        DownloadState.DOWNLOADING -> stringResource(R.string.dl_downloading)
                        DownloadState.QUEUED -> stringResource(R.string.dl_queued)
                        DownloadState.ERROR -> "GAGAL · ${entry.errorMessage ?: ""}"
                        else -> stringResource(R.string.dl_canceled)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = when (entry.state) {
                        DownloadState.DONE -> NanzMusifyRose
                        DownloadState.ERROR -> NanzMusifyCrimson
                        else -> NanzMusifyTextMuted
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (onPlay != null) {
                NanzMusifyMiniPlay(isPlaying = activePlaying && isPlaying, onClick = onPlay, size = 36.dp)
            }
            if (onCancel != null) {
                IconButton(onClick = onCancel) {
                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.action_cancel), tint = NanzMusifyTextSecondary)
                }
            }
            if (onRetry != null) {
                IconButton(onClick = onRetry) {
                    Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.action_retry), tint = NanzMusifyTextSecondary)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.DeleteOutline, contentDescription = stringResource(R.string.action_delete), tint = NanzMusifyTextMuted)
            }
        }

        if (entry.state == DownloadState.DOWNLOADING || entry.state == DownloadState.QUEUED) {
            Spacer(Modifier.height(8.dp))
            if (entry.bytesTotal > 0) {
                val p = (entry.bytesDone.toFloat() / entry.bytesTotal.toFloat()).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { p },
                    modifier = Modifier.fillMaxWidth().height(2.dp),
                    color = NanzMusifyCrimson,
                    trackColor = NanzMusifySurface,
                    strokeCap = StrokeCap.Butt,
                )
            } else {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().height(2.dp),
                    color = NanzMusifyCrimson,
                    trackColor = NanzMusifySurface,
                )
            }
        }
    }
}
