/*
 * Copyright (C) 2026 Nanas
 *
 * SPDX-License-Identifier: GPL-3.0-only
 */
package com.alfath.musicall.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.RemoteViews
import com.alfath.musicall.app.NanzMusifyApp
import com.alfath.musicall.app.R
import com.alfath.musicall.app.data.model.NanzMusifyTrack
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Widget "kotak" lagu offline di layar utama (wishlist tombol kiri/kanan pilih
 * lagu terunduh). Berbeda dari [PlayerWidgetReceiver] (transport lagu yang
 * SEDANG diputar) — widget ini adalah PEMILIH lagu terunduh: ketuk label
 * kategori untuk berpindah playlist/favorit/semua unduhan, ketuk kiri/kanan
 * untuk pindah & langsung memutar lagu offline lain, semuanya tanpa perlu
 * membuka aplikasi sama sekali.
 *
 * Semua kategori (semua playlist + Favorit + Semua Unduhan) selalu bisa
 * dipilih ketika perangkat online. Begitu perangkat TANPA koneksi, daftar
 * kategori yang bisa diketuk otomatis disaring hanya ke kategori yang
 * benar-benar punya lagu terunduh — sesuai permintaan produk.
 */
class OfflineWidgetReceiver : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        refreshAll(context, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val manager = AppWidgetManager.getInstance(context) ?: return
        val ids = manager.getAppWidgetIds(ComponentName(context, OfflineWidgetReceiver::class.java))
        if (ids.isEmpty()) return

        when (intent.action) {
            ACTION_PREV -> handleStep(context, ids, -1)
            ACTION_NEXT -> handleStep(context, ids, 1)
            ACTION_CATEGORY -> handleCategorySwitch(context, ids)
            ACTION_PLAY -> handlePlay(context, ids)
        }
    }

    // ------------------------------------------------------------------
    // Aksi tombol
    // ------------------------------------------------------------------

    private fun handleStep(context: Context, ids: IntArray, direction: Int) {
        val pending = goAsync()
        scope.launch {
            runCatching {
                val categories = eligibleCategories(context)
                if (categories.isEmpty()) return@runCatching
                val currentCategoryId = OfflineWidgetState.selectedCategoryId(context)
                val category = categories.firstOrNull { it.id == currentCategoryId } ?: categories.first()
                if (category.tracks.isEmpty()) return@runCatching

                val currentIndex = OfflineWidgetState.selectedTrackIndex(context)
                val nextIndex = (currentIndex + direction + category.tracks.size) % category.tracks.size
                OfflineWidgetState.saveSelection(context, category.id, nextIndex)

                playTrack(context, category, nextIndex)
                renderAll(context, ids, categories, category.id, nextIndex)
            }
            runCatching { pending.finish() }
        }
    }

    private fun handleCategorySwitch(context: Context, ids: IntArray) {
        val pending = goAsync()
        scope.launch {
            runCatching {
                val categories = eligibleCategories(context)
                if (categories.isEmpty()) return@runCatching
                val currentCategoryId = OfflineWidgetState.selectedCategoryId(context)
                val currentPos = categories.indexOfFirst { it.id == currentCategoryId }.coerceAtLeast(0)
                val next = categories[(currentPos + 1) % categories.size]
                OfflineWidgetState.saveSelection(context, next.id, 0)
                renderAll(context, ids, categories, next.id, 0)
            }
            runCatching { pending.finish() }
        }
    }

    private fun handlePlay(context: Context, ids: IntArray) {
        val pending = goAsync()
        scope.launch {
            runCatching {
                val categories = eligibleCategories(context)
                val currentCategoryId = OfflineWidgetState.selectedCategoryId(context)
                val category = categories.firstOrNull { it.id == currentCategoryId } ?: categories.firstOrNull()
                if (category != null && category.tracks.isNotEmpty()) {
                    val index = OfflineWidgetState.selectedTrackIndex(context).coerceIn(0, category.tracks.lastIndex)
                    val selected = category.tracks[index]
                    val nowPlaying = WidgetState.snapshot
                    if (nowPlaying.videoId == selected.videoId) {
                        // Lagu terpilih sudah dimuat — cukup putar/jeda, jangan muat ulang antrean.
                        (context.applicationContext as NanzMusifyApp).locator.player.toggle()
                    } else {
                        playTrack(context, category, index)
                    }
                }
            }
            runCatching { pending.finish() }
        }
    }

    /** Mulai memutar lagu terpilih lewat PlayerManager — sama seperti tombol putar di dalam aplikasi. */
    private fun playTrack(context: Context, category: OfflineWidgetCategory, index: Int) {
        val locator = (context.applicationContext as NanzMusifyApp).locator
        val queue = category.tracks.map {
            NanzMusifyTrack(it.videoId, it.title, it.artist, it.album, it.durationSec, it.thumbnailUrl)
        }
        locator.player.playQueue(queue, index, autoplay = true)
    }

    // ------------------------------------------------------------------
    // Render
    // ------------------------------------------------------------------

    private fun refreshAll(context: Context, ids: IntArray) {
        val pending = goAsync()
        scope.launch {
            runCatching {
                val categories = eligibleCategories(context)
                val categoryId = OfflineWidgetState.selectedCategoryId(context)
                val index = OfflineWidgetState.selectedTrackIndex(context)
                renderAll(context, ids, categories, categoryId, index)
            }
            runCatching { pending.finish() }
        }
    }

    /** Kategori yang boleh dipilih widget SEKARANG: semua kategori saat online, kategori berisi lagu offline saja saat tidak ada koneksi. */
    private suspend fun eligibleCategories(context: Context): List<OfflineWidgetCategory> {
        val all = OfflineWidgetState.loadCategories(context)
        return if (isOnline(context)) all else all.filter { it.tracks.isNotEmpty() }
    }

    private fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return true
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun renderAll(
        context: Context,
        ids: IntArray,
        categories: List<OfflineWidgetCategory>,
        categoryId: Long,
        trackIndex: Int,
    ) {
        val manager = AppWidgetManager.getInstance(context) ?: return
        val category = categories.firstOrNull { it.id == categoryId } ?: categories.firstOrNull()
        val track = category?.tracks?.getOrNull(trackIndex.coerceIn(0, (category.tracks.size - 1).coerceAtLeast(0)))

        val nowPlaying = WidgetState.snapshot
        val isPlayingSelected = track != null && nowPlaying.isPlaying && nowPlaying.videoId == track.videoId

        ids.forEach { id ->
            runCatching { manager.updateAppWidget(id, buildViews(context, category, track, null, isPlayingSelected)) }
        }
        val artUrl = track?.thumbnailUrl.orEmpty()
        if (artUrl.isBlank()) return
        val art = loadArtwork(context, artUrl) ?: return
        ids.forEach { id ->
            runCatching { manager.updateAppWidget(id, buildViews(context, category, track, art, isPlayingSelected)) }
        }
    }

    private fun buildViews(
        context: Context,
        category: OfflineWidgetCategory?,
        track: OfflineWidgetTrack?,
        art: Bitmap?,
        isPlayingSelected: Boolean,
    ): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_offline_player)

        views.setTextViewText(
            R.id.offline_widget_category_label,
            category?.name?.ifBlank { context.getString(R.string.offline_widget_category_all) }
                ?: context.getString(R.string.offline_widget_category_all),
        )

        views.setTextViewText(
            R.id.offline_widget_title,
            track?.title ?: context.getString(R.string.offline_widget_empty_title),
        )
        views.setTextViewText(
            R.id.offline_widget_artist,
            when {
                track != null -> track.artist.ifBlank { context.getString(R.string.common_youtube) }
                category != null && category.tracks.isEmpty() -> context.getString(R.string.offline_widget_empty_category)
                else -> ""
            },
        )

        if (art != null) {
            views.setImageViewBitmap(R.id.offline_widget_art, art)
        } else {
            views.setImageViewResource(R.id.offline_widget_art, R.drawable.ic_widget_note)
        }
        views.setImageViewResource(
            R.id.offline_widget_play,
            if (isPlayingSelected) R.drawable.ic_widget_pause else R.drawable.ic_widget_play,
        )

        views.setOnClickPendingIntent(R.id.offline_widget_category, actionIntent(context, ACTION_CATEGORY, REQ_CATEGORY))
        views.setOnClickPendingIntent(R.id.offline_widget_prev, actionIntent(context, ACTION_PREV, REQ_PREV))
        views.setOnClickPendingIntent(R.id.offline_widget_play, actionIntent(context, ACTION_PLAY, REQ_PLAY))
        views.setOnClickPendingIntent(R.id.offline_widget_next, actionIntent(context, ACTION_NEXT, REQ_NEXT))
        views.setOnClickPendingIntent(R.id.offline_widget_root, openApp(context))
        return views
    }

    private fun actionIntent(context: Context, action: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, OfflineWidgetReceiver::class.java).apply { this.action = action }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun openApp(context: Context): PendingIntent {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?: Intent(Intent.ACTION_MAIN)
        return PendingIntent.getActivity(
            context,
            REQ_OPEN_APP,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun loadArtwork(context: Context, url: String): Bitmap? {
        cachedArt?.let { if (cachedArtUrl == url) return it }
        val decoded = runCatching {
            if (url.startsWith("file://") || url.startsWith("content://")) {
                context.contentResolver.openInputStream(android.net.Uri.parse(url))
                    ?.use { BitmapFactory.decodeStream(it) }
            } else {
                httpClient.newCall(Request.Builder().url(url).build()).execute().use { response ->
                    if (!response.isSuccessful) null else response.body?.byteStream()?.let { BitmapFactory.decodeStream(it) }
                }
            }
        }.getOrNull() ?: return null
        val art = if (decoded.width > ART_MAX_PX || decoded.height > ART_MAX_PX) {
            runCatching { Bitmap.createScaledBitmap(decoded, ART_MAX_PX, ART_MAX_PX, true) }.getOrDefault(decoded)
        } else {
            decoded
        }
        cachedArtUrl = url
        cachedArt = art
        return art
    }

    companion object {
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        private val httpClient by lazy { OkHttpClient() }

        private const val ART_MAX_PX = 256

        @Volatile
        private var cachedArtUrl: String = ""

        @Volatile
        private var cachedArt: Bitmap? = null

        const val ACTION_PREV = "com.alfath.musicall.app.offlinewidget.PREV"
        const val ACTION_NEXT = "com.alfath.musicall.app.offlinewidget.NEXT"
        const val ACTION_CATEGORY = "com.alfath.musicall.app.offlinewidget.CATEGORY"
        const val ACTION_PLAY = "com.alfath.musicall.app.offlinewidget.PLAY"

        private const val REQ_OPEN_APP = 20
        private const val REQ_CATEGORY = 21
        private const val REQ_PREV = 22
        private const val REQ_PLAY = 23
        private const val REQ_NEXT = 24

        /** Minta launcher merender ulang semua instance widget offline ini. */
        fun refresh(context: Context) {
            runCatching {
                val manager = AppWidgetManager.getInstance(context) ?: return
                val ids = manager.getAppWidgetIds(ComponentName(context, OfflineWidgetReceiver::class.java))
                if (ids.isEmpty()) return
                val intent = Intent(context, OfflineWidgetReceiver::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
                context.sendBroadcast(intent)
            }
        }
    }
}
