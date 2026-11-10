/*
 * SPDX-FileCopyrightText: 2026 手写相册 contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.dot.gallery.app.presentation.journal

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.dot.gallery.app.presentation.hw.LocalHwViewer
import com.dot.gallery.app.presentation.hw.hwThumb
import com.dot.gallery.core.LocalEventHandler
import com.dot.gallery.core.navigateUp
import com.dot.gallery.domain.model.MediaState
import com.dot.gallery.domain.model.UriMedia
import com.dot.gallery.domain.util.getUri
import com.github.panpf.sketch.AsyncImage
import com.github.panpf.sketch.request.ComposableImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId

/*
 * 随笔编辑器：文字块与照片块交替排列。
 * - 底部“照片”从相册里挑，插在光标所在段落之后；
 * - 点照片用查看器打开，长按照片出现“移除”。
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun JournalEditScreen(entryId: String, vm: JournalViewModel, library: State<MediaState<UriMedia>>) {
    val eventHandler = LocalEventHandler.current
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    var title by remember { mutableStateOf("") }
    val blocks = remember { mutableStateListOf<EditorBlock>(EditorBlock.Text()) }
    var createdAt by remember { mutableStateOf<Long?>(null) }
    var weather by remember { mutableStateOf("") }
    var mood by remember { mutableStateOf("") }
    var place by remember { mutableStateOf("") }
    var loaded by remember { mutableStateOf(false) }
    var focused by remember { mutableStateOf(0) }
    var picking by remember { mutableStateOf(false) }
    var inking by remember { mutableStateOf(false) }
    var existing by remember { mutableStateOf(false) }
    var deleteArmed by remember { mutableStateOf(false) }
    var deleted by remember { mutableStateOf(false) }
    var armed by remember { mutableStateOf<Pair<String, Long>?>(null) }
    val firstFocus = remember { FocusRequester() }

    LaunchedEffect(entryId) {
        val e = vm.entry(entryId)
        if (e != null) {
            existing = true
            title = e.title
            createdAt = e.createdAt
            weather = e.weather.orEmpty(); mood = e.mood.orEmpty(); place = e.placeName.orEmpty()
            blocks.clear(); blocks.addAll(vm.loadBlocks(entryId))
        }
        loaded = true
        if (e == null) runCatching { firstFocus.requestFocus() }
    }

    fun snapshot() = blocks.toList()
    fun saveAndExit() { if (!deleted) vm.save(entryId, createdAt, title, snapshot(), weather, mood, place); eventHandler.navigateUp() }
    BackHandler(enabled = !picking && !inking) { saveAndExit() }
    // 切到后台也存一份，免得被系统回收后丢字
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) { if (loaded && !deleted) vm.save(entryId, createdAt, title, snapshot(), weather, mood, place) }

    fun insertPhotos(list: List<UriMedia>) {
        if (list.isEmpty()) return
        scope.launch {
            val photos = withContext(Dispatchers.IO) {
                list.map { m -> val (w, h) = sizeOf(context, m.getUri()); JournalPhoto(m.id, m.getUri().toString(), w, h) }
            }
            val at = focused.coerceIn(0, blocks.lastIndex)
            val next = blocks.toMutableList().apply { add(at + 1, EditorBlock.Photos(photos = photos)) }
            blocks.clear(); blocks.addAll(normalize(next))
        }
    }

    fun insertInk(strokes: List<com.dot.gallery.data.InkStroke>, color: Int) {
        if (strokes.isEmpty()) return
        val at = focused.coerceIn(0, blocks.lastIndex)
        val next = blocks.toMutableList().apply { add(at + 1, EditorBlock.Ink(strokes = strokes, color = color)) }
        blocks.clear(); blocks.addAll(normalize(next))
    }

    fun removeBlock(key: String) {
        val next = blocks.filterNot { it.key == key }
        blocks.clear(); blocks.addAll(normalize(next))
        armed = null
    }

    // 从相册带过来的照片：新随笔打开后直接放进去
    LaunchedEffect(loaded) {
        if (loaded && !existing) {
            val draft = JournalDraft.photos
            JournalDraft.photos = emptyList()
            insertPhotos(draft.filter { it.mimeType.startsWith("image/") }.take(9))
        }
    }
    LaunchedEffect(deleteArmed) { if (deleteArmed) { kotlinx.coroutines.delay(3000); deleteArmed = false } }

    fun removePhoto(blockKey: String, id: Long) {
        val next = blocks.map { b -> if (b is EditorBlock.Photos && b.key == blockKey) b.copy(photos = b.photos.filterNot { it.id == id }) else b }
        blocks.clear(); blocks.addAll(normalize(next))
        armed = null
    }

    val openedAt = remember { System.currentTimeMillis() }
    val day = Instant.ofEpochMilli(createdAt ?: openedAt).atZone(ZoneId.systemDefault()).toLocalDate()

    Box(Modifier.fillMaxSize().background(colors.background)) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("完成", color = colors.primary, fontWeight = FontWeight.SemiBold, letterSpacing = 0.15.em,
                    modifier = Modifier.clickable { saveAndExit() }.padding(10.dp))
                Spacer(Modifier.weight(1f))
                if (existing) Text(
                    if (deleteArmed) "再点一次删除" else "删除",
                    color = if (deleteArmed) colors.primary else colors.onSurfaceVariant, fontSize = 13.sp, letterSpacing = 0.12.em,
                    modifier = Modifier.clickable {
                        if (deleteArmed) { deleted = true; vm.delete(entryId); eventHandler.navigateUp() } else deleteArmed = true
                    }.padding(10.dp)
                )
                Text("${day.year} · ${day.monthValue} 月 ${day.dayOfMonth} 日", style = MaterialTheme.typography.labelMedium,
                    color = colors.onSurfaceVariant, modifier = Modifier.padding(end = 10.dp))
            }
            LazyColumn(Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(start = 26.dp, end = 26.dp, bottom = 40.dp)) {
                item(key = "title") {
                    Column {
                        BasicTextField(
                            value = title, onValueChange = { title = it }, singleLine = true,
                            textStyle = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 26.sp, color = colors.onBackground),
                            cursorBrush = SolidColor(colors.primary),
                            decorationBox = { inner ->
                                Box { if (title.isEmpty()) Text("标题", fontFamily = FontFamily.Serif, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = colors.outline); inner() }
                            },
                            modifier = Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 10.dp)
                        )
                        Row(Modifier.fillMaxWidth().padding(bottom = 14.dp)) {
                            MetaField(weather, "天气", Modifier.weight(1f)) { weather = it }
                            MetaField(mood, "心情", Modifier.weight(1f)) { mood = it }
                            MetaField(place, "地点", Modifier.weight(1.4f)) { place = it }
                        }
                        HorizontalDivider(color = colors.outline, thickness = 0.5.dp)
                    }
                }
                itemsIndexed(blocks, key = { _, b -> b.key }) { index, b ->
                    when (b) {
                        is EditorBlock.Text -> BasicTextField(
                            value = b.text,
                            onValueChange = { v -> val i = blocks.indexOfFirst { it.key == b.key }; if (i >= 0) blocks[i] = b.copy(text = v) },
                            textStyle = TextStyle(fontSize = 17.sp, lineHeight = 30.sp, color = colors.onBackground),
                            cursorBrush = SolidColor(colors.primary),
                            decorationBox = { inner ->
                                Box {
                                    if (b.text.isEmpty() && loaded && index == 0 && blocks.size == 1)
                                        Text("写点什么……", fontSize = 17.sp, lineHeight = 30.sp, color = colors.outline)
                                    inner()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp)
                                .onFocusChanged { if (it.isFocused) { focused = index; armed = null } }
                                .then(if (index == 0) Modifier.focusRequester(firstFocus) else Modifier)
                        )
                        is EditorBlock.Photos -> PhotoBlock(
                            block = b, library = library.value.media, armed = armed,
                            onArm = { armed = b.key to it },
                            onRemove = { removePhoto(b.key, it) },
                            onDisarm = { armed = null }
                        )
                        is EditorBlock.Ink -> {
                            val isArmed = armed?.first == b.key
                            var replay by remember(b.key) { mutableStateOf(0) }
                            Box(
                                Modifier.fillMaxWidth().padding(vertical = 6.dp).combinedClickable(
                                    onClick = { if (isArmed) removeBlock(b.key) else if (armed != null) armed = null else replay++ },
                                    onLongClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); armed = b.key to -1L }
                                )
                            ) {
                                com.dot.gallery.app.presentation.hw.HwInkView(b.strokes, Modifier.fillMaxWidth(), replay)
                                if (isArmed) Box(Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.45f)), contentAlignment = Alignment.Center) {
                                    Text("移除", color = Color.White, fontFamily = FontFamily.Serif, fontSize = 15.sp, letterSpacing = 0.2.em)
                                }
                            }
                        }
                    }
                }
            }
            HorizontalDivider(color = colors.outline, thickness = 0.5.dp)
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("＋ 照片", color = colors.primary, fontFamily = FontFamily.Serif, fontSize = 15.sp, letterSpacing = 0.12.em,
                    modifier = Modifier.clickable { picking = true }.padding(horizontal = 10.dp, vertical = 12.dp))
                Text("＋ 手写", color = colors.primary, fontFamily = FontFamily.Serif, fontSize = 15.sp, letterSpacing = 0.12.em,
                    modifier = Modifier.clickable { inking = true }.padding(horizontal = 10.dp, vertical = 12.dp))
                Spacer(Modifier.weight(1f))
                val count = blocks.sumOf { if (it is EditorBlock.Text) it.text.count { c -> !c.isWhitespace() } else 0 }
                if (count > 0) Text("$count 字", style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant, modifier = Modifier.padding(end = 10.dp))
            }
        }

        AnimatedVisibility(
            visible = picking,
            enter = slideInVertically(spring(dampingRatio = 0.9f, stiffness = 420f)) { it / 3 } + fadeIn(),
            exit = slideOutVertically(spring(dampingRatio = 0.9f, stiffness = 520f)) { it / 3 } + fadeOut(),
        ) {
            PhotoPicker(library.value.media, onCancel = { picking = false }, onPick = { picking = false; insertPhotos(it) })
        }
        if (inking) com.dot.gallery.app.presentation.hw.HwInkPad(emptyList(), 0, onCancel = { inking = false }) { st, c ->
            inking = false; insertInk(st, c)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PhotoBlock(
    block: EditorBlock.Photos,
    library: List<UriMedia>,
    armed: Pair<String, Long>?,
    onArm: (Long) -> Unit,
    onRemove: (Long) -> Unit,
    onDisarm: () -> Unit,
) {
    val viewer = LocalHwViewer.current
    val haptic = LocalHapticFeedback.current
    val photos = block.photos
    val byId = remember(library) { library.associateBy { it.id } }
    val medias = photos.mapNotNull { byId[it.id] }

    @Composable
    fun Cell(p: JournalPhoto, modifier: Modifier) {
        val m = byId[p.id]
        val isArmed = armed?.first == block.key && armed.second == p.id
        Box(
            modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                .then(if (m != null) Modifier.hwThumb(viewer, m) else Modifier)
                .combinedClickable(
                    onClick = {
                        when {
                            isArmed -> onRemove(p.id)
                            armed != null -> onDisarm()
                            m != null -> viewer?.open(medias, m)
                        }
                    },
                    onLongClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onArm(p.id) }
                )
        ) {
            AsyncImage(
                request = ComposableImageRequest(p.uri), contentDescription = null,
                contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
            )
            if (isArmed) Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)), contentAlignment = Alignment.Center) {
                Text("移除", color = Color.White, fontFamily = FontFamily.Serif, fontSize = 15.sp, letterSpacing = 0.2.em)
            }
        }
    }

    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        when (photos.size) {
            1 -> {
                val p = photos.first()
                val ratio = if (p.w > 0 && p.h > 0) (p.w.toFloat() / p.h).coerceIn(0.6f, 2.2f) else 4f / 3f
                Cell(p, Modifier.fillMaxWidth().aspectRatio(ratio))
            }
            2 -> Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                photos.forEach { Cell(it, Modifier.weight(1f).aspectRatio(0.8f)) }
            }
            else -> photos.chunked(3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    row.forEach { Cell(it, Modifier.weight(1f).aspectRatio(1f)) }
                    repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

/** 编辑器里的挑照片面板：按时间倒序，点选带序号，最多 9 张。 */
@Composable
private fun PhotoPicker(media: List<UriMedia>, onCancel: () -> Unit, onPick: (List<UriMedia>) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val picked = remember { mutableStateListOf<UriMedia>() }
    BackHandler { onCancel() }
    Column(Modifier.fillMaxSize().background(colors.background).statusBarsPadding().navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("取消", color = colors.onSurfaceVariant, modifier = Modifier.clickable(onClick = onCancel).padding(10.dp))
            Spacer(Modifier.weight(1f))
            Text("挑照片", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Spacer(Modifier.weight(1f))
            Text(
                if (picked.isEmpty()) "插入" else "插入 ${picked.size}",
                color = if (picked.isEmpty()) colors.outline else colors.primary, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(enabled = picked.isNotEmpty()) { onPick(picked.toList()) }.padding(10.dp)
            )
        }
        val images = remember(media) { media.filter { it.mimeType.startsWith("image/") } }
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(images, key = { it.id }) { m ->
                val order = picked.indexOfFirst { it.id == m.id }
                Box(Modifier.aspectRatio(1f).background(colors.surfaceVariant).clickable {
                    if (order >= 0) picked.removeAt(order) else if (picked.size < 9) picked.add(m)
                }) {
                    AsyncImage(
                        request = ComposableImageRequest(m.getUri().toString()), contentDescription = m.label,
                        contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        Modifier.align(Alignment.TopEnd).padding(6.dp).size(20.dp)
                            .background(if (order >= 0) colors.primary else Color.Black.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (order >= 0) Text("${order + 1}", color = Color.White, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

/** 读 MediaStore 的宽高（考虑旋转）；查不到就返回 0。 */
private fun sizeOf(context: Context, uri: Uri): Pair<Int, Int> = runCatching {
    context.contentResolver.query(
        uri, arrayOf(MediaStore.MediaColumns.WIDTH, MediaStore.MediaColumns.HEIGHT, MediaStore.MediaColumns.ORIENTATION), null, null, null
    )?.use { c ->
        if (!c.moveToFirst()) return@use 0 to 0
        val w = c.getInt(0); val h = c.getInt(1); val o = c.getInt(2)
        if (o == 90 || o == 270) h to w else w to h
    } ?: (0 to 0)
}.getOrDefault(0 to 0)


/** 标题下面的一小格：天气 / 心情 / 地点，想写什么写什么，不写就空着。 */
@Composable
private fun MetaField(value: String, hint: String, modifier: Modifier, onChange: (String) -> Unit) {
    val colors = MaterialTheme.colorScheme
    BasicTextField(
        value = value, onValueChange = { onChange(it.take(20)) }, singleLine = true,
        textStyle = TextStyle(fontSize = 13.sp, letterSpacing = 0.08.em, color = colors.onSurfaceVariant),
        cursorBrush = SolidColor(colors.primary),
        decorationBox = { inner -> Box { if (value.isEmpty()) Text(hint, fontSize = 13.sp, letterSpacing = 0.08.em, color = colors.outline); inner() } },
        modifier = modifier.padding(end = 10.dp)
    )
}
