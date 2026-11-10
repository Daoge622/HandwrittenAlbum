/*
 * SPDX-FileCopyrightText: 2026 手写相册 contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.dot.gallery.app.presentation.hw

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.dot.gallery.core.LocalEventHandler
import com.dot.gallery.core.LocalMediaHandler
import com.dot.gallery.core.navigateUp
import com.dot.gallery.core.ui.util.rememberActivityResult
import com.dot.gallery.data.repository.MediaMutationResult
import com.dot.gallery.domain.model.MediaState
import com.dot.gallery.domain.model.UriMedia
import com.dot.gallery.domain.util.getUri
import com.github.panpf.sketch.AsyncImage
import com.github.panpf.sketch.request.ComposableImageRequest
import kotlinx.coroutines.launch

enum class HwCollection { Trash, Favorites }

/*
 * 回收站 / 收藏：
 * - 回收站：点按即选择；底部“还原 / 彻底删除”（彻底删除由系统弹窗确认）；每张显示剩余天数；
 * - 收藏：点按用自己的查看器打开，长按多选后“取消收藏 / 分享”。
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HwCollectionScreen(kind: HwCollection, mediaState: State<MediaState<UriMedia>>) {
    val colors = MaterialTheme.colorScheme
    val context = androidx.compose.ui.platform.LocalContext.current
    val eventHandler = LocalEventHandler.current
    val handler = LocalMediaHandler.current
    val viewer = LocalHwViewer.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val items = mediaState.value.media
    val selected = remember { mutableStateListOf<Long>() }
    val selectedSet = remember(selected.toList()) { selected.toHashSet() }
    val selecting = selected.isNotEmpty()
    val restoreRequest = rememberActivityResult(onResultOk = { selected.clear() })
    val deleteRequest = rememberActivityResult(onResultOk = { selected.clear() })
    val unfavRequest = rememberActivityResult(onResultOk = { selected.clear() })

    BackHandler(enabled = selecting) { selected.clear() }

    Box(Modifier.fillMaxSize().background(colors.background)) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(start = 12.dp, end = 20.dp, top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("‹", fontSize = 30.sp, color = colors.onBackground, modifier = Modifier.clickable { eventHandler.navigateUp() }.padding(horizontal = 12.dp))
                Text(
                    if (selecting) "已选 ${selected.size} 张" else if (kind == HwCollection.Trash) "回收站" else "收藏",
                    fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 24.sp, letterSpacing = 0.06.em
                )
                Spacer(Modifier.weight(1f))
                if (items.isNotEmpty()) Text(
                    if (selected.size == items.size) "全不选" else "全选", color = colors.primary, fontSize = 14.sp,
                    modifier = Modifier.clickable {
                        if (selected.size == items.size) selected.clear() else { selected.clear(); selected.addAll(items.map { it.id }) }
                    }.padding(8.dp)
                )
            }
            Text(
                if (items.isEmpty()) (if (kind == HwCollection.Trash) "回收站是空的" else "还没有收藏的照片")
                else if (kind == HwCollection.Trash) "${items.size} 张 · 到期后自动删除" else "${items.size} 张",
                style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant,
                modifier = Modifier.padding(start = 24.dp, top = 4.dp, bottom = 14.dp)
            )
            HorizontalDivider(Modifier.padding(horizontal = 24.dp), color = colors.outline, thickness = 0.5.dp)
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(top = 2.dp, bottom = 120.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(items, key = { it.id }) { m ->
                    val isSel = m.id in selectedSet
                    val shrink by animateFloatAsState(if (isSel) 0.86f else 1f, spring(dampingRatio = 0.7f, stiffness = 520f), label = "sel")
                    Box(
                        Modifier.aspectRatio(1f).background(colors.surfaceVariant)
                            .hwThumb(viewer, m)
                            .combinedClickable(
                                onClick = {
                                    if (kind == HwCollection.Trash || selecting) { if (isSel) selected.remove(m.id) else selected.add(m.id) }
                                    else viewer?.open(items, m)
                                },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    if (isSel) selected.remove(m.id) else selected.add(m.id)
                                }
                            )
                    ) {
                        AsyncImage(
                            request = ComposableImageRequest(m.getUri().toString()), contentDescription = m.label,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().graphicsLayer { scaleX = shrink; scaleY = shrink }
                        )
                        if (kind == HwCollection.Trash) m.expiryTimestamp?.let { exp ->
                            val days = ((exp * 1000 - System.currentTimeMillis()) / 86_400_000L).coerceAtLeast(0)
                            Text(
                                "$days 天", color = Color.White, fontSize = 10.sp,
                                modifier = Modifier.align(Alignment.BottomStart).padding(5.dp)
                            )
                        }
                        if (selecting || kind == HwCollection.Trash) SelectMark(isSel, Modifier.align(Alignment.TopEnd).padding(6.dp).size(20.dp))
                    }
                }
            }
        }

        if (selecting) {
            val picked = items.filter { it.id in selectedSet }
            Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(colors.background).navigationBarsPadding()) {
                HorizontalDivider(color = colors.outline, thickness = 0.5.dp)
                Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    if (kind == HwCollection.Trash) {
                        Action("还原") {
                            scope.launch { if (handler.trashMedia(restoreRequest, picked, false) == MediaMutationResult.COMPLETED) selected.clear() }
                        }
                        Action("彻底删除", danger = true) {
                            scope.launch { if (handler.deleteMedia(deleteRequest, picked) == MediaMutationResult.COMPLETED) selected.clear() }
                        }
                    } else {
                        Action("取消收藏") { scope.launch { handler.toggleFavorite(unfavRequest, picked, false) } }
                        Action("分享") {
                            val uris = ArrayList(picked.map { it.getUri() })
                            val i = android.content.Intent(android.content.Intent.ACTION_SEND_MULTIPLE).apply {
                                type = "*/*"; putParcelableArrayListExtra(android.content.Intent.EXTRA_STREAM, uris)
                                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(android.content.Intent.createChooser(i, null))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Action(label: String, danger: Boolean = false, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Text(
        label, color = if (danger) colors.primary else colors.onBackground,
        fontFamily = FontFamily.Serif, fontSize = 15.sp, letterSpacing = 0.15.em,
        modifier = Modifier.clickable(onClick = onClick).padding(horizontal = 18.dp, vertical = 14.dp)
    )
}
