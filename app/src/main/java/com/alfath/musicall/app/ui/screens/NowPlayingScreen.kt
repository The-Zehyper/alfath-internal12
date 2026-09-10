/*
 * Copyright (C) 2026 Nanas
 *
 * SPDX-License-Identifier: GPL-3.0-only
 */
package com.alfath.musicall.app.ui.screens

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import coil3.compose.AsyncImage
import com.alfath.musicall.app.R
import com.alfath.musicall.app.data.model.NanzMusifyTrack
import com.alfath.musicall.app.data.model.formatMs
import com.alfath.musicall.app.player.PlayerManager
import com.alfath.musicall.app.player.PlayerUiState
import com.alfath.musicall.app.ui.components.Artwork
import com.alfath.musicall.app.ui.components.LyricsContent
import com.alfath.musicall.app.ui.components.NanzMusifyPlayButton
import com.alfath.musicall.app.ui.theme.NanzMusifyTextSecondary
import com.alfath.musicall.app.ui.theme.NanzMusifyTextPrimary
import com.alfath.musicall.app.ui.theme.NanzMusifyElevated
import com.alfath.musicall.app.ui.theme.NanzMusifySurface
import com.alfath.musicall.app.ui.theme.NanzMusifyBackground
import com.alfath.musicall.app.ui.theme.NanzMusifyCrimson
import com.alfath.musicall.app.ui.theme.NanzMusifyRose
import com.alfath.musicall.app.ui.theme.NanzMusifyTextMuted
import com.alfath.musicall.app.ui.theme.NanzMusifyMotion
import com.alfath.musicall.app.ui.theme.nanzmusifyTween
import com.alfath.musicall.app.ui.theme.NanzMusifyRadius
import com.alfath.musicall.app.ui.theme.NanzMusifyScrimSheet
import com.alfath.musicall.app.ui.theme.NanzMusifyLine
import com.alfath.musicall.app.ui.theme.NanzMusifyHairline
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.rememberCoroutineScope
import com.alfath.musicall.app.data.settings.NanzMusifySettings
import com.alfath.musicall.app.ui.vm.LocalNanzMusify
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

import com.alfath.musicall.app.ui.theme.rememberDynamicArtworkPalette
import com.alfath.musicall.app.ui.theme.DynamicArtworkPalette
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.DeleteSweep

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    playerState: PlayerUiState,
    player: PlayerManager,
    isLiked: Boolean,
    isDownloaded: Boolean,
    videoMode: Boolean,
    controller: Player?,
    onBack: () -> Unit,
    onToggle: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onSeekFraction: (Float) -> Unit,
    onShuffle: (Boolean) -> Unit,
    onCycleRepeat: () -> Unit,
    onLike: () -> Unit,
    onDownload: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onSleepTimer: () -> Unit,
    onShare: () -> Unit,
    onPlayAt: (Int) -> Unit,
    onRemoveQueueItem: (Int) -> Unit,
    onMoveQueueItem: (Int, Int) -> Unit,
    onSetVideoMode: (Boolean) -> Unit,
    onSeekMs: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val track = playerState.currentTrack
    val dynamicPalette = rememberDynamicArtworkPalette(track?.thumbnailUrl)

    var sliderValue by remember { mutableFloatStateOf(0f) }
    var dragging by remember { mutableStateOf(false) }
    var showQueue by remember { mutableStateOf(false) }
    var showLyrics by remember { mutableStateOf(false) }
    var showPlaybackParams by remember { mutableStateOf(false) }

    // Posisi/durasi dari flow khusus (ticker 500ms)
    val pos by player.position.collectAsStateWithLifecycle()

    // Foto kanal uploader lagu berjalan (lazy per lagu; null → monogram huruf)
    val locator = com.alfath.musicall.app.ui.vm.LocalNanzMusify.current
    val artistAvatarUrl by androidx.compose.runtime.produceState<String?>(
        initialValue = null,
        track?.videoId,
    ) {
        value = null
        val t = track
        if (t != null && !t.isLocal) {
            val byVideo = runCatching { locator.youtube.uploaderAvatar(t.videoId) }.getOrNull()
            value = byVideo?.takeIf { it.isNotBlank() } ?: runCatching {
                locator.youtube.channelProfile(t.artist)?.first
            }.getOrNull()
        }
    }

    val progress = if (pos.durationMs > 0) {
        pos.positionMs.toFloat() / pos.durationMs.toFloat()
    } else 0f

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(NanzMusifyBackground),
    ) {
        val wide = maxWidth >= 700.dp

        // Latar dinamis: gradien adaptif dari palet warna sampul musik
        Box(
            Modifier
                .fillMaxSize()
                .background(dynamicPalette.ambientGradient),
        )

        if (track == null) {
            Column(
                Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(stringResource(R.string.np_empty_title), style = MaterialTheme.typography.titleMedium, color = NanzMusifyTextSecondary)
                Spacer(Modifier.height(10.dp))
                Text(
                    stringResource(R.string.np_empty_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = NanzMusifyTextMuted,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(20.dp))
                Text(
                    stringResource(R.string.action_close),
                    style = MaterialTheme.typography.labelMedium,
                    color = dynamicPalette.accent,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .border(1.dp, dynamicPalette.accent.copy(alpha = 0.6f), RoundedCornerShape(50))
                        .clickable(onClick = onBack)
                        .padding(horizontal = 24.dp, vertical = 10.dp),
                )
            }
            return@BoxWithConstraints
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = if (wide) 48.dp else 24.dp)
                .widthIn(max = 560.dp)
                .align(Alignment.Center),
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = stringResource(R.string.action_close), tint = NanzMusifyTextPrimary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.np_kicker), style = MaterialTheme.typography.labelMedium, color = NanzMusifyTextSecondary)
                    Text(
                        when {
                            playerState.isBuffering -> stringResource(R.string.np_loading_stream)
                            track.isLocal -> stringResource(R.string.np_local_file)
                            videoMode -> stringResource(R.string.np_video)
                            else -> stringResource(R.string.np_audio_only)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = NanzMusifyTextMuted,
                    )
                }
                if (track.isLocal) {
                    Spacer(Modifier.size(48.dp))
                } else {
                    IconButton(onClick = onShare) {
                        Icon(Icons.Filled.Share, contentDescription = stringResource(R.string.np_share), tint = NanzMusifyTextPrimary)
                    }
                }
            }

            // Segmented MUSIK | VIDEO ala iOS
            if (!track.isLocal) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clip(RoundedCornerShape(50))
                        .background(NanzMusifySurface.copy(alpha = 0.6f))
                        .border(1.dp, NanzMusifyLine.copy(alpha = 0.3f), RoundedCornerShape(50))
                        .padding(4.dp),
                ) {
                    ModePill(
                        label = stringResource(R.string.np_music),
                        selected = !videoMode,
                        accentColor = dynamicPalette.accent,
                        onClick = { onSetVideoMode(false) },
                    )
                    Spacer(Modifier.width(4.dp))
                    ModePill(
                        label = stringResource(R.string.np_video),
                        selected = videoMode,
                        accentColor = dynamicPalette.accent,
                        onClick = { onSetVideoMode(true) },
                    )
                }
            }

            // Segmented Cover | Lirik ala referensi — selalu ada di mode musik
            if (!videoMode) {
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clip(RoundedCornerShape(50))
                        .background(NanzMusifySurface.copy(alpha = 0.6f))
                        .border(1.dp, NanzMusifyLine.copy(alpha = 0.3f), RoundedCornerShape(50))
                        .padding(4.dp),
                ) {
                    ModePill(
                        label = stringResource(R.string.np_cover),
                        selected = !showLyrics,
                        accentColor = dynamicPalette.accent,
                        onClick = { showLyrics = false },
                    )
                    Spacer(Modifier.width(4.dp))
                    ModePill(
                        label = stringResource(R.string.np_lyrics),
                        selected = showLyrics,
                        accentColor = dynamicPalette.accent,
                        onClick = { showLyrics = true },
                    )
                }
            }

            Spacer(Modifier.height(if (wide) 24.dp else 14.dp))

            // Kanvas utama: artwork (mode musik) atau PlayerView (mode video)
            if (videoMode && controller != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(NanzMusifySurface),
                ) {
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                useController = true
                                keepScreenOn = true
                                setShutterBackgroundColor(android.graphics.Color.BLACK)
                            }
                        },
                        update = { view -> view.player = controller },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            } else {
                ArtworkOrLyrics(
                    track = track,
                    isDownloaded = isDownloaded,
                    showLyrics = showLyrics,
                    positionMs = pos.positionMs,
                    accentColor = dynamicPalette.accent,
                    onToggleLyrics = { showLyrics = !showLyrics },
                    onSeekMs = onSeekMs,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(Modifier.height(22.dp))

            // Meta: judul + artis di kiri, hati di kanan
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        track.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = NanzMusifyTextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(6.dp))
                    val artistName = track.artist.ifBlank { stringResource(R.string.common_youtube_music) }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ArtistAvatar(name = artistName, url = artistAvatarUrl)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            artistName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = NanzMusifyTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                IconButton(onClick = onLike) {
                    Icon(
                        if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = stringResource(if (isLiked) R.string.np_like_remove else R.string.np_like_add),
                        tint = if (isLiked) dynamicPalette.accent else NanzMusifyTextSecondary,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
            Text(
                stringResource(R.string.np_audio_queue, playerState.currentIndex + 1, playerState.queue.size),
                style = MaterialTheme.typography.labelSmall,
                color = NanzMusifyTextMuted,
            )

            // Pratinjau "Berikutnya" (pola Now Playing Metrolist/Meld): dua lagu
            // setelah yang berjalan, ketuk = langsung lompat. Menghilang sendiri
            // saat tidak ada lagu tersisa, jadi tidak menambah kekacauan.
            UpNextBlock(
                queue = playerState.queue,
                currentIndex = playerState.currentIndex,
                accentColor = dynamicPalette.accent,
                onPlayAt = onPlayAt,
            )

            Spacer(Modifier.height(18.dp))

            // Seek
            Slider(
                value = if (dragging) sliderValue else progress.coerceIn(0f, 1f),
                onValueChange = {
                    dragging = true
                    sliderValue = it
                },
                onValueChangeFinished = {
                    onSeekFraction(sliderValue)
                    dragging = false
                },
                colors = SliderDefaults.colors(
                    thumbColor = NanzMusifyTextPrimary,
                    activeTrackColor = dynamicPalette.accent,
                    inactiveTrackColor = NanzMusifySurface.copy(alpha = 0.5f),
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    formatMs(if (dragging) (sliderValue * pos.durationMs).toLong() else pos.positionMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = NanzMusifyTextSecondary,
                )
                Text(formatMs(pos.durationMs), style = MaterialTheme.typography.labelSmall, color = NanzMusifyTextSecondary)
            }

            Spacer(Modifier.height(16.dp))

            // Kontrol utama
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                IconButton(onClick = { onShuffle(!playerState.shuffleEnabled) }) {
                    Icon(
                        Icons.Filled.Shuffle,
                        contentDescription = stringResource(R.string.common_shuffle),
                        tint = if (playerState.shuffleEnabled) dynamicPalette.accent else NanzMusifyTextSecondary,
                    )
                }
                IconButton(onClick = onPrev, modifier = Modifier.size(52.dp)) {
                    Icon(
                        Icons.Filled.SkipPrevious,
                        contentDescription = stringResource(R.string.np_previous),
                        tint = NanzMusifyTextPrimary,
                        modifier = Modifier.size(38.dp),
                    )
                }
                NanzMusifyPlayButton(
                    isPlaying = playerState.isPlaying,
                    onClick = onToggle,
                    size = 84.dp,
                )
                IconButton(onClick = onNext, modifier = Modifier.size(52.dp)) {
                    Icon(
                        Icons.Filled.SkipNext,
                        contentDescription = stringResource(R.string.np_next),
                        tint = NanzMusifyTextPrimary,
                        modifier = Modifier.size(38.dp),
                    )
                }
                IconButton(onClick = onCycleRepeat) {
                    Icon(
                        when (playerState.repeatMode) {
                            Player.REPEAT_MODE_ONE -> Icons.Filled.RepeatOne
                            else -> Icons.Filled.Repeat
                        },
                        contentDescription = stringResource(R.string.np_repeat),
                        tint = if (playerState.repeatMode != Player.REPEAT_MODE_OFF) dynamicPalette.accent else NanzMusifyTextSecondary,
                    )
                }
            }

            Spacer(Modifier.height(22.dp))

            // Aksi sekunder: lirik · unduh · playlist · timer · antrean
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SecondaryAction(
                    icon = Icons.Filled.Lyrics,
                    label = stringResource(R.string.np_lyrics),
                    active = showLyrics,
                    accentColor = dynamicPalette.accent,
                    onClick = { showLyrics = !showLyrics },
                )
                if (!track.isLocal) {
                    SecondaryAction(
                        icon = Icons.Filled.Download,
                        label = stringResource(R.string.np_download),
                        active = isDownloaded,
                        accentColor = dynamicPalette.accent,
                        onClick = onDownload,
                    )
                }
                SecondaryAction(
                    icon = Icons.Filled.PlaylistAdd,
                    label = stringResource(R.string.common_playlist_caps),
                    active = false,
                    accentColor = dynamicPalette.accent,
                    onClick = onAddToPlaylist,
                )
                SecondaryAction(
                    icon = Icons.Filled.Bedtime,
                    label = if (playerState.sleepDeadlineMs != null) {
                        val mins = ((playerState.sleepDeadlineMs - System.currentTimeMillis()) / 60000L).coerceAtLeast(0)
                        stringResource(R.string.np_timer_left, mins)
                    } else stringResource(R.string.np_timer),
                    active = playerState.sleepDeadlineMs != null,
                    accentColor = dynamicPalette.accent,
                    onClick = onSleepTimer,
                )
                SecondaryAction(
                    icon = Icons.Filled.QueueMusic,
                    label = stringResource(R.string.np_queue),
                    active = false,
                    accentColor = dynamicPalette.accent,
                    onClick = { showQueue = true },
                )
                val audioPrefs = rememberPlaybackPrefs()
                val audioTweaked = audioPrefs.playbackSpeed != 1f || audioPrefs.pitchSemitones != 0
                SecondaryAction(
                    icon = Icons.Filled.Speed,
                    label = if (audioTweaked) {
                        stringResource(R.string.np_speed_active_fmt, audioPrefs.playbackSpeed)
                    } else {
                        stringResource(R.string.np_speed)
                    },
                    active = audioTweaked,
                    accentColor = dynamicPalette.accent,
                    onClick = { showPlaybackParams = true },
                )
            }

            if (showPlaybackParams) {
                PlaybackParamsDialog(onDismiss = { showPlaybackParams = false })
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    // ---- Lembar Antrean & Riwayat Putar (Metrolist Style) ----
    if (showQueue) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val historyTracks by locator.library.history.collectAsStateWithLifecycle(initialValue = emptyList())
        var queueTab by remember { mutableStateOf(0) } // 0 = Antrean, 1 = Riwayat

        ModalBottomSheet(
            onDismissRequest = { showQueue = false },
            sheetState = sheetState,
            containerColor = NanzMusifyElevated,
            scrimColor = NanzMusifyScrimSheet,
            contentColor = NanzMusifyTextPrimary,
            shape = NanzMusifyRadius.top(),
            dragHandle = {
                Spacer(
                    Modifier
                        .padding(top = 10.dp)
                        .width(44.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(50))
                        .background(NanzMusifySurface),
                )
            },
        ) {
            Column(Modifier.padding(bottom = 24.dp)) {
                // Header Segmented Tab: Antrean vs Riwayat
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(NanzMusifySurface.copy(alpha = 0.5f))
                            .padding(3.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (queueTab == 0) dynamicPalette.accent else Color.Transparent)
                                .clickable { queueTab = 0 }
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                        ) {
                            Text(
                                stringResource(R.string.queue_title, playerState.queue.size),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (queueTab == 0) Color.White else NanzMusifyTextSecondary,
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (queueTab == 1) dynamicPalette.accent else Color.Transparent)
                                .clickable { queueTab = 1 }
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                        ) {
                            Text(
                                stringResource(R.string.history_title),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (queueTab == 1) Color.White else NanzMusifyTextSecondary,
                            )
                        }
                    }

                    if (queueTab == 0 && playerState.queue.size > 1) {
                        Text(
                            stringResource(R.string.history_clear),
                            style = MaterialTheme.typography.labelSmall,
                            color = dynamicPalette.accent,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable {
                                    // Bersihkan antrean selain lagu yang sedang diputar
                                    val currentIdx = playerState.currentIndex
                                    for (i in playerState.queue.indices.reversed()) {
                                        if (i != currentIdx) onRemoveQueueItem(i)
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                        )
                    }
                }

                if (queueTab == 0) {
                    // Radio: bukan lagi sekadar badge — tombol nyata yang memulai
                    // radio dari lagu berjalan (pola Metrolist: antrean langsung
                    // terisi lagu terkait, bukan menunggu lagu habis).
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Filled.Radio,
                            contentDescription = null,
                            tint = dynamicPalette.accent,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            stringResource(R.string.radio_hint),
                            style = MaterialTheme.typography.labelSmall,
                            color = NanzMusifyTextMuted,
                            modifier = Modifier.weight(1f),
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .border(1.dp, dynamicPalette.accent, RoundedCornerShape(50))
                                .background(dynamicPalette.accent.copy(alpha = 0.16f), RoundedCornerShape(50))
                                .clickable { track?.let(player::startRadio) }
                                .padding(horizontal = 14.dp, vertical = 7.dp),
                        ) {
                            Text(
                                stringResource(R.string.radio_start),
                                style = MaterialTheme.typography.labelMedium,
                                color = dynamicPalette.accent,
                            )
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(420.dp),
                        contentPadding = PaddingValues(start = 10.dp, end = 10.dp, bottom = 16.dp),
                    ) {
                        itemsIndexed(playerState.queue, key = { i, t -> "${t.videoId}_$i" }) { index, item ->
                            val active = index == playerState.currentIndex
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                                    .clip(RoundedCornerShape(NanzMusifyRadius.md))
                                    .background(
                                        if (active) dynamicPalette.accent.copy(alpha = 0.14f)
                                        else NanzMusifySurface.copy(alpha = 0.45f),
                                    )
                                    .clickable { onPlayAt(index) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    "%02d".format(index + 1),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (active) dynamicPalette.accent else NanzMusifyTextMuted,
                                    modifier = Modifier.width(28.dp),
                                )
                                Artwork(url = item.thumbnailUrl, title = item.title, size = 40.dp)
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        item.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = NanzMusifyTextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        item.artist.ifBlank { stringResource(R.string.common_youtube) },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NanzMusifyTextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                IconButton(
                                    onClick = { if (index > 0) onMoveQueueItem(index, index - 1) },
                                    enabled = index > 0,
                                    modifier = Modifier.size(32.dp),
                                ) {
                                    Icon(
                                        Icons.Filled.KeyboardArrowUp,
                                        contentDescription = stringResource(R.string.move_up),
                                        tint = if (index > 0) NanzMusifyTextSecondary else NanzMusifySurface,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        if (index < playerState.queue.lastIndex) onMoveQueueItem(index, index + 1)
                                    },
                                    enabled = index < playerState.queue.lastIndex,
                                    modifier = Modifier.size(32.dp),
                                ) {
                                    Icon(
                                        Icons.Filled.KeyboardArrowDown,
                                        contentDescription = stringResource(R.string.move_down),
                                        tint = if (index < playerState.queue.lastIndex) NanzMusifyTextSecondary else NanzMusifySurface,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                                if (active) {
                                    Icon(
                                        Icons.Filled.GraphicEq,
                                        contentDescription = stringResource(R.string.common_playing),
                                        tint = dynamicPalette.accent,
                                        modifier = Modifier.size(18.dp),
                                    )
                                } else {
                                    IconButton(onClick = { onRemoveQueueItem(index) }, modifier = Modifier.size(32.dp)) {
                                        Icon(
                                            Icons.Filled.Close,
                                            contentDescription = stringResource(R.string.queue_remove),
                                            tint = NanzMusifyTextMuted,
                                            modifier = Modifier.size(16.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Tab Riwayat Putar
                    if (historyTracks.isNotEmpty()) {
                        // Riwayat → antrean: satu ketukan mengisi antrean dengan
                        // lagu yang baru diputar (jalur otomatisnya di
                        // PlayerManager.maybeExtendQueue saat radio kehabisan lagu).
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                stringResource(R.string.history_queue_hint),
                                style = MaterialTheme.typography.labelSmall,
                                color = NanzMusifyTextMuted,
                                modifier = Modifier.weight(1f),
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .border(1.dp, dynamicPalette.accent, RoundedCornerShape(50))
                                    .background(dynamicPalette.accent.copy(alpha = 0.16f), RoundedCornerShape(50))
                                    .clickable { player.enqueueHistory() }
                                    .padding(horizontal = 14.dp, vertical = 7.dp),
                            ) {
                                Text(
                                    stringResource(R.string.history_queue_action),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = dynamicPalette.accent,
                                )
                            }
                        }
                    }
                    if (historyTracks.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                stringResource(R.string.history_empty_body),
                                style = MaterialTheme.typography.bodySmall,
                                color = NanzMusifyTextMuted,
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(420.dp),
                            contentPadding = PaddingValues(start = 10.dp, end = 10.dp, bottom = 16.dp),
                        ) {
                            itemsIndexed(historyTracks, key = { i, hTrack -> "${hTrack.videoId}_$i" }) { index, hTrack ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 3.dp)
                                        .clip(RoundedCornerShape(NanzMusifyRadius.md))
                                        .background(NanzMusifySurface.copy(alpha = 0.45f))
                                        .clickable {
                                            player.playQueue(historyTracks, index)
                                        }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Artwork(url = hTrack.thumbnailUrl, title = hTrack.title, size = 42.dp)
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            hTrack.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = NanzMusifyTextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Text(
                                            hTrack.artist.ifBlank { stringResource(R.string.common_youtube) },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = NanzMusifyTextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                    IconButton(
                                        onClick = { player.addToQueue(hTrack) },
                                        modifier = Modifier.size(36.dp),
                                    ) {
                                        Icon(
                                            Icons.Filled.PlaylistAdd,
                                            contentDescription = stringResource(R.string.action_add_to_queue),
                                            tint = dynamicPalette.accent,
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Pill ala segmented control iOS. */
@Composable
private fun ModePill(
    label: String,
    selected: Boolean,
    accentColor: Color = NanzMusifyCrimson,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) accentColor else NanzMusifySurface.copy(alpha = 0f))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) NanzMusifyTextPrimary else NanzMusifyTextSecondary,
        )
    }
}

@Composable
private fun SecondaryAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    active: Boolean,
    accentColor: Color = NanzMusifyCrimson,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (active) accentColor else NanzMusifyTextPrimary,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = if (active) accentColor else NanzMusifyTextSecondary,
        )
    }
}

/**
 * Pratinjau "Berikutnya": maksimal dua lagu setelah posisi berjalan, dalam satu
 * kartu inset membulat (pola Now Playing Metrolist/Meld). Ketuk baris = lompat
 * ke lagu itu di antrean. Tidak digambar sama sekali saat tidak ada lagu
 * tersisa — nol biaya, nol kekacauan visual.
 */
@Composable
private fun UpNextBlock(
    queue: List<NanzMusifyTrack>,
    currentIndex: Int,
    accentColor: Color,
    onPlayAt: (Int) -> Unit,
) {
    val upcoming = if (currentIndex in queue.indices) {
        queue.drop(currentIndex + 1).take(2)
    } else {
        emptyList()
    }
    if (upcoming.isEmpty()) return
    val baseIndex = currentIndex + 1

    Spacer(Modifier.height(16.dp))
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(NanzMusifySurface.copy(alpha = 0.35f))
            .border(1.dp, NanzMusifyHairline, shape)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(
            stringResource(R.string.np_up_next).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = NanzMusifyTextMuted,
        )
        Spacer(Modifier.height(2.dp))
        upcoming.forEachIndexed { offset, item ->
            val index = baseIndex + offset
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onPlayAt(index) }
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Artwork(url = item.thumbnailUrl, title = item.title, size = 38.dp, cornerRadius = 8.dp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = NanzMusifyTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = item.artist.ifBlank { stringResource(R.string.common_youtube_music) },
                        style = MaterialTheme.typography.labelSmall,
                        color = NanzMusifyTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (item.durationSec > 0) {
                    Text(
                        text = formatMs(item.durationSec * 1000L),
                        style = MaterialTheme.typography.labelSmall,
                        color = NanzMusifyTextMuted,
                    )
                }
                Spacer(Modifier.width(4.dp))
                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp),
                )
            }
            if (offset < upcoming.lastIndex) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 50.dp)
                        .height(1.dp)
                        .background(NanzMusifyTextMuted.copy(alpha = 0.2f)),
                )
            }
        }
    }
}

/**
 * Kanvas sampul ⇄ lirik (ketuk untuk berganti, fade 450ms):
 *  - Sampul: bingkai persegi sedikit memanjang ke bawah (~1:1,06), gambar
 *    MENGISI penuh areanya (crop tengah), kualitas terjaga lewat rantai
 *    varian ytimg: maxres → sd → hq.
 *  - Lirik: artwork yang sama menjadi latar, diblur (API 31+) + digelapkan
 *    bergradasi supaya teks lirik paling menonjol.
 */
@Composable
private fun ArtworkOrLyrics(
    track: NanzMusifyTrack,
    isDownloaded: Boolean,
    showLyrics: Boolean,
    positionMs: Long,
    accentColor: Color,
    onToggleLyrics: () -> Unit,
    onSeekMs: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            // Persegi nyaris 1:1, sedikit lebih tinggi (lebih elegan dari 16:9)
            .aspectRatio(0.94f)
            .clip(RoundedCornerShape(20.dp))
            .background(NanzMusifySurface)
            .clickable(onClick = onToggleLyrics),
    ) {
        Crossfade(targetState = showLyrics, animationSpec = nanzmusifyTween(NanzMusifyMotion.deliberate), label = "art_lyrics") { lyrics ->
            if (!lyrics) {
                Box(Modifier.fillMaxSize()) {
                    if (track.thumbnailUrl.isNotBlank()) {
                        RichArtwork(
                            url = track.thumbnailUrl,
                            contentDescription = track.title,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        Box(Modifier.fillMaxSize().background(NanzMusifySurface))
                    }
                    if (isDownloaded) {
                        Box(
                            Modifier
                                .align(Alignment.TopStart)
                                .padding(10.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(NanzMusifyRose)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Text(stringResource(R.string.common_offline), style = MaterialTheme.typography.labelSmall, color = NanzMusifyBackground)
                        }
                    }
                }
            } else {
                Box(Modifier.fillMaxSize()) {
                    // Latar lirik = artwork yang sama, diblur
                    if (track.thumbnailUrl.isNotBlank()) {
                        RichArtwork(
                            url = track.thumbnailUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .then(
                                    if (android.os.Build.VERSION.SDK_INT >= 31) {
                                        Modifier.blur(32.dp)
                                    } else {
                                        // Pra-API 31: tanpa RenderEffect — redupkan saja
                                        Modifier.alpha(0.22f)
                                    },
                                ),
                        )
                    }
                    // Skrim gradasi → teks lirik jauh lebih menonjol dari thumbnail
                    Box(
                        Modifier.fillMaxSize().background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.55f),
                                    Color.Black.copy(alpha = 0.72f),
                                ),
                            ),
                        ),
                    )
                    LyricsContent(
                        track = track,
                        positionMs = positionMs,
                        onSeekMs = onSeekMs,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            // Tombol lirik terapung — penanda visual bahwa kanvas bisa berpindah
            // sampul ⇄ lirik (aksesibilitas: aksi tidak hanya lewat ketukan pada
            // gambar). Di pojok atas: saat mode lirik, area itu adalah ruang
            // kosong auto-scroll, jadi tidak pernah menutupi baris teks.
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .size(40.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.38f))
                    .border(
                        1.dp,
                        if (showLyrics) accentColor else NanzMusifyHairline.copy(alpha = 0.6f),
                        RoundedCornerShape(50),
                    )
                    .clickable(onClick = onToggleLyrics),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.Lyrics,
                    contentDescription = stringResource(R.string.np_lyrics),
                    tint = if (showLyrics) accentColor else NanzMusifyTextPrimary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

/**
 * Gambar artwork dengan rantai kualitas ytimg (maxres 1280×720 → sd 640×480 →
 * hq 480×360) — maxres tidak tersedia di semua video, jadi turun otomatis saat
 * 404. Non-ytimg (file lokal, dsb) ditampilkan apa adanya. Selalu ContentScale.Crop.
 */
@Composable
private fun RichArtwork(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    val candidates = remember(url) { ytimgCandidates(url) }
    var index by remember(url) { androidx.compose.runtime.mutableIntStateOf(0) }
    AsyncImage(
        model = candidates[index.coerceAtMost(candidates.lastIndex)],
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        onError = {
            // Varian tak tersedia (404) → turun ke kualitas berikutnya
            if (index < candidates.lastIndex) index++
        },
        modifier = modifier,
    )
}

/** Rantai URL varian ytimg terbesar → terkecil untuk videoId dari URL gambar. */
private fun ytimgCandidates(url: String): List<String> =
    if (url.contains("i.ytimg.com") && url.contains("/vi/")) {
        val vid = url.substringAfter("/vi/").substringBefore("/")
        listOf(
            "https://i.ytimg.com/vi/$vid/maxresdefault.jpg",
            "https://i.ytimg.com/vi/$vid/sddefault.jpg",
            "https://i.ytimg.com/vi/$vid/hqdefault.jpg",
            url, // jaga-jaga: sumber asli (mungkin varian lain)
        )
    } else {
        listOf(url)
    }

/** Foto kanal artis (bulat) — fallback monogram huruf pertama. */
@Composable
private fun ArtistAvatar(
    name: String,
    url: String?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(24.dp)
            .clip(androidx.compose.foundation.shape.CircleShape)
            .background(NanzMusifySurface),
        contentAlignment = Alignment.Center,
    ) {
        if (!url.isNullOrBlank()) {
            AsyncImage(
                model = url,
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Text(
                text = name.take(1).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = NanzMusifyCrimson,
            )
        }
    }
}

/**
 * Preferensi audio (kecepatan & nada) dibaca langsung dari Pengaturan — sumber
 * kebenaran tunggalnya di sana; PlaybackService yang menerapkannya ke ExoPlayer.
 */
@Composable
private fun rememberPlaybackPrefs(): NanzMusifySettings {
    val locator = LocalNanzMusify.current
    val settings by locator.settings.settings
        .collectAsStateWithLifecycle(initialValue = NanzMusifySettings())
    return settings
}

/**
 * Dialog kecepatan putar & geser nada. Nilai disimpan saat gestur selesai
 * (bukan tiap langkah geser) supaya DataStore tidak dibanjiri tulisan.
 */
@Composable
private fun PlaybackParamsDialog(onDismiss: () -> Unit) {
    val locator = LocalNanzMusify.current
    val scope = rememberCoroutineScope()
    val settings = rememberPlaybackPrefs()

    var speedDrag by remember { mutableStateOf<Float?>(null) }
    var pitchDrag by remember { mutableStateOf<Float?>(null) }
    val speed = speedDrag ?: settings.playbackSpeed
    val pitch = pitchDrag ?: settings.pitchSemitones.toFloat()
    val tweaked = settings.playbackSpeed != 1f || settings.pitchSemitones != 0

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NanzMusifyElevated,
        title = {
            Text(
                stringResource(R.string.speed_dialog_title),
                style = MaterialTheme.typography.titleMedium,
                color = NanzMusifyTextPrimary,
            )
        },
        text = {
            Column {
                Text(
                    stringResource(R.string.speed_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = NanzMusifyTextSecondary,
                )
                Text(
                    stringResource(R.string.speed_value_fmt, speed),
                    style = MaterialTheme.typography.titleMedium,
                    color = NanzMusifyCrimson,
                )
                Slider(
                    value = speed,
                    onValueChange = { speedDrag = it },
                    onValueChangeFinished = {
                        speedDrag?.let { v -> scope.launch { locator.settings.setPlaybackSpeed(v) } }
                        speedDrag = null
                    },
                    valueRange = 0.5f..2f,
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    stringResource(R.string.pitch_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = NanzMusifyTextSecondary,
                )
                Text(
                    stringResource(R.string.pitch_value_fmt, pitch.roundToInt()),
                    style = MaterialTheme.typography.titleMedium,
                    color = NanzMusifyCrimson,
                )
                Slider(
                    value = pitch,
                    onValueChange = { pitchDrag = it },
                    onValueChangeFinished = {
                        pitchDrag?.let { v ->
                            scope.launch { locator.settings.setPitchSemitones(v.roundToInt()) }
                        }
                        pitchDrag = null
                    },
                    valueRange = -12f..12f,
                    steps = 23,
                )
                if (tweaked) {
                    TextButton(
                        onClick = {
                            scope.launch {
                                locator.settings.setPlaybackSpeed(1f)
                                locator.settings.setPitchSemitones(0)
                            }
                        },
                    ) {
                        Text(
                            stringResource(R.string.speed_reset),
                            style = MaterialTheme.typography.labelLarge,
                            color = NanzMusifyCrimson,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    stringResource(R.string.common_close),
                    style = MaterialTheme.typography.labelLarge,
                    color = NanzMusifyTextSecondary,
                )
            }
        },
    )
}
