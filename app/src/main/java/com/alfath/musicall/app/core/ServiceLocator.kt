/*
 * Copyright (C) 2026 Nanas
 *
 * SPDX-License-Identifier: GPL-3.0-only
 */
package com.alfath.musicall.app.core

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.alfath.musicall.app.NanzMusifyApp
import com.alfath.musicall.app.data.LibraryRepository
import com.alfath.musicall.app.data.db.DownloadState
import com.alfath.musicall.app.data.db.NanzMusifyDatabase
import com.alfath.musicall.app.data.model.NanzMusifyTrack
import com.alfath.musicall.app.data.settings.SettingsRepository
import com.alfath.musicall.app.download.NanzMusifyDownloadManager
import com.alfath.musicall.app.lyrics.LyricsRepository
import com.alfath.musicall.app.player.PlayerManager
import com.alfath.musicall.app.yt.YouTubeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

private val Context.sessionStore by preferencesDataStore(name = "nanzmusify_session")

/** Menyimpan antrean & posisi terakhir agar sesi dengar bisa dilanjutkan. */
class SessionStore(private val context: Context) {

    @Serializable
    data class SavedSession(
        val queue: List<NanzMusifyTrack> = emptyList(),
        val index: Int = 0,
        val positionMs: Long = 0L,
        val shuffle: Boolean = false,
        val repeatMode: Int = 0,
    )

    private val json = Json { ignoreUnknownKeys = true }
    private val key = stringPreferencesKey("session_json")

    suspend fun save(queue: List<NanzMusifyTrack>, index: Int, positionMs: Long, shuffle: Boolean, repeatMode: Int) {
        val data = SavedSession(queue.take(200), index, positionMs, shuffle, repeatMode)
        runCatching {
            context.sessionStore.edit { it[key] = json.encodeToString(SavedSession.serializer(), data) }
        }
    }

    suspend fun load(): SavedSession? = runCatching {
        val raw = context.sessionStore.data.map { it[key] }.first() ?: return null
        json.decodeFromString(SavedSession.serializer(), raw)
    }.getOrNull()
}

/** Service locator sederhana (manual DI) — satu instance per aplikasi. */
class ServiceLocator(val app: NanzMusifyApp) {

    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val db: NanzMusifyDatabase by lazy {
        Room.databaseBuilder(app, NanzMusifyDatabase::class.java, "nanzmusify.db")
            .fallbackToDestructiveMigration(dropAllTables = false)
            .build()
    }

    val library: LibraryRepository by lazy {
        LibraryRepository(db.libraryDao(), db.downloadDao())
    }

    val settings: SettingsRepository by lazy { SettingsRepository(app) }

    /**
     * NanzMusify berjalan ANONIM penuh: tidak ada cookie/kredensial akun di mana pun.
     * Lihat notes/02 §1 — jangan menambahkan jalur login tanpa keputusan produk.
     */
    /** Profil selera pengguna untuk algoritma rekomendasi (lokal, on-device). */
    val taste: com.alfath.musicall.app.data.taste.TasteRepository by lazy {
        com.alfath.musicall.app.data.taste.TasteRepository(app)
    }

    /** Query pencarian yang diminta dari layar lain (mis. chip vibe di Home). */
    val pendingSearchQuery = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)

    val youtube: YouTubeRepository by lazy { YouTubeRepository() }

    val lyrics: LyricsRepository by lazy { LyricsRepository(settings) }

    val local: com.alfath.musicall.app.local.LocalMusicRepository by lazy {
        com.alfath.musicall.app.local.LocalMusicRepository(app)
    }

    val session: SessionStore by lazy { SessionStore(app) }

    val player: PlayerManager by lazy { PlayerManager(app, this) }

    val downloads: NanzMusifyDownloadManager by lazy { NanzMusifyDownloadManager(app, this) }

    init {
        // Kualitas stream mengikuti pengaturan
        appScope.launch {
            settings.settings.collect { youtube.defaultQuality = it.audioQuality }
        }
        // Muat profil selera tersimpan (algoritma rekomendasi)
        appScope.launch { taste.warm() }
        // Unduhan yang terpotong karena proses mati → antre ulang
        appScope.launch {
            runCatching { db.downloadDao().resetDownloadingToQueued() }
        }
    }

    /** Dipakai ResolvingDataSource: file unduhan (jika ada & utuh) mengalahkan stream. */
    fun downloadedFileFor(videoId: String): String? = runBlocking(Dispatchers.IO) {
        val row = db.downloadDao().byIdOnce(videoId) ?: return@runBlocking null
        if (row.state != DownloadState.DONE) return@runBlocking null
        val path = row.filePath ?: return@runBlocking null
        if (File(path).exists() && File(path).length() > 0L) path else null
    }
}
