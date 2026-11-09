/*
 * SPDX-FileCopyrightText: 2026 手写相册 contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.dot.gallery.app.presentation.hw

import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.dot.gallery.domain.model.MediaState
import com.dot.gallery.domain.model.UriMedia
import com.dot.gallery.domain.model.definedTimestamp
import com.dot.gallery.domain.util.getUri
import com.dot.gallery.domain.util.isVideo
import com.github.panpf.sketch.AsyncImage
import com.github.panpf.sketch.request.ComposableImageRequest
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId
import kotlin.math.hypot

/*
 * 手写相册「相册」页：
 * - 全部照片按月分组的网格；双指捏合在 2–6 列之间切换，每张照片弹簧滑到新位置（LazyGrid animateItem）；
 * - 文件夹视图：封面 + 名称 + 张数，点开在本页筛选；
 * - 搜索：附言正文、自定义栏目、相册名、文件名、日期（如 “2025”、“11月”）；
 * - 有附言的照片右下角一个朱红小点。点照片用自己的查看器从原位展开。
 */

private val zone: ZoneId = ZoneId.systemDefault()
private val DateFamily = androidx.compose.ui.text.font.FontFamily(androidx.compose.ui.text.font.Font(com.dot.gallery.R.font.hw_cormorant_medium))
private val ItemSpring = spring<IntOffset>(dampingRatio = 0.86f, stiffness = 320f)

private sealed interface Cell {
    data class Header(val key: String, val year: Int, val month: Int, val count: Int) : Cell
    data class Photo(val media: UriMedia) : Cell
}

@Composable
fun HwAlbumScreen(paddingValues: PaddingValues, mediaState: State<MediaState<UriMedia>>) {
    val viewer = LocalHwViewer.current
    val colors = MaterialTheme.colorScheme
    val repo = rememberNotesRepository()
    val all = mediaState.value.media
    val withNotes by remember { repo.mediaIdsWithNotes() }.collectAsState(initial = emptyList())
    val noteSet = remember(withNotes) { withNotes.toHashSet() }

    var cols by remember { mutableIntStateOf(4) }
    var folders by remember { mutableStateOf(false) }
    var album by remember { mutableStateOf<Pair<Long, String>?>(null) }
    var query by remember { mutableStateOf("") }
    var noteHits by remember { mutableStateOf<Set<Long>>(emptySet()) }

    // 附言与栏目的匹配走数据库，停顿 250ms 再查
    LaunchedEffect(query) {
        val q = query.trim()
        if (q.isEmpty()) { noteHits = emptySet(); return@LaunchedEffect }
        delay(250)
        noteHits = repo.searchMediaIds(q).toHashSet()
    }

    val shown = remember(all, album, query, noteHits) {
        val q = query.trim()
        all.asSequence()
            .filter { album == null || it.albumID == album!!.first }
            .filter { m ->
                if (q.isEmpty()) true else {
                    val d = Instant.ofEpochSecond(m.definedTimestamp).atZone(zone)
                    m.id in noteHits || m.albumLabel.contains(q, true) || m.label.contains(q, true) ||
                        "${d.year}".contains(q) || "${d.year}年${d.monthValue}月".contains(q) || "${d.monthValue}月".let { q == it } ||
                        "${d.year}.${d.monthValue}".contains(q) || "${d.monthValue}月${d.dayOfMonth}日".contains(q)
                }
            }.toList()
    }
    val cells = remember(shown) {
        val out = ArrayList<Cell>(shown.size + 32)
        shown.groupBy { Instant.ofEpochSecond(it.definedTimestamp).atZone(zone).let { d -> d.year * 100 + d.monthValue } }
            .forEach { (ym, list) ->
                out += Cell.Header("h$ym", ym / 100, ym % 100, list.size)
                list.forEach { out += Cell.Photo(it) }
            }
        out
    }
    val albums = remember(all) {
        all.groupBy { it.albumID }.map { (id, l) -> Triple(id, l.first(), l.size) }.sortedByDescending { it.third }
    }

    Column(Modifier.fillMaxSize().background(colors.background).statusBarsPadding()) {
        // 标题 + 视图切换
        Row(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 22.dp), verticalAlignment = Alignment.Bottom) {
            Text(album?.second ?: "相册", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 28.sp, letterSpacing = 0.06.em)
            Spacer(Modifier.weight(1f))
            if (album != null) {
                Text("全部", color = colors.primary, fontSize = 14.sp, letterSpacing = 0.12.em, modifier = Modifier.clickable { album = null }.padding(8.dp))
            } else {
                listOf(false to "照片", true to "文件夹").forEach { (f, label) ->
                    Text(
                        label, fontSize = 14.sp, letterSpacing = 0.12.em,
                        fontWeight = if (folders == f) FontWeight.Bold else FontWeight.Normal,
                        color = if (folders == f) colors.onBackground else colors.onSurfaceVariant,
                        modifier = Modifier.clickable { folders = f }.padding(start = 14.dp, top = 8.dp, bottom = 8.dp)
                    )
                }
            }
        }
        // 搜索框：一条细线，不用胶囊
        Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            BasicTextField(
                value = query, onValueChange = { query = it; if (it.isNotBlank()) folders = false }, singleLine = true,
                textStyle = TextStyle(fontSize = 16.sp, color = colors.onBackground),
                cursorBrush = SolidColor(colors.primary),
                decorationBox = { inner -> Box { if (query.isEmpty()) Text("搜附言、栏目、相册、日期", fontSize = 16.sp, color = colors.outline); inner() } },
                modifier = Modifier.weight(1f).padding(vertical = 10.dp)
            )
            if (query.isNotEmpty()) Text("清除", color = colors.onSurfaceVariant, fontSize = 13.sp, modifier = Modifier.clickable { query = "" }.padding(8.dp))
        }
        HorizontalDivider(Modifier.padding(horizontal = 24.dp), color = colors.outline, thickness = 0.5.dp)
        if (query.isNotBlank()) {
            Text(
                "找到 ${shown.size} 张" + if (noteHits.isNotEmpty()) " · 其中 ${shown.count { it.id in noteHits }} 张附言匹配" else "",
                style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant,
                modifier = Modifier.padding(start = 24.dp, top = 10.dp)
            )
        }

        if (folders && album == null) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 18.dp, bottom = paddingValues.calculateBottomPadding() + 120.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(albums, key = { it.first }) { (id, cover, count) ->
                    Column(Modifier.clickable { album = id to cover.albumLabel; folders = false }) {
                        Box(Modifier.fillMaxWidth().aspectRatio(1f).background(colors.surfaceVariant)) {
                            AsyncImage(
                                request = ComposableImageRequest(cover.getUri().toString()), contentDescription = cover.albumLabel,
                                contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
                            )
                        }
                        Text(cover.albumLabel, fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, maxLines = 1, modifier = Modifier.padding(top = 10.dp))
                        Text("$count", style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(cols),
                contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding() + 120.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        // 双指捏合切换密度：张开减少列数，捏合增加列数（每跨过一档就切一次）
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                            var base = 0f
                            while (true) {
                                val ev = awaitPointerEvent(PointerEventPass.Initial)
                                val pressed = ev.changes.filter { it.pressed }
                                if (pressed.isEmpty()) break
                                if (pressed.size >= 2) {
                                    val a = pressed[0].position; val b = pressed[1].position
                                    val d = hypot(a.x - b.x, a.y - b.y)
                                    if (base == 0f) base = d
                                    if (d / base > 1.25f && cols > 2) { cols--; base = d }
                                    else if (d / base < 0.8f && cols < 6) { cols++; base = d }
                                    ev.changes.forEach { it.consume() }
                                }
                            }
                        }
                    }
            ) {
                cells.forEach { cell ->
                    when (cell) {
                        is Cell.Header -> item(key = cell.key, span = { GridItemSpan(maxLineSpan) }) {
                            Row(Modifier.animateItem(placementSpec = ItemSpring).padding(start = 24.dp, end = 24.dp, top = 26.dp, bottom = 12.dp), verticalAlignment = Alignment.Bottom) {
                                Text("%02d".format(cell.month), fontFamily = DateFamily, fontSize = 30.sp, lineHeight = 30.sp)
                                Spacer(Modifier.width(10.dp))
                                Text("${cell.year}", style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant, modifier = Modifier.padding(bottom = 3.dp))
                                Spacer(Modifier.weight(1f))
                                Text("${cell.count} 张", style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant, modifier = Modifier.padding(bottom = 3.dp))
                            }
                        }
                        is Cell.Photo -> item(key = cell.media.id) {
                            val m = cell.media
                            Box(
                                Modifier.animateItem(placementSpec = ItemSpring).aspectRatio(1f).background(colors.surfaceVariant)
                                    .hwThumb(viewer, m)
                                    .clickable { viewer?.open(shown, m) }
                            ) {
                                AsyncImage(
                                    request = ComposableImageRequest(m.getUri().toString()), contentDescription = m.label,
                                    contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
                                )
                                if (m.isVideo) Text(
                                    m.duration?.toLongOrNull()?.let { "%d:%02d".format(it / 60000, (it / 1000) % 60) } ?: "视频",
                                    color = Color.White, fontSize = 10.sp,
                                    modifier = Modifier.align(Alignment.BottomStart).padding(5.dp)
                                )
                                if (m.id in noteSet) Box(
                                    Modifier.align(Alignment.BottomEnd).padding(6.dp).size(6.dp)
                                        .background(Color(0xFFB22A20), CircleShape)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
