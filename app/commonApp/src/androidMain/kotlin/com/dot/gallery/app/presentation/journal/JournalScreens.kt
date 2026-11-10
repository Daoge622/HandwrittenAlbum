/*
 * SPDX-FileCopyrightText: 2026 手写相册 contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.dot.gallery.app.presentation.journal

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dot.gallery.core.LocalEventHandler
import com.dot.gallery.core.navigate
import com.dot.gallery.core.navigateUp
import com.dot.gallery.core.presentation.util.Screen
import com.dot.gallery.data.dao.JournalDao
import com.dot.gallery.data.entity.JournalBlockEntity
import com.dot.gallery.data.entity.JournalBlockType
import com.dot.gallery.data.entity.JournalEntryEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.util.UUID

/*
 * 手写相册「随笔」：时间线列表 + 编辑器（文字块 + 照片块）。
 * 数据存在 journal_entry / journal_block（见 HandwrittenEntities.kt）。
 */

// 段落块的 payload：{"text": "..."}；用 Android 自带的 org.json，避免额外依赖
private fun paragraphJson(text: String): String = org.json.JSONObject().put("text", text).toString()
private fun paragraphText(payload: String): String? = runCatching { org.json.JSONObject(payload).optString("text") }.getOrNull()

/** 随笔里的一张照片：MediaStore id + uri + 原始宽高（用于按比例排版）。 */
data class JournalPhoto(val id: Long, val uri: String, val w: Int, val h: Int)

sealed class EditorBlock {
    abstract val key: String
    data class Text(override val key: String = UUID.randomUUID().toString(), val text: String = "") : EditorBlock()
    data class Photos(override val key: String = UUID.randomUUID().toString(), val photos: List<JournalPhoto>) : EditorBlock()
    data class Ink(override val key: String = UUID.randomUUID().toString(), val strokes: List<com.dot.gallery.data.InkStroke>, val color: Int) : EditorBlock()
}

// 手写块的 payload：{"ink": base64(InkCodec), "color": 0|1}
private fun inkJson(b: EditorBlock.Ink): String = org.json.JSONObject()
    .put("ink", android.util.Base64.encodeToString(com.dot.gallery.data.InkCodec.encode(b.strokes), android.util.Base64.NO_WRAP))
    .put("color", b.color).toString()

private fun inkOf(payload: String): EditorBlock.Ink? = runCatching {
    val o = org.json.JSONObject(payload)
    EditorBlock.Ink(strokes = com.dot.gallery.data.InkCodec.decode(android.util.Base64.decode(o.getString("ink"), android.util.Base64.NO_WRAP)), color = o.optInt("color"))
}.getOrNull()

private fun photosJson(list: List<JournalPhoto>): String {
    val arr = org.json.JSONArray()
    list.forEach { arr.put(org.json.JSONObject().put("id", it.id).put("uri", it.uri).put("w", it.w).put("h", it.h)) }
    return org.json.JSONObject().put("items", arr).toString()
}

private fun photosOf(payload: String): List<JournalPhoto> = runCatching {
    val arr = org.json.JSONObject(payload).getJSONArray("items")
    (0 until arr.length()).map { i ->
        val o = arr.getJSONObject(i)
        JournalPhoto(o.optLong("id"), o.optString("uri"), o.optInt("w"), o.optInt("h"))
    }
}.getOrDefault(emptyList())

/** 保证：以文字块开头和结尾，图片块之间总夹着文字块，相邻文字块合并。 */
fun normalize(list: List<EditorBlock>): List<EditorBlock> {
    val out = mutableListOf<EditorBlock>()
    list.forEach { b ->
        val last = out.lastOrNull()
        when {
            b is EditorBlock.Photos && b.photos.isEmpty() -> Unit
            b is EditorBlock.Text && last is EditorBlock.Text ->
                out[out.lastIndex] = last.copy(text = listOf(last.text, b.text).filter { it.isNotBlank() }.joinToString("\n\n"))
            b is EditorBlock.Ink && b.strokes.isEmpty() -> Unit
            b !is EditorBlock.Text && (last == null || last !is EditorBlock.Text) -> { out += EditorBlock.Text(); out += b }
            else -> out += b
        }
    }
    if (out.lastOrNull() !is EditorBlock.Text) out += EditorBlock.Text()
    return out
}

class JournalViewModel(private val dao: JournalDao) : ViewModel() {
    val entries = dao.all().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** 每篇随笔的第一段，用作列表摘要。 */
    suspend fun excerpt(id: String): String =
        dao.blocks(id).firstOrNull { it.type == JournalBlockType.PARAGRAPH }
            ?.let { paragraphText(it.payloadJson) }.orEmpty()

    suspend fun allParagraphs(id: String): List<String> =
        dao.blocks(id).filter { it.type == JournalBlockType.PARAGRAPH }.mapNotNull { paragraphText(it.payloadJson) }

    suspend fun entry(id: String): JournalEntryEntity? = dao.entry(id)

    /** 编辑器载入：段落合并成文字块，图片块单独成块。 */
    suspend fun loadBlocks(id: String): List<EditorBlock> {
        val out = mutableListOf<EditorBlock>()
        dao.blocks(id).forEach { b ->
            when (b.type) {
                JournalBlockType.PARAGRAPH -> {
                    val t = paragraphText(b.payloadJson).orEmpty()
                    val last = out.lastOrNull()
                    if (last is EditorBlock.Text) out[out.lastIndex] = last.copy(text = last.text + "\n\n" + t)
                    else out += EditorBlock.Text(text = t)
                }
                JournalBlockType.IMAGES -> out += EditorBlock.Photos(photos = photosOf(b.payloadJson))
                JournalBlockType.INK -> inkOf(b.payloadJson)?.let { out += it }
            }
        }
        return normalize(out)
    }

    /** 列表封面：第一组图片的第一张。 */
    suspend fun cover(id: String): JournalPhoto? =
        dao.blocks(id).firstOrNull { it.type == JournalBlockType.IMAGES }?.let { photosOf(it.payloadJson).firstOrNull() }

    fun save(id: String, createdAt: Long?, title: String, blocks: List<EditorBlock>, then: () -> Unit = {}) {
        val hasContent = title.isNotBlank() || blocks.any { (it is EditorBlock.Text && it.text.isNotBlank()) || (it is EditorBlock.Photos && it.photos.isNotEmpty()) || it is EditorBlock.Ink }
        if (!hasContent) { then(); return }
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val entry = JournalEntryEntity(id = id, title = title.trim(), createdAt = createdAt ?: now, updatedAt = now)
            val rows = mutableListOf<JournalBlockEntity>()
            blocks.forEach { b ->
                when (b) {
                    is EditorBlock.Text -> b.text.split(Regex("\n{2,}")).map { it.trim() }.filter { it.isNotEmpty() }.forEach { p ->
                        rows += JournalBlockEntity(id = "$id-${rows.size}", entryId = id, position = rows.size, type = JournalBlockType.PARAGRAPH, payloadJson = paragraphJson(p))
                    }
                    is EditorBlock.Photos -> if (b.photos.isNotEmpty())
                        rows += JournalBlockEntity(id = "$id-${rows.size}", entryId = id, position = rows.size, type = JournalBlockType.IMAGES, payloadJson = photosJson(b.photos))
                    is EditorBlock.Ink -> if (b.strokes.isNotEmpty())
                        rows += JournalBlockEntity(id = "$id-${rows.size}", entryId = id, position = rows.size, type = JournalBlockType.INK, payloadJson = inkJson(b))
                }
            }
            dao.save(entry, rows)
            then()
        }
    }

    fun delete(id: String) {
        viewModelScope.launch { dao.deleteEntry(id) }
    }
}

private val zone = ZoneId.systemDefault()
private fun dateLabel(ms: Long): String {
    val d = Instant.ofEpochMilli(ms).atZone(zone).toLocalDate()
    return "${d.monthValue}.${"%02d".format(d.dayOfMonth)}"
}
private fun yearOf(ms: Long) = Instant.ofEpochMilli(ms).atZone(zone).year

@Composable
fun JournalScreen(paddingValues: PaddingValues, vm: JournalViewModel) {
    val eventHandler = LocalEventHandler.current
    val entries by vm.entries.collectAsState()

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 22.dp, bottom = 6.dp), verticalAlignment = Alignment.Bottom) {
            Text("随笔", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 28.sp, letterSpacing = 0.08.em)
            Spacer(Modifier.weight(1f))
            Text(
                "写一篇",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.15.em,
                fontSize = 14.sp,
                modifier = Modifier
                    .clickable { eventHandler.navigate(Screen.JournalEditScreen.id(UUID.randomUUID().toString())) }
                    .padding(8.dp)
            )
        }
        Text(
            if (entries.isEmpty()) "还没有随笔" else "共 ${entries.size} 篇",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(Modifier.height(14.dp))
        HorizontalDivider(Modifier.padding(horizontal = 24.dp), color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)

        if (entries.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(bottom = 160.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("今天的故事，从一笔开始。", fontFamily = FontFamily.Serif, fontSize = 18.sp)
                    Spacer(Modifier.height(18.dp))
                    Text(
                        "写一篇",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.2.em,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.extraLarge)
                            .clickable { eventHandler.navigate(Screen.JournalEditScreen.id(UUID.randomUUID().toString())) }
                            .padding(horizontal = 26.dp, vertical = 12.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = paddingValues.calculateBottomPadding() + 120.dp)
            ) {
                items(entries, key = { it.id }) { e ->
                    val y = yearOf(e.createdAt)
                    Column(Modifier.fillMaxWidth().clickable { eventHandler.navigate(Screen.JournalEditScreen.id(e.id)) }) {
                        Spacer(Modifier.height(22.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                e.title.ifBlank { "无题" },
                                fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 19.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                if (y != java.time.LocalDate.now().year) "$y.${dateLabel(e.createdAt)}" else dateLabel(e.createdAt),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        var excerpt by remember(e.id, e.updatedAt) { mutableStateOf("") }
                        var cover by remember(e.id, e.updatedAt) { mutableStateOf<JournalPhoto?>(null) }
                        LaunchedEffect(e.id, e.updatedAt) { excerpt = vm.excerpt(e.id); cover = vm.cover(e.id) }
                        cover?.let { c ->
                            com.github.panpf.sketch.AsyncImage(
                                request = com.github.panpf.sketch.request.ComposableImageRequest(c.uri), contentDescription = null,
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier.padding(top = 12.dp).fillMaxWidth().aspectRatio(16f / 9f)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            )
                        }
                        if (excerpt.isNotEmpty()) {
                            Text(
                                excerpt, maxLines = 3, style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        Spacer(Modifier.height(22.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

