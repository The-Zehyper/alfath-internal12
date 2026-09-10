/*
 * Copyright (C) 2026 Nanas
 *
 * SPDX-License-Identifier: GPL-3.0-only
 */
package com.alfath.musicall.app.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.alfath.musicall.app.R
import com.alfath.musicall.app.data.model.NanzMusifyGenre
import com.alfath.musicall.app.ui.theme.NanzMusifyTextSecondary
import com.alfath.musicall.app.ui.theme.NanzMusifyTextPrimary
import com.alfath.musicall.app.ui.theme.NanzMusifyLine
import com.alfath.musicall.app.ui.theme.NanzMusifyBackground
import com.alfath.musicall.app.ui.theme.NanzMusifyCrimson
import com.alfath.musicall.app.ui.theme.NanzMusifyMotion
import com.alfath.musicall.app.ui.theme.nanzmusifySpring

/** Genre reel dari desain asli — kini memicu kueri YouTube nyata. */
@Composable
fun GenreReelSlider(
    genres: List<NanzMusifyGenre>,
    selectedId: String?,
    onGenreClick: (NanzMusifyGenre) -> Unit,
    onPlayGenre: (NanzMusifyGenre) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val fling = rememberSnapFlingBehavior(lazyListState = listState)
    val config = LocalConfiguration.current
    val isWide = config.screenWidthDp >= 600
    val cardWidth = if (isWide) 220.dp else 168.dp

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Column {
                Text(text = stringResource(R.string.genre_header), style = MaterialTheme.typography.labelMedium, color = NanzMusifyTextSecondary)
                Spacer(Modifier.height(4.dp))
                Text(text = stringResource(R.string.genre_sub), style = MaterialTheme.typography.headlineSmall, color = NanzMusifyTextPrimary)
            }
            Text(text = stringResource(R.string.genre_count, genres.size), style = MaterialTheme.typography.labelSmall, color = NanzMusifyTextSecondary)
        }

        Spacer(Modifier.height(16.dp))

        LazyRow(
            state = listState,
            flingBehavior = fling,
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            itemsIndexed(genres, key = { _, g -> g.id }) { index, genre ->
                GenreReelCard(
                    genre = genre,
                    selected = genre.id == selectedId,
                    index = index,
                    modifier = Modifier.width(cardWidth),
                    onClick = { onGenreClick(genre) },
                    onPlay = { onPlayGenre(genre) },
                )
            }
        }
    }
}

@Composable
private fun GenreReelCard(
    genre: NanzMusifyGenre,
    selected: Boolean,
    index: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onPlay: () -> Unit,
) {
    val animScale by animateFloatAsState(
        targetValue = if (selected) 1.02f else 1f,
        animationSpec = nanzmusifySpring(dampingRatio = NanzMusifyMotion.dampingSoft, stiffness = NanzMusifyMotion.stiffnessGentle),
        label = "genre_scale",
    )
    val borderColor = if (selected) NanzMusifyCrimson else NanzMusifyLine

    Box(
        modifier = modifier
            .graphicsLayer { scaleX = animScale; scaleY = animScale }
            .aspectRatio(0.78f)
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, borderColor, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
    ) {
        Image(
            painter = painterResource(genre.imageRes),
            contentDescription = genre.name,
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0.55f) }),
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(NanzMusifyBackground.copy(alpha = 0.15f), NanzMusifyBackground.copy(alpha = 0.88f)),
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp)
                .fillMaxWidth(),
        ) {
            Text(text = "%02d".format(index + 1), style = MaterialTheme.typography.labelSmall, color = NanzMusifyCrimson)
            Spacer(Modifier.height(6.dp))
            Text(
                text = genre.name,
                style = MaterialTheme.typography.titleLarge,
                color = NanzMusifyTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = genre.subtitle.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = NanzMusifyTextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(R.string.common_youtube).uppercase(), style = MaterialTheme.typography.labelSmall, color = NanzMusifyTextSecondary)
                NanzMusifyMiniPlay(isPlaying = selected, onClick = onPlay, size = 34.dp)
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(1.dp)
                .background(if (selected) NanzMusifyCrimson else Color.Transparent),
        )
    }
}
