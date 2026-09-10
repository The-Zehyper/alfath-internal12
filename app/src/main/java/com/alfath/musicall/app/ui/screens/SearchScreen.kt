/*
 * Copyright (C) 2026 Nanas
 *
 * SPDX-License-Identifier: GPL-3.0-only
 */
package com.alfath.musicall.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alfath.musicall.app.R
import com.alfath.musicall.app.data.model.NanzMusifyTrack
import com.alfath.musicall.app.data.model.SearchFilter
import com.alfath.musicall.app.data.model.YtPlaylist
import com.alfath.musicall.app.player.PlayerUiState
import com.alfath.musicall.app.ui.components.Artwork
import com.alfath.musicall.app.ui.components.SectionRule
import com.alfath.musicall.app.ui.components.TrackRow
import com.alfath.musicall.app.ui.theme.NanzMusifyTextSecondary
import com.alfath.musicall.app.ui.theme.NanzMusifyTextPrimary
import com.alfath.musicall.app.ui.theme.NanzMusifyLine
import com.alfath.musicall.app.ui.theme.NanzMusifyRadius
import com.alfath.musicall.app.ui.theme.NanzMusifySurface
import com.alfath.musicall.app.ui.theme.NanzMusifyBackground
import com.alfath.musicall.app.ui.theme.NanzMusifyCrimson
import com.alfath.musicall.app.ui.theme.NanzMusifyTextMuted
import com.alfath.musicall.app.ui.vm.SearchViewModel

// Key LazyColumn untuk rak playlist selalu diawali penanda ini supaya tidak
// bertabrakan dengan key item lain yang kebetulan sama (playlist YouTube Music
// tidak punya URL — url-nya dibangun dari playlistId).
private const val PLAYLIST_ROW_KEY_PREFIX = "pl:"

@Composable
fun SearchScreen(
    vm: SearchViewModel,
    playerState: PlayerUiState,
    onPlayQueue: (List<NanzMusifyTrack>, Int) -> Unit,
    onTrackMore: (NanzMusifyTrack) -> Unit,
    onLike: (NanzMusifyTrack) -> Unit,
    onOpenYtPlaylist: (YtPlaylist) -> Unit,
    likedIds: Set<String>,
    downloadedIds: Set<String>,
    onOpenBrowse: ((String, String) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val keyboard = LocalSoftwareKeyboardController.current

    // Kueri dari layar lain (chip vibe di Home) — jalankan pencariannya di sini
    val locator = com.alfath.musicall.app.ui.vm.LocalNanzMusify.current
    LaunchedEffect(Unit) {
        locator.pendingSearchQuery.collect { q ->
            if (!q.isNullOrBlank()) {
                locator.pendingSearchQuery.value = null
                vm.search(q)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NanzMusifyBackground),
    ) {
        // ---- Header ----
        Column(Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
            Text(text = stringResource(R.string.search_kicker), style = MaterialTheme.typography.labelMedium, color = NanzMusifyTextSecondary)
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.search_title),
                style = MaterialTheme.typography.displaySmall,
                color = NanzMusifyTextPrimary,
            )
            Spacer(Modifier.height(8.dp))
            Box(Modifier.width(40.dp).height(2.dp).background(NanzMusifyCrimson))
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = state.query,
                onValueChange = vm::onQueryChange,
                singleLine = true,
                shape = RoundedCornerShape(50),
                placeholder = {
                    Text(
                        stringResource(R.string.search_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = NanzMusifyTextMuted,
                    )
                },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = NanzMusifyTextSecondary)
                },
                trailingIcon = {
                    if (state.query.isNotEmpty()) {
                        IconButton(onClick = { vm.onQueryChange("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = stringResource(R.string.history_clear), tint = NanzMusifyTextSecondary)
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    vm.search()
                    keyboard?.hide()
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NanzMusifyCrimson.copy(alpha = 0.5f),
                    unfocusedBorderColor = NanzMusifyLine,
                    cursorColor = NanzMusifyTextPrimary,
                    focusedTextColor = NanzMusifyTextPrimary,
                    unfocusedTextColor = NanzMusifyTextPrimary,
                    focusedContainerColor = NanzMusifySurface.copy(alpha = 0.5f),
                    unfocusedContainerColor = NanzMusifySurface.copy(alpha = 0.5f),
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(12.dp))

            // ---- Filter chips ----
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(SearchFilter.entries.toList(), key = { it.name }) { filter ->
                    val selected = state.filter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .border(
                                1.dp,
                                if (selected) NanzMusifyCrimson.copy(alpha = 0.6f) else NanzMusifyLine,
                                RoundedCornerShape(50),
                            )
                            .background(
                                if (selected) NanzMusifyCrimson.copy(alpha = 0.22f)
                                else NanzMusifySurface.copy(alpha = 0.35f),
                            )
                            .clickable { vm.setFilter(filter) }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        Text(
                            text = stringResource(when (filter) {
                                SearchFilter.ALL -> R.string.filter_all
                                SearchFilter.SONGS -> R.string.filter_songs
                                SearchFilter.VIDEOS -> R.string.filter_videos
                                SearchFilter.ALBUMS -> R.string.filter_albums
                                SearchFilter.ARTISTS -> R.string.filter_artists
                                SearchFilter.PLAYLISTS -> R.string.filter_playlists
                                SearchFilter.PODCASTS -> R.string.filter_podcasts
                            }),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected) NanzMusifyTextPrimary else NanzMusifyTextSecondary,
                        )
                    }
                }
            }
        }

        // ---- Konten ----
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp),
        ) {
            // Saran pencarian
            if (!state.searched && state.suggestions.isNotEmpty()) {
                item {
                    SectionRule(label = stringResource(R.string.search_suggest))
                    Column(Modifier.padding(horizontal = 20.dp)) {
                        state.suggestions.forEach { suggestion ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        vm.search(suggestion)
                                        keyboard?.hide()
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    Icons.Filled.Search,
                                    contentDescription = null,
                                    tint = NanzMusifyTextMuted,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    suggestion,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = NanzMusifyTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }

            if (state.searching) {
                item {
                    Box(
                        Modifier.fillMaxWidth().padding(40.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = NanzMusifyCrimson, modifier = Modifier.size(32.dp))
                            Spacer(Modifier.height(10.dp))
                            Text(stringResource(R.string.search_loading), style = MaterialTheme.typography.labelSmall, color = NanzMusifyTextMuted)
                        }
                    }
                }
            }

            state.error?.let { err ->
                item {
                    Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                        Text(stringResource(R.string.downloads_failed), style = MaterialTheme.typography.labelMedium, color = NanzMusifyCrimson)
                        Spacer(Modifier.height(6.dp))
                        Text(err, style = MaterialTheme.typography.bodySmall, color = NanzMusifyTextSecondary)
                    }
                }
            }

            // Playlist hasil pencarian (termasuk hasil PODCAST — podcast = playlist episode)
            // musicPlaylists = rak playlist dari YouTube Music (jalur WEB_REMIX);
            // playlists = playlist dari extractor (hanya saat filter PLAYLISTS,
            // supaya tab SEMUA tidak menampilkan dua rak playlist).
            val shownPlaylists = if (state.filter == SearchFilter.PLAYLISTS || state.filter == SearchFilter.PODCASTS) {
                state.playlists
            } else {
                state.musicPlaylists.map { pl ->
                    // Rak playlist YouTube Music tidak membawa url — bangun dari
                    // playlistId; layar tujuan butuh list id saja.
                    val playlistUrl = "https://music.youtube.com/playlist?list=${pl.playlistId}"
                    YtPlaylist(playlistUrl, pl.title, pl.author, pl.thumbUrl, 0)
                }
            }
            if (shownPlaylists.isNotEmpty()) {
                item {
                    SectionRule(
                        label = stringResource(
                            if (state.filter == SearchFilter.PODCASTS) R.string.filter_podcasts
                            else R.string.filter_playlists,
                        ),
                    )
                }
                items(shownPlaylists, key = { PLAYLIST_ROW_KEY_PREFIX + it.url }) { pl ->
                    YtPlaylistRow(
                        playlist = pl,
                        isPodcast = state.filter == SearchFilter.PODCASTS,
                        onClick = { onOpenYtPlaylist(pl) },
                    )
                }
            }

            // ---- Artis hasil YouTube Music (halaman artis khusus) ----
            // Sumbernya `state.artists` (browseId kanal UC...), BUKAN nama artis
            // dari metadata lagu: nama tidak bisa dipakai sebagai browseId, dan
            // halaman artis yang dibuka dari sana selalu gagal.
            // Selalu tampil saat hasilnya ada (tab SEMUA ikut menampilkan semua
            // rak ala Meld/Metrolist — bukan hanya saat filter ARTIS).
            if (state.artists.isNotEmpty() && onOpenBrowse != null && state.filter != SearchFilter.ALBUMS) {
                item {
                    SectionRule(label = stringResource(R.string.filter_artists))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        items(state.artists, key = { it.browseId }) { artist ->
                            ArtistResultCard(
                                artist = artist,
                                onOpen = { onOpenBrowse(artist.browseId, artist.name) },
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            // ---- Album hasil YouTube Music ----
            // Selalu tampil saat hasilnya ada (pola ringkasan pencarian Meld:
            // SEMUA = semua rak berurutan; tab ALBUM/ARTIS menyaring).
            if (state.albums.isNotEmpty() && onOpenBrowse != null && state.filter != SearchFilter.ARTISTS) {
                item {
                    SectionRule(label = stringResource(R.string.filter_albums))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(state.albums, key = { it.browseId }) { album ->
                            AlbumResultCard(
                                album = album,
                                onOpen = { onOpenBrowse(album.browseId, album.title) },
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            // Track hasil pencarian (hanya saat tidak menyaring artis/album —
            // tab ARTIS & ALBUM menampilkan rak jenis itu saja ala Meld).
            if (state.tracks.isNotEmpty() && state.filter !in setOf(SearchFilter.ARTISTS, SearchFilter.ALBUMS)) {
                item { SectionRule(label = stringResource(R.string.search_results, state.tracks.size)) }
                itemsIndexed(state.tracks, key = { _, t -> t.videoId }) { index, track ->
                    TrackRow(
                        track = track,
                        isActive = playerState.currentTrack?.videoId == track.videoId,
                        isPlaying = playerState.isPlaying,
                        isLiked = likedIds.contains(track.videoId),
                        isDownloaded = downloadedIds.contains(track.videoId),
                        index = index,
                        onPlay = { onPlayQueue(state.tracks, index) },
                        onLike = { onLike(track) },
                        onMore = { onTrackMore(track) },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                    )
                }

                if (state.hasNext) {
                    item {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, NanzMusifyLine, RoundedCornerShape(16.dp))
                                .background(NanzMusifySurface.copy(alpha = 0.3f))
                                .clickable(enabled = !state.loadingMore) { vm.loadMore() }
                                .padding(14.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (state.loadingMore) {
                                CircularProgressIndicator(color = NanzMusifyCrimson, modifier = Modifier.size(20.dp))
                            } else {
                                Text(
                                    stringResource(R.string.search_load_more),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = NanzMusifyTextPrimary,
                                )
                            }
                        }
                    }
                }
            }

            if (state.searched && !state.searching && state.tracks.isEmpty() && state.playlists.isEmpty() &&
                state.musicPlaylists.isEmpty() && state.artists.isEmpty() && state.albums.isEmpty() && state.error == null
            ) {
                item {
                    Column(
                        Modifier.fillMaxWidth().padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            stringResource(R.string.search_no_results),
                            style = MaterialTheme.typography.labelMedium,
                            color = NanzMusifyTextSecondary,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.search_no_results_body),
                            style = MaterialTheme.typography.bodySmall,
                            color = NanzMusifyTextMuted,
                        )
                    }
                }
            }

            // ---- Kondisi awal (belum ada pencarian): cuma Riwayat ----
            if (!state.searched && state.suggestions.isEmpty()) {
                if (state.history.isEmpty()) {
                    item {
                        Column(
                            Modifier.fillMaxWidth().padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                stringResource(R.string.search_intro),
                                style = MaterialTheme.typography.bodySmall,
                                color = NanzMusifyTextMuted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            )
                        }
                    }
                } else {
                    item {
                        Spacer(Modifier.height(8.dp))
                        SectionRule(label = stringResource(R.string.search_history))
                    }
                    itemsIndexed(state.history, key = { _, t -> "hist_${t.videoId}" }) { index, track ->
                        TrackRow(
                            track = track,
                            isActive = playerState.currentTrack?.videoId == track.videoId,
                            isPlaying = playerState.isPlaying,
                            isLiked = likedIds.contains(track.videoId),
                            isDownloaded = downloadedIds.contains(track.videoId),
                            index = index,
                            onPlay = { onPlayQueue(state.history, index) },
                            onLike = { onLike(track) },
                            onMore = { onTrackMore(track) },
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun YtPlaylistRow(playlist: YtPlaylist, isPodcast: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, NanzMusifyLine, RoundedCornerShape(14.dp))
            .background(NanzMusifySurface.copy(alpha = 0.3f))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Artwork(url = playlist.thumbnailUrl, title = playlist.name, size = 52.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                playlist.name,
                style = MaterialTheme.typography.titleSmall,
                color = NanzMusifyTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                "${playlist.uploader.ifBlank { stringResource(R.string.common_youtube) }}  ·  ${stringResource(R.string.playlist_track_count, playlist.streamCount.toInt())}",
                style = MaterialTheme.typography.labelSmall,
                color = NanzMusifyTextSecondary,
            )
        }
        Text(
            stringResource(if (isPodcast) R.string.filter_podcasts else R.string.common_playlist_caps),
            style = MaterialTheme.typography.labelSmall,
            color = NanzMusifyCrimson,
        )
    }
}

/** Kartu artis hasil pencarian: avatar bulat, ketuk → halaman artis khusus. */
@Composable
private fun ArtistResultCard(
    artist: com.alfath.musicall.app.yt.YtArtist,
    onOpen: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(104.dp)
            .clip(RoundedCornerShape(NanzMusifyRadius.md))
            .background(NanzMusifySurface.copy(alpha = 0.35f))
            .clickable(onClick = onOpen)
            .padding(8.dp),
    ) {
        Artwork(
            url = artist.thumbUrl,
            title = artist.name,
            size = 76.dp,
            cornerRadius = 38.dp,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = artist.name,
            style = MaterialTheme.typography.labelMedium,
            color = NanzMusifyTextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Text(
            text = artist.subtitle.ifBlank { stringResource(R.string.filter_artists) },
            style = MaterialTheme.typography.labelSmall,
            color = if (artist.subtitle.isBlank()) NanzMusifyCrimson else NanzMusifyTextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

/** Kartu album hasil pencarian: ketuk → halaman album (browse InnerTube). */
@Composable
private fun AlbumResultCard(
    album: com.alfath.musicall.app.yt.YtAlbum,
    onOpen: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(132.dp)
            .clip(RoundedCornerShape(NanzMusifyRadius.md))
            .background(NanzMusifySurface.copy(alpha = 0.35f))
            .clickable(onClick = onOpen)
            .padding(8.dp),
    ) {
        Artwork(
            url = album.thumbUrl,
            title = album.title,
            size = 116.dp,
            cornerRadius = NanzMusifyRadius.sm,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = album.title,
            style = MaterialTheme.typography.labelMedium,
            color = NanzMusifyTextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (album.subtitle.isNotBlank()) {
            Text(
                text = album.subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = NanzMusifyTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
