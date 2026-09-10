/*
 * Copyright (C) 2026 Nanas
 *
 * SPDX-License-Identifier: GPL-3.0-only
 */
package com.alfath.musicall.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.alfath.musicall.app.core.ServiceLocator
import com.alfath.musicall.app.download.DownloadService
import com.alfath.musicall.app.yt.YouTubeRepository

/**
 * NanzMusify — Hear What Words Can't Say.
 * Client musik YouTube audio-only: pencarian, playlist, unduhan, background play.
 */
class NanzMusifyApp : Application() {

    lateinit var locator: ServiceLocator
        private set

    override fun onCreate() {
        super.onCreate()
        YouTubeRepository.initNewPipe()
        locator = ServiceLocator(this)
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java) ?: return
        val downloads = NotificationChannel(
            DownloadService.CHANNEL_ID,
            getString(R.string.downloads_channel),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = getString(R.string.downloads_channel_desc)
            setShowBadge(false)
        }
        manager.createNotificationChannel(downloads)
    }
}
