/*
 * Copyright (C) 2026 Nanas
 *
 * SPDX-License-Identifier: GPL-3.0-only
 */
package com.alfath.musicall.app.widget

import android.content.Context
import com.alfath.musicall.app.NanzMusifyApp
import com.alfath.musicall.app.data.db.DownloadEntity
import com.alfath.musicall.app.data.db.DownloadState
import kotlinx.coroutines.flow.first

/** Satu lagu unduhan siap tampil di widget offline (ringan, tanpa dependensi Compose). */
data class OfflineWidgetTrack(
    val videoId: String,
    val title: String,
    val artist: String,
    val album: String,
    val thumbnailUrl: String,
    val durationSec: Long,
)

/** Satu "kategori" yang bisa dipilih di widget: Semua Unduhan, Favorit, atau playlist buatan pengguna. */
data class OfflineWidgetCategory(
    val id: Long,
    val name: String,
    val tracks: List<OfflineWidgetTrack>,
)

private fun DownloadEntity.toWidgetTrack() =
    OfflineWidgetTrack(videoId, title, artist, album, thumbnailUrl, durationSec)

/**
 * Sumber data + status pilihan (kategori & indeks lagu aktif) untuk widget lagu
 * offline (wishlist tombol kiri/kanan pilih lagu terunduh, wishlist #17).
 *
 * Kategori yang dikembalikan SELALU lengkap (semua playlist, termasuk yang kosong
 * dari unduhan) — [OfflineWidgetReceiver] sendiri yang menyaring ke kategori
 * berisi lagu offline saja ketika perangkat sedang tanpa koneksi, sesuai
 * permintaan "tampilkan semua kategori tapi hanya kategori offline saat offline".
 */
object OfflineWidgetState {

    const val ALL_DOWNLOADS_ID = -1L
    const val LIKED_ID = -2L

    private const val PREFS = "nanzmusify_offline_widget"
    private const val KEY_CATEGORY = "selected_category_id"
    private const val KEY_TRACK_INDEX = "selected_track_index"

    /** Snapshot kategori terakhir yang berhasil dimuat — dipakai render cepat sebelum query DB selesai. */
    @Volatile
    var lastCategories: List<OfflineWidgetCategory> = emptyList()
        private set

    suspend fun loadCategories(context: Context): List<OfflineWidgetCategory> {
        val locator = (context.applicationContext as NanzMusifyApp).locator
        val db = locator.db
        val downloads = db.downloadDao().all().first().filter { it.state == DownloadState.DONE }
        val downloadMap = downloads.associateBy { it.videoId }

        val allCategory = OfflineWidgetCategory(
            id = ALL_DOWNLOADS_ID,
            name = "Semua Unduhan",
            tracks = downloads.map { it.toWidgetTrack() },
        )

        val likedIds = db.libraryDao().likedTracks().first().map { it.videoId }
        val likedCategory = OfflineWidgetCategory(
            id = LIKED_ID,
            name = "Favorit",
            tracks = likedIds.mapNotNull { downloadMap[it]?.toWidgetTrack() },
        )

        val playlistCategories = db.libraryDao().playlists().first().map { playlist ->
            val itemIds = db.libraryDao().playlistItemsOnce(playlist.id).map { it.videoId }
            OfflineWidgetCategory(
                id = playlist.id,
                name = playlist.name,
                tracks = itemIds.mapNotNull { downloadMap[it]?.toWidgetTrack() },
            )
        }

        val result = listOf(allCategory, likedCategory) + playlistCategories
        lastCategories = result
        return result
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun selectedCategoryId(context: Context): Long =
        prefs(context).getLong(KEY_CATEGORY, ALL_DOWNLOADS_ID)

    fun selectedTrackIndex(context: Context): Int =
        prefs(context).getInt(KEY_TRACK_INDEX, 0)

    fun saveSelection(context: Context, categoryId: Long, trackIndex: Int) {
        prefs(context).edit()
            .putLong(KEY_CATEGORY, categoryId)
            .putInt(KEY_TRACK_INDEX, trackIndex)
            .apply()
    }
}
