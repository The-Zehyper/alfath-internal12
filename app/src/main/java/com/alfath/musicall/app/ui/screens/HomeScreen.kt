/*
 * Copyright (C) 2026 Nanas
 *
 * SPDX-License-Identifier: GPL-3.0-only
 */
package com.alfath.musicall.app.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.alfath.musicall.app.R
import com.alfath.musicall.app.data.model.EditorialSection
import com.alfath.musicall.app.data.model.NanzMusifyArchive
import com.alfath.musicall.app.data.model.NanzMusifyTrack
import com.alfath.musicall.app.player.PlayerUiState
import com.alfath.musicall.app.ui.components.Artwork
import com.alfath.musicall.app.ui.components.BrutalFrame
import com.alfath.musicall.app.ui.components.GenreReelSlider
import com.alfath.musicall.app.ui.components.NanzMusifyPlayButton
import com.alfath.musicall.app.ui.components.SectionRule
import com.alfath.musicall.app.ui.components.TrackRow
import com.alfath.musicall.app.ui.theme.NanzMusifyBackground
import com.alfath.musicall.app.ui.theme.NanzMusifyCrimson
import com.alfath.musicall.app.ui.theme.NanzMusifyElevated
import com.alfath.musicall.app.ui.theme.NanzMusifyLine
import com.alfath.musicall.app.ui.theme.NanzMusifyRadius
import com.alfath.musicall.app.ui.theme.NanzMusifySurface
import com.alfath.musicall.app.ui.theme.NanzMusifyTextMuted
import com.alfath.musicall.app.ui.theme.NanzMusifyTextPrimary
import com.alfath.musicall.app.ui.theme.NanzMusifyTextSecondary
import com.alfath.musicall.app.ui.vm.HomeViewModel
import java.util.Calendar

@Composable
fun HomeScreen(
    vm: HomeViewModel,
    playerState: PlayerUiState,
    onPlayQueue: (List<NanzMusifyTrack>, Int) -> Unit,
    onTogglePlay: () -> Unit,
    onOpenTrack: () -> Unit,
    onTrackMore: (NanzMusifyTrack) -> Unit,
    onLike: (NanzMusifyTrack) -> Unit,
    likedIds: Set<String>,
    downloadedIds: Set<String>,
    onOpenEditorial: (EditorialSection) -> Unit,
    onSearchClick: () -> Unit,
    onOpenBrowse: (String, String) -> Unit = { _, _ -> },
    onOpenProfile: () -> Unit = {},
    onOpenLibrary: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val home by vm.state.collectAsStateWithLifecycle()
    val locator = com.alfath.musicall.app.ui.vm.LocalNanzMusify.current
    val settingsVm: com.alfath.musicall.app.ui.vm.SettingsViewModel =
        com.alfath.musicall.app.ui.vm.nanzmusifyViewModel { com.alfath.musicall.app.ui.vm.SettingsViewModel(it) }
    val settings by settingsVm.settings.collectAsStateWithLifecycle()
    var showNameDialog by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf(false)
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(NanzMusifyBackground),
    ) {
        val wide = maxWidth >= 600.dp

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp),
        ) {
            item(key = "brand_header") {
                HomeBrandHeader(
                    displayName = settings.displayName.ifBlank { "Nanas" },
                    onEditName = { showNameDialog = true },
                    onSearchClick = onSearchClick,
                    onProfileClick = onOpenProfile,
                )
            }

            // Bar pencarian — langsung loncat ke tab Explore
            item(key = "home_search") {
                HomeSearchBar(onClick = onSearchClick)
            }

            // Grid "Quick Picks" — 2 kolom, akses cepat ke lagu yang lagi
            // dipersonalisasi / populer (sama datanya dengan sebelumnya,
            // tampilannya sekarang meniru referensi: kartu mini berdampingan).
            item(key = "quick_picks_rule") {
                Spacer(Modifier.height(22.dp))
                SectionRule(label = stringResource(R.string.home_quick_picks))
            }
            item(key = "quick_picks_grid") {
                if (home.loading && home.quickPicks.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(36.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = NanzMusifyCrimson, modifier = Modifier.size(32.dp))
                            Spacer(Modifier.height(12.dp))
                            Text(stringResource(R.string.home_preparing), style = MaterialTheme.typography.bodySmall, color = NanzMusifyTextMuted)
                        }
                    }
                } else {
                    Column(Modifier.padding(horizontal = 20.dp)) {
                        home.quickPicks.chunked(2).forEach { pair ->
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                pair.forEach { track ->
                                    QuickPickGridCard(
                                        track = track,
                                        active = playerState.currentTrack?.videoId == track.videoId,
                                        isPlaying = playerState.isPlaying,
                                        onClick = { onPlayQueue(home.quickPicks, home.quickPicks.indexOf(track)) },
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                                if (pair.size < 2) Spacer(Modifier.weight(1f))
                            }
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }
            }

            // "Popular Playlists" — kurasi editorial ditata sebagai kartu playlist
            // horizontal + tile "Buat Playlist" persis referensi.
            item(key = "popular_playlists_rule") {
                Spacer(Modifier.height(8.dp))
                SectionRule(label = stringResource(R.string.home_popular_playlists))
            }
            item(key = "popular_playlists_row") {
                PopularPlaylistsRow(
                    editorials = NanzMusifyArchive.editorials,
                    onOpen = onOpenEditorial,
                    onCreatePlaylist = onOpenLibrary,
                )
            }

            // ALGORITMA SELERA — "Untuk Kamu": lagu yang cocok dengan genre,
            // artis, gaya (speed up/reverb/Nightcore, dst) yang sering diputar,
            // plus chip vibe dari tagar kreator (#sadvibes, …)
            if (home.personaActive) {
                item(key = "persona_rule") {
                    Spacer(Modifier.height(20.dp))
                    SectionRule(label = stringResource(R.string.home_for_you))
                }
                item(key = "persona_row") {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        itemsIndexed(home.personaTracks, key = { i, t -> "p_${t.videoId}_$i" }) { _, track ->
                            QuickCard(
                                track = track,
                                active = playerState.currentTrack?.videoId == track.videoId,
                                onClick = { onPlayQueue(home.personaTracks, home.personaTracks.indexOf(track)) },
                            )
                        }
                    }
                }
                if (home.personaChips.isNotEmpty()) {
                    item(key = "persona_chips") {
                        PersonaChips(
                            chips = home.personaChips,
                            onChip = { chip ->
                                locator.pendingSearchQuery.value = chip.removePrefix("#")
                                onSearchClick()
                            },
                        )
                    }
                }
            }

            // ARTIS POPULER PER BENUA (dipindah dari Search agar tab Search ringan)
            item(key = "artist_regions") {
                LaunchedEffect(Unit) { vm.ensureArtistSection() }
                ArtistRegionsBlock(
                    region = home.artistRegion,
                    artists = home.artists,
                    loading = home.artistsLoading,
                    onRegion = vm::selectArtistRegion,
                    onArtist = { artist ->
                        // Halaman artis InnerTube bila kanal dikenal; kalau tidak,
                        // jatuh kembali ke pencarian seperti sebelumnya.
                        if (artist.browseId.isNotBlank()) {
                            onOpenBrowse(artist.browseId, artist.name)
                        } else {
                            locator.pendingSearchQuery.value = artist.name
                            onSearchClick()
                        }
                    },
                )
            }

            // ---- TREN PER NEGARA: genre/mood + lagu yang sedang naik ----
            item(key = "trending_block") {
                LaunchedEffect(Unit) { vm.ensureTrendingSection() }
                TrendingCountryBlock(
                    country = home.trendingCountry,
                    genres = home.trendingGenres,
                    tracks = home.trendingTracks,
                    loading = home.trendingLoading,
                    playerState = playerState,
                    likedIds = likedIds,
                    downloadedIds = downloadedIds,
                    onGenre = { item -> onOpenBrowse(item.browseId, item.title) },
                    onPlayTracks = { list, index -> onPlayQueue(list, index) },
                    onLike = onLike,
                    onTrackMore = onTrackMore,
                )
            }

            // Sedang diputar — kartu ringkas (tap → Now Playing)
            if (playerState.currentTrack != null) {
                item(key = "nowplaying") {
                    NowPlayingStrip(
                        track = playerState.currentTrack,
                        isPlaying = playerState.isPlaying,
                        isBuffering = playerState.isBuffering,
                        onToggle = onTogglePlay,
                        onOpen = onOpenTrack,
                    )
                }
            }

            // Baru diputar — riwayat pengguna
            if (home.history.isNotEmpty()) {
                item(key = "history_rule") {
                    Spacer(Modifier.height(20.dp))
                    SectionRule(label = stringResource(R.string.home_history))
                }
                item(key = "history_row") {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        itemsIndexed(home.history, key = { _, t -> t.videoId }) { _, track ->
                            QuickCard(
                                track = track,
                                active = playerState.currentTrack?.videoId == track.videoId,
                                onClick = { onPlayQueue(home.history, home.history.indexOf(track)) },
                            )
                        }
                    }
                }
            }

            // Jelajahi suasana — genre
            item(key = "genres_rule") {
                Spacer(Modifier.height(24.dp))
                SectionRule(label = stringResource(R.string.home_genres))
            }
            item(key = "genres") {
                GenreReelSlider(
                    genres = NanzMusifyArchive.genres,
                    selectedId = home.selectedGenreId,
                    onGenreClick = { g ->
                        vm.selectGenre(if (home.selectedGenreId == g.id) null else g)
                    },
                    onPlayGenre = { g ->
                        vm.playGenre(g) { tracks -> onPlayQueue(tracks, 0) }
                    },
                )
            }

            // Trek dari genre terpilih
            val selectedGenre = NanzMusifyArchive.genres.find { it.id == home.selectedGenreId }
            if (selectedGenre != null) {
                item(key = "genre_tracks_rule") {
                    Spacer(Modifier.height(20.dp))
                    SectionRule(label = stringResource(R.string.home_genre_for_you, selectedGenre.name))
                }
                if (home.movementLoading) {
                    item(key = "genre_loading") {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = NanzMusifyCrimson, modifier = Modifier.size(32.dp))
                        }
                    }
                } else {
                    itemsIndexed(home.movementTracks.take(8), key = { _, t -> t.videoId }) { index, track ->
                        TrackRow(
                            track = track,
                            isActive = playerState.currentTrack?.videoId == track.videoId,
                            isPlaying = playerState.isPlaying,
                            isLiked = likedIds.contains(track.videoId),
                            isDownloaded = downloadedIds.contains(track.videoId),
                            index = index,
                            onPlay = { onPlayQueue(home.movementTracks.take(8), index) },
                            onLike = { onLike(track) },
                            onMore = { onTrackMore(track) },
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                        )
                    }
                }
            }

            // Featured untukmu — kurasi editorial
            item(key = "featured_rule") {
                Spacer(Modifier.height(24.dp))
                SectionRule(label = stringResource(R.string.home_featured))
            }
            itemsIndexed(NanzMusifyArchive.editorials.take(3), key = { _, e -> e.id }) { _, section ->
                EditorialCard(
                    section = section,
                    wide = wide,
                    onOpen = { onOpenEditorial(section) },
                    onPlay = { vm.playEditorial(section) { tracks -> onPlayQueue(tracks, 0) } },
                )
            }

            item(key = "footer") {
                Spacer(Modifier.height(24.dp))
                FooterBlock()
            }
        }
    }

    if (showNameDialog) {
        NameEditDialog(
            current = settings.displayName.ifBlank { "Nanas" },
            onSave = {
                settingsVm.setDisplayName(it)
                showNameDialog = false
            },
            onDismiss = { showNameDialog = false },
        )
    }
}

/** Header brand ala referensi: wordmark besar + tombol cari/profil + chip kategori. */
@Composable
private fun HomeBrandHeader(
    displayName: String,
    onEditName: () -> Unit,
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit,
) {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greetRes = when (hour) {
        in 4..10 -> R.string.greeting_morning
        in 11..14 -> R.string.greeting_noon
        in 15..18 -> R.string.greeting_afternoon
        else -> R.string.greeting_evening
    }
    var selectedChip by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0) }

    Column(Modifier.padding(top = 20.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    "NanzMusify",
                    style = MaterialTheme.typography.headlineLarge,
                    color = NanzMusifyTextPrimary,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(greetRes, displayName),
                        style = MaterialTheme.typography.bodyMedium,
                        color = NanzMusifyTextSecondary,
                    )
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = stringResource(R.string.name_edit_title),
                        tint = NanzMusifyCrimson,
                        modifier = Modifier
                            .size(14.dp)
                            .clickable(onClick = onEditName),
                    )
                }
            }
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(NanzMusifySurface)
                    .clickable(onClick = onSearchClick)
                    .padding(10.dp),
            ) {
                Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.search_kicker), tint = NanzMusifyTextPrimary)
            }
            Spacer(Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(NanzMusifySurface)
                    .clickable(onClick = onProfileClick)
                    .padding(10.dp),
            ) {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = stringResource(R.string.nav_profile),
                    tint = NanzMusifyTextPrimary,
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        val chips = listOf(
            R.string.home_chip_all,
            R.string.home_chip_history,
            R.string.home_chip_for_you,
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            itemsIndexed(chips, key = { i, _ -> "chip_$i" }) { index, labelRes ->
                val selected = index == selectedChip
                Text(
                    text = stringResource(labelRes),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) NanzMusifyBackground else NanzMusifyTextSecondary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (selected) NanzMusifyTextPrimary else NanzMusifySurface)
                        .clickable { selectedChip = index }
                        .padding(horizontal = 16.dp, vertical = 9.dp),
                )
            }
        }
    }
}

/** Kartu mini "Quick Picks" — thumbnail persegi + judul/artis di sisinya, dua per baris. */
@Composable
private fun QuickPickGridCard(
    track: NanzMusifyTrack,
    active: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (active) NanzMusifyElevated else NanzMusifySurface)
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Artwork(url = track.thumbnailUrl, title = track.title, size = 44.dp, cornerRadius = 8.dp)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                track.title,
                style = MaterialTheme.typography.labelMedium,
                color = NanzMusifyTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                track.artist.ifBlank { stringResource(R.string.common_youtube) },
                style = MaterialTheme.typography.labelSmall,
                color = NanzMusifyTextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (active && isPlaying) {
            Icon(
                Icons.Filled.GraphicEq,
                contentDescription = null,
                tint = NanzMusifyCrimson,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

/** Rak "Popular Playlists" — kartu editorial ditata sebagai playlist + tile "Buat Playlist". */
@Composable
private fun PopularPlaylistsRow(
    editorials: List<EditorialSection>,
    onOpen: (EditorialSection) -> Unit,
    onCreatePlaylist: () -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item(key = "create_playlist_tile") {
            Column(
                modifier = Modifier
                    .width(140.dp)
                    .clickable(onClick = onCreatePlaylist),
            ) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, NanzMusifyLine, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("+", style = MaterialTheme.typography.displaySmall, color = NanzMusifyTextSecondary)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.home_create_playlist),
                    style = MaterialTheme.typography.labelMedium,
                    color = NanzMusifyTextPrimary,
                )
            }
        }
        itemsIndexed(editorials, key = { _, e -> e.id }) { _, section ->
            Column(
                modifier = Modifier
                    .width(140.dp)
                    .clickable { onOpen(section) },
            ) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(RoundedCornerShape(16.dp)),
                ) {
                    Image(
                        painter = painterResource(section.previewRes),
                        contentDescription = section.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    section.title,
                    style = MaterialTheme.typography.labelMedium,
                    color = NanzMusifyTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    stringResource(R.string.home_playlist_subtitle),
                    style = MaterialTheme.typography.labelSmall,
                    color = NanzMusifyTextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** Bar pencarian pill ala iOS — semu (tap membuka tab Explore). */
@Composable
private fun HomeSearchBar(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 20.dp)
            .clip(RoundedCornerShape(50))
            .background(NanzMusifySurface.copy(alpha = 0.7f))
            .border(1.dp, NanzMusifyLine.copy(alpha = 0.35f), RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Filled.Search,
            contentDescription = stringResource(R.string.search_kicker),
            tint = NanzMusifyCrimson,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            stringResource(R.string.home_search_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = NanzMusifyTextMuted,
        )
    }
}

/** Dialog ganti nama sapaan — tersimpan permanen di pengaturan. */
@Composable
private fun NameEditDialog(
    current: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf(current)
    }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NanzMusifyElevated,
        title = {
            Text(
                stringResource(R.string.name_edit_title),
                style = MaterialTheme.typography.labelMedium,
                color = NanzMusifyCrimson,
            )
        },
        text = {
            androidx.compose.material3.OutlinedTextField(
                value = name,
                onValueChange = { if (it.length <= 24) name = it },
                singleLine = true,
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NanzMusifyCrimson,
                    unfocusedBorderColor = NanzMusifyLine,
                    cursorColor = NanzMusifyTextPrimary,
                    focusedTextColor = NanzMusifyTextPrimary,
                    unfocusedTextColor = NanzMusifyTextPrimary,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = { if (name.isNotBlank()) onSave(name.trim()) }) {
                Text(stringResource(R.string.action_save), style = MaterialTheme.typography.labelMedium, color = NanzMusifyCrimson)
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel), style = MaterialTheme.typography.labelMedium, color = NanzMusifyTextSecondary)
            }
        },
    )
}

/** Strip "sedang diputar" — artwork bulat kecil + kontrol simpel. */
@Composable
private fun NowPlayingStrip(
    track: NanzMusifyTrack?,
    isPlaying: Boolean,
    isBuffering: Boolean,
    onToggle: () -> Unit,
    onOpen: () -> Unit,
) {
    if (track == null) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(NanzMusifyElevated)
            .clickable(onClick = onOpen)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Artwork(
            url = track.thumbnailUrl,
            title = track.title,
            size = 52.dp,
            cornerRadius = 12.dp,
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                track.title,
                style = MaterialTheme.typography.titleSmall,
                color = NanzMusifyTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                if (isBuffering) stringResource(R.string.home_loading) else track.artist.ifBlank { stringResource(R.string.common_youtube) },
                style = MaterialTheme.typography.bodySmall,
                color = NanzMusifyTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(12.dp))
        NanzMusifyPlayButton(isPlaying = isPlaying, onClick = onToggle, size = 44.dp)
    }
}

@Composable
private fun QuickCard(
    track: NanzMusifyTrack,
    active: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(150.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (active) NanzMusifyElevated else NanzMusifySurface)
            .clickable(onClick = onClick)
            .padding(bottom = 12.dp),
    ) {
        Box(Modifier.size(150.dp, 150.dp)) {
            if (track.thumbnailUrl.isNotBlank()) {
                AsyncImage(
                    model = track.thumbnailUrl,
                    contentDescription = track.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(listOf(NanzMusifyElevated, NanzMusifySurface))),
                )
            }
            if (active) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(CircleShape)
                        .background(NanzMusifyCrimson)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Text(stringResource(R.string.common_playing), style = MaterialTheme.typography.labelSmall, color = NanzMusifyTextPrimary)
                }
            }
        }
        Column(Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Text(
                track.title,
                style = MaterialTheme.typography.titleSmall,
                color = NanzMusifyTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                track.artist.ifBlank { stringResource(R.string.common_youtube) },
                style = MaterialTheme.typography.bodySmall,
                color = NanzMusifyTextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
internal fun EditorialCard(
    section: EditorialSection,
    wide: Boolean,
    onOpen: () -> Unit,
    onPlay: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        BrutalFrame(modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen)) {
            Box(modifier = Modifier.fillMaxWidth().height(if (wide) 240.dp else 260.dp)) {
                Image(
                    painter = painterResource(section.backgroundRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            listOf(NanzMusifyBackground.copy(alpha = 0.25f), NanzMusifyBackground.copy(alpha = 0.9f)),
                        ),
                    ),
                )

                Column(
                    Modifier.fillMaxSize().padding(18.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(section.title, style = MaterialTheme.typography.headlineMedium, color = NanzMusifyTextPrimary)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            section.body,
                            style = MaterialTheme.typography.bodyMedium,
                            color = NanzMusifyTextSecondary,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        NanzMusifyPlayButton(isPlaying = false, onClick = onPlay, size = 44.dp, showOrbit = false)
                        Spacer(Modifier.width(12.dp))
                        Text(stringResource(R.string.common_play), style = MaterialTheme.typography.titleSmall, color = NanzMusifyTextPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun FooterBlock() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(text = "NANZMUSIFY", style = MaterialTheme.typography.headlineSmall, color = NanzMusifyTextMuted)
        Text(
            text = stringResource(R.string.home_quote),
            style = MaterialTheme.typography.bodySmall,
            color = NanzMusifyTextMuted,
        )
        Spacer(Modifier.height(24.dp))
    }
}

/** Chip vibe personality — ketuk = cari konteks itu (#sadvibes → "sadvibes"). */
@Composable
private fun PersonaChips(
    chips: List<String>,
    onChip: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        chips.take(5).forEach { chip ->
            Text(
                text = chip,
                style = MaterialTheme.typography.labelMedium,
                color = NanzMusifyTextSecondary,
                maxLines = 1,
                modifier = Modifier
                    .border(1.dp, NanzMusifyLine, RoundedCornerShape(50))
                    .clickable { onChip(chip) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
    }
}

/**
 * Kartu artis populer per benua — DIPINDAH dari tab Search ke Home.
 * Pengaman anti force close:
 *  - kunci Lazy unik per indeks (nama ganda dari charts tak pernah tabrakan),
 *  - fetch ditunda sampai seksi ini benar-benar dirender (cache 6 jam),
 *  - daftar kurasi cadangan bila charts wilayah tak tersedia.
 */
@Composable
private fun ArtistRegionsBlock(
    region: com.alfath.musicall.app.yt.YouTubeRepository.ArtistRegion,
    artists: List<com.alfath.musicall.app.yt.YouTubeRepository.ArtistCard>,
    loading: Boolean,
    onRegion: (com.alfath.musicall.app.yt.YouTubeRepository.ArtistRegion) -> Unit,
    onArtist: (com.alfath.musicall.app.yt.YouTubeRepository.ArtistCard) -> Unit,
) {
    val regions = com.alfath.musicall.app.yt.YouTubeRepository.ArtistRegion.values()
    val regionLabels = mapOf(
        com.alfath.musicall.app.yt.YouTubeRepository.ArtistRegion.EUROPE to stringResource(R.string.region_europe),
        com.alfath.musicall.app.yt.YouTubeRepository.ArtistRegion.ASIA to stringResource(R.string.region_asia),
        com.alfath.musicall.app.yt.YouTubeRepository.ArtistRegion.AMERICA to stringResource(R.string.region_america),
        com.alfath.musicall.app.yt.YouTubeRepository.ArtistRegion.AFRICA to stringResource(R.string.region_africa),
        com.alfath.musicall.app.yt.YouTubeRepository.ArtistRegion.ARAB to stringResource(R.string.region_arab),
    )

    Spacer(Modifier.height(22.dp))
    SectionRule(label = stringResource(R.string.search_top_artists))
    Spacer(Modifier.height(12.dp))

    // Pemilih benua
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(regions.toList(), key = { i, r -> "reg_${r.name}_$i" }) { _, r ->
            val selected = r == region
            Text(
                text = regionLabels[r].orEmpty(),
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) NanzMusifyCrimson else NanzMusifyTextSecondary,
                modifier = Modifier
                    .border(
                        1.dp,
                        if (selected) NanzMusifyCrimson else NanzMusifyLine,
                        RoundedCornerShape(50),
                    )
                    .background(
                        if (selected) NanzMusifyCrimson.copy(alpha = 0.12f) else NanzMusifySurface.copy(alpha = 0.25f),
                        RoundedCornerShape(50),
                    )
                    .clickable { onRegion(r) }
                    .padding(horizontal = 14.dp, vertical = 7.dp),
            )
        }
    }

    when {
        loading && artists.isEmpty() -> {
            Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    color = NanzMusifyCrimson,
                    modifier = Modifier.size(26.dp),
                )
            }
        }
        artists.isNotEmpty() -> {
            Spacer(Modifier.height(14.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                itemsIndexed(artists, key = { i, a -> "art_${a.name}_$i" }) { _, artist ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(96.dp)
                            .clickable { onArtist(artist) },
                    ) {
                        Box(
                            modifier = Modifier
                                .size(84.dp)
                                .clip(CircleShape)
                                .border(1.dp, NanzMusifyLine, CircleShape)
                                .background(NanzMusifySurface),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (artist.thumbUrl.isNotBlank()) {
                                AsyncImage(
                                    model = artist.thumbUrl,
                                    contentDescription = artist.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            } else {
                                Text(
                                    text = artist.name.take(1).uppercase(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = NanzMusifyCrimson,
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = artist.name,
                            style = MaterialTheme.typography.labelMedium,
                            color = NanzMusifyTextPrimary,
                            maxLines = 2,
                            textAlign = TextAlign.Center,
                        )
                        if (artist.subtitle.isNotBlank()) {
                            Text(
                                text = artist.subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = NanzMusifyTextMuted,
                                maxLines = 1,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Blok "Tren di <negara>": rak genre/mood lokal + daftar lagu tren.
 *
 * Dimuat malas (hanya saat blok ini benar-benar dirender) supaya frame pertama
 * Beranda tidak menunggu jaringan — sama seperti blok artis per benua.
 */
@Composable
private fun TrendingCountryBlock(
    country: String,
    genres: List<com.alfath.musicall.app.yt.BrowseItem>,
    tracks: List<NanzMusifyTrack>,
    loading: Boolean,
    playerState: PlayerUiState,
    likedIds: Set<String>,
    downloadedIds: Set<String>,
    onGenre: (com.alfath.musicall.app.yt.BrowseItem) -> Unit,
    onPlayTracks: (List<NanzMusifyTrack>, Int) -> Unit,
    onLike: (NanzMusifyTrack) -> Unit,
    onTrackMore: (NanzMusifyTrack) -> Unit,
) {
    if (!loading && country.isBlank() && genres.isEmpty() && tracks.isEmpty()) return

    Spacer(Modifier.height(20.dp))
    SectionRule(
        label = if (country.isBlank()) {
            stringResource(R.string.home_trending)
        } else {
            stringResource(R.string.home_trending_in, country)
        },
    )

    if (genres.isNotEmpty()) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            itemsIndexed(genres, key = { _, g -> g.browseId }) { _, genre ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(NanzMusifyRadius.pill))
                        .background(NanzMusifySurface.copy(alpha = 0.5f))
                        .border(1.dp, NanzMusifyLine, RoundedCornerShape(NanzMusifyRadius.pill))
                        .clickable { onGenre(genre) }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                ) {
                    Text(
                        genre.title,
                        style = MaterialTheme.typography.labelMedium,
                        color = NanzMusifyTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }

    when {
        loading && tracks.isEmpty() -> Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressIndicator(color = NanzMusifyCrimson, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(12.dp))
            Text(
                stringResource(R.string.home_trending_loading),
                style = MaterialTheme.typography.labelSmall,
                color = NanzMusifyTextMuted,
            )
        }

        tracks.isNotEmpty() -> Column {
            tracks.take(10).forEachIndexed { index, track ->
                TrackRow(
                    track = track,
                    isActive = playerState.currentTrack?.videoId == track.videoId,
                    isPlaying = playerState.isPlaying,
                    isLiked = likedIds.contains(track.videoId),
                    isDownloaded = downloadedIds.contains(track.videoId),
                    index = index,
                    onPlay = { onPlayTracks(tracks, index) },
                    onLike = { onLike(track) },
                    onMore = { onTrackMore(track) },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp),
                )
            }
        }
    }
}
