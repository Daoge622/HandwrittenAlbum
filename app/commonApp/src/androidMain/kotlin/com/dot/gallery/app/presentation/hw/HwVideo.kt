/*
 * SPDX-FileCopyrightText: 2026 手写相册 contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.dot.gallery.app.presentation.hw

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import androidx.media3.ui.compose.modifiers.resizeWithContentScale
import androidx.compose.ui.layout.ContentScale
import com.dot.gallery.domain.model.UriMedia
import com.dot.gallery.domain.util.getUri
import kotlinx.coroutines.delay

/*
 * 查看器里的视频：只在当前页时播放；点一下暂停/继续；底部一条细进度线和时间。
 * 播放内核用 Media3 ExoPlayer（ReFra 已集成，Apache-2.0）。
 */
@OptIn(UnstableApi::class)
@Composable
fun HwVideo(media: UriMedia, active: Boolean, uiAlpha: Float) {
    val context = LocalContext.current
    val player = remember(media.id) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(media.getUri()))
            repeatMode = Player.REPEAT_MODE_ONE
            prepare()
        }
    }
    DisposableEffect(player) { onDispose { player.release() } }
    LaunchedEffect(active) { if (active) player.play() else player.pause() }

    val presentation = androidx.media3.ui.compose.state.rememberPresentationState(player)
    var playing by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var label by remember { mutableStateOf("") }
    LaunchedEffect(player) {
        while (true) {
            playing = player.isPlaying
            val d = player.duration.coerceAtLeast(1)
            progress = (player.currentPosition.toFloat() / d).coerceIn(0f, 1f)
            fun fmt(ms: Long) = "%d:%02d".format(ms / 60000, (ms / 1000) % 60)
            label = if (player.duration > 0) "${fmt(player.currentPosition)} / ${fmt(player.duration)}" else ""
            delay(200)
        }
    }

    Box(
        Modifier.fillMaxSize().clickable(remember { MutableInteractionSource() }, indication = null) {
            if (player.isPlaying) player.pause() else player.play()
        },
        contentAlignment = Alignment.Center
    ) {
        PlayerSurface(
            player = player,
            surfaceType = SURFACE_TYPE_TEXTURE_VIEW,
            modifier = Modifier.fillMaxSize().resizeWithContentScale(ContentScale.Fit, presentation.videoSizeDp)
        )
        if (!playing) {
            Box(Modifier.background(Color.Black.copy(alpha = 0.35f), CircleShape).padding(horizontal = 22.dp, vertical = 12.dp)) {
                Text("继续", color = Color.White, fontSize = 15.sp)
            }
        }
        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().navigationBarsPadding().padding(start = 24.dp, end = 24.dp, bottom = 74.dp)
        ) {
            if (uiAlpha > 0f) {
                Row(Modifier.fillMaxWidth()) {
                    Box(Modifier.weight(1f).height(1.dp).background(Color.White.copy(alpha = 0.3f))) {
                        Box(Modifier.fillMaxWidth(progress).height(1.dp).background(Color.White))
                    }
                }
                Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp, modifier = Modifier.padding(top = 6.dp))
            }
        }
    }
}
