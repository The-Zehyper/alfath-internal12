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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.alfath.musicall.app.R
import com.alfath.musicall.app.data.model.NanzMusifyArchive
import com.alfath.musicall.app.ui.components.NanzMusifyPlayButton
import com.alfath.musicall.app.ui.theme.NanzMusifyTextSecondary
import com.alfath.musicall.app.ui.theme.NanzMusifyTextPrimary
import com.alfath.musicall.app.ui.theme.NanzMusifyLine
import com.alfath.musicall.app.ui.theme.NanzMusifyBackground
import com.alfath.musicall.app.ui.theme.NanzMusifyCrimson
import com.alfath.musicall.app.ui.theme.NanzMusifyTextMuted

@Composable
fun EditorialDetailScreen(
    sectionId: String,
    onBack: () -> Unit,
    onPlayMovement: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val section = NanzMusifyArchive.editorials.find { it.id == sectionId } ?: NanzMusifyArchive.editorials.first()

    Box(modifier = modifier.fillMaxSize().background(NanzMusifyBackground)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp),
        ) {
            item {
                Box(Modifier.fillMaxWidth().height(340.dp)) {
                    Image(
                        painter = painterResource(section.backgroundRes),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                    Box(
                        Modifier.fillMaxSize().background(
                            Brush.verticalGradient(
                                listOf(NanzMusifyBackground.copy(alpha = 0.2f), NanzMusifyBackground),
                            ),
                        ),
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.action_back),
                                tint = NanzMusifyTextPrimary,
                            )
                        }
                        Text(stringResource(R.string.editorial_kicker), style = MaterialTheme.typography.labelMedium, color = NanzMusifyTextSecondary)
                    }
                    Column(
                        Modifier
                            .align(Alignment.BottomStart)
                            .padding(20.dp),
                    ) {
                        Text(section.indexLabel, style = MaterialTheme.typography.labelMedium, color = NanzMusifyCrimson)
                        Spacer(Modifier.height(8.dp))
                        Text(section.title, style = MaterialTheme.typography.displaySmall, color = NanzMusifyTextPrimary)
                    }
                }
            }

            item {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        section.body,
                        style = MaterialTheme.typography.bodyLarge,
                        color = NanzMusifyTextSecondary,
                    )
                    Spacer(Modifier.height(24.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, NanzMusifyCrimson)
                            .background(NanzMusifyCrimson.copy(alpha = 0.14f))
                            .clickable { onPlayMovement(section.searchQuery) }
                            .padding(18.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            NanzMusifyPlayButton(
                                isPlaying = false,
                                onClick = { onPlayMovement(section.searchQuery) },
                                size = 52.dp,
                                showOrbit = true,
                            )
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(
                                    stringResource(R.string.editorial_play),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = NanzMusifyTextPrimary,
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    Text(
                        stringResource(R.string.editorial_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = NanzMusifyTextMuted,
                    )
                }
            }
        }
    }
}
