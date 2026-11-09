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
 * 手写相册「随笔」：时间线列表 + 编辑器（第一版：标题 + 正文段落）。
 * 数据存在 journal_entry / journal_block（见 HandwrittenEntities.kt）。
 * 图片块、手写块、天气心情卡片在后续轮次加入。
 */

// 段落块的 payload：{"text": "..."}；用 Android 自带的 org.json，避免额外依赖
private fun paragraphJson(text: String): String = org.json.JSONObject().put("text", text).toString()
private fun paragraphText(payload: String): String? = runCatching { org.json.JSONObject(payload).optString("text") }.getOrNull()

class JournalViewModel(private val dao: JournalDao) : ViewModel() {
    val entries = dao.all().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** 每篇随笔的第一段，用作列表摘要。 */
    suspend fun excerpt(id: String): String =
        dao.blocks(id).firstOrNull { it.type == JournalBlockType.PARAGRAPH }
            ?.let { paragraphText(it.payloadJson) }.orEmpty()

    suspend fun allParagraphs(id: String): List<String> =
        dao.blocks(id).filter { it.type == JournalBlockType.PARAGRAPH }.mapNotNull { paragraphText(it.payloadJson) }

    suspend fun load(id: String): Pair<JournalEntryEntity?, String> {
        val e = dao.entry(id) ?: return null to ""
        return e to excerpt(id)
    }

    fun save(id: String, createdAt: Long?, title: String, body: String) {
        if (title.isBlank() && body.isBlank()) return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val entry = JournalEntryEntity(id = id, title = title.trim(), createdAt = createdAt ?: now, updatedAt = now)
            val blocks = body.split(Regex("\n{2,}")).map { it.trim() }.filter { it.isNotEmpty() }.mapIndexed { i, p ->
                JournalBlockEntity(
                    id = "$id-$i", entryId = id, position = i, type = JournalBlockType.PARAGRAPH,
                    payloadJson = paragraphJson(p)
                )
            }
            dao.save(entry, blocks)
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
                        LaunchedEffect(e.id, e.updatedAt) { excerpt = vm.excerpt(e.id) }
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

@Composable
fun JournalEditScreen(entryId: String, vm: JournalViewModel) {
    val eventHandler = LocalEventHandler.current
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var createdAt by remember { mutableStateOf<Long?>(null) }
    var loaded by remember { mutableStateOf(false) }
    val bodyFocus = remember { FocusRequester() }

    LaunchedEffect(entryId) {
        val (e, _) = vm.load(entryId)
        if (e != null) {
            title = e.title
            createdAt = e.createdAt
            // 读出全部段落，用空行连接
            body = vm.allText(entryId)
        }
        loaded = true
        if (e == null) runCatching { bodyFocus.requestFocus() }
    }

    fun saveAndExit() {
        vm.save(entryId, createdAt, title, body)
        eventHandler.navigateUp()
    }
    BackHandler { saveAndExit() }

    val now = remember { createdAt ?: System.currentTimeMillis() }
    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding().navigationBarsPadding().imePadding()
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("完成", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, letterSpacing = 0.15.em,
                modifier = Modifier.clickable { saveAndExit() }.padding(10.dp))
            Spacer(Modifier.weight(1f))
            Text(
                Instant.ofEpochMilli(createdAt ?: now).atZone(zone).toLocalDate().let { "${it.year} · ${it.monthValue} 月 ${it.dayOfMonth} 日" },
                style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 10.dp)
            )
        }
        Column(Modifier.padding(horizontal = 26.dp)) {
            BasicTextField(
                value = title, onValueChange = { title = it }, singleLine = true,
                textStyle = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 26.sp, color = MaterialTheme.colorScheme.onBackground),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { inner ->
                    Box { if (title.isEmpty()) Text("标题", fontFamily = FontFamily.Serif, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline); inner() }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 16.dp)
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
            BasicTextField(
                value = body, onValueChange = { body = it },
                textStyle = TextStyle(fontSize = 17.sp, lineHeight = 30.sp, color = MaterialTheme.colorScheme.onBackground),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { inner ->
                    Box { if (body.isEmpty() && loaded) Text("写点什么……", fontSize = 17.sp, lineHeight = 30.sp, color = MaterialTheme.colorScheme.outline); inner() }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 18.dp).focusRequester(bodyFocus)
            )
        }
    }
}

/** 编辑器需要的全文：所有段落用空行连接。 */
suspend fun JournalViewModel.allText(id: String): String = allParagraphs(id).joinToString("\n\n")
