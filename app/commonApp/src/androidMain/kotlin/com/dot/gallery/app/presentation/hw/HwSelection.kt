/*
 * SPDX-FileCopyrightText: 2026 手写相册 contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.dot.gallery.app.presentation.hw

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.dot.gallery.core.LocalMediaHandler
import com.dot.gallery.core.ui.util.rememberActivityResult
import com.dot.gallery.core.ui.util.writeRequest
import com.dot.gallery.data.repository.MediaMutationResult
import com.dot.gallery.domain.model.UriMedia
import com.dot.gallery.domain.util.getUri
import kotlinx.coroutines.launch

/*
 * 多选后的底部操作条：分享 · 标签 · 收藏 · 删除。
 * 删除只进系统回收站（需要时系统弹窗确认），确认后清空选择。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HwSelectionBar(
    picked: List<UriMedia>,
    repo: HwNotesRepository,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    albums: List<Pair<String, String>> = emptyList(),
) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    val handler = LocalMediaHandler.current
    var tagging by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }
    val allTags by remember { repo.allTags() }.collectAsState(initial = emptyList())
    val favoriteRequest = rememberActivityResult(onResultOk = { onDone() })
    val trashRequest = rememberActivityResult(onResultOk = { onDone() })

    // ---- 移到 / 复制到相册 ----
    var moving by remember { mutableStateOf(false) }
    var copyMode by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var newAlbum by remember { mutableStateOf("") }
    var pendingDir by remember { mutableStateOf<String?>(null) }

    fun doMove(dir: String) {
        busy = true
        scope.launch {
            val target = dir.trimEnd('/')
            val todo = picked.filter { it.path.substringBeforeLast('/') != target }
            var failed = 0
            todo.forEach { m ->
                val ok = runCatching { handler.moveMedia(m, target) }.getOrDefault(false)
                if (ok) android.media.MediaScannerConnection.scanFile(context, arrayOf(target + "/" + m.label), arrayOf(m.mimeType), null)
                else failed++
            }
            context.contentResolver.notifyChange(android.provider.MediaStore.Files.getContentUri("external"), null)
            busy = false; moving = false
            if (failed > 0) android.widget.Toast.makeText(context, "有 $failed 张没能移动", android.widget.Toast.LENGTH_SHORT).show()
            onDone()
        }
    }
    val moveRequest = rememberActivityResult(onResultCanceled = { pendingDir = null }, onResultOk = { pendingDir?.let { doMove(it) }; pendingDir = null })

    fun sendTo(dir: String) {
        if (busy || picked.isEmpty()) return
        if (copyMode) {
            scope.launch {
                picked.forEach { handler.copyMedia(it, dir) }
                moving = false; onDone()
            }
        } else {
            val req = picked.writeRequest(context.contentResolver)
            if (req == null) doMove(dir) else { pendingDir = dir; moveRequest.launch(req) }
        }
    }

    fun addTag(name: String) {
        if (name.isBlank()) return
        scope.launch {
            picked.forEach { repo.addTag(it, name) }
            input = ""; tagging = false; onDone()
        }
    }

    Column(
        modifier
            .fillMaxWidth()
            .background(colors.background)
            .navigationBarsPadding()
            .imePadding()
    ) {
        HorizontalDivider(color = colors.outline, thickness = 0.5.dp)
        if (moving) {
            Column(Modifier.padding(horizontal = 24.dp, vertical = 14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    listOf(false to "移动", true to "复制").forEach { (c, label) ->
                        Text(
                            label, fontFamily = FontFamily.Serif, fontSize = 15.sp, letterSpacing = 0.12.em,
                            color = if (copyMode == c) colors.onBackground else colors.outline,
                            modifier = Modifier.clickable { copyMode = c }.padding(end = 18.dp, top = 6.dp, bottom = 6.dp)
                        )
                    }
                    Text("${picked.size} 张", fontSize = 12.sp, letterSpacing = 0.12.em, color = colors.onSurfaceVariant, modifier = Modifier.weight(1f))
                    Text(if (busy) "处理中…" else "取消", color = colors.onSurfaceVariant, fontSize = 14.sp,
                        modifier = Modifier.clickable(enabled = !busy) { moving = false }.padding(8.dp))
                }
                Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    BasicTextField(
                        value = newAlbum, onValueChange = { v -> newAlbum = v.filterNot { it in "/\\:*?\"<>|" }.take(40) }, singleLine = true,
                        textStyle = TextStyle(fontFamily = FontFamily.Serif, fontSize = 16.sp, color = colors.onBackground),
                        cursorBrush = SolidColor(colors.primary),
                        decorationBox = { inner -> Box { if (newAlbum.isEmpty()) Text("新相册的名字", fontSize = 16.sp, color = colors.outline); inner() } },
                        modifier = Modifier.weight(1f)
                    )
                    if (newAlbum.isNotBlank()) Text(
                        "新建", color = colors.primary, fontSize = 14.sp,
                        modifier = Modifier.clickable {
                            sendTo(android.os.Environment.getExternalStorageDirectory().absolutePath + "/Pictures/" + newAlbum.trim())
                        }.padding(8.dp)
                    )
                }
                HorizontalDivider(color = colors.outlineVariant, thickness = 0.5.dp)
                androidx.compose.foundation.lazy.LazyColumn(Modifier.fillMaxWidth().heightIn(max = 260.dp)) {
                    items(albums.size) { i ->
                        val (label, dir) = albums[i]
                        Text(
                            label, fontFamily = FontFamily.Serif, fontSize = 16.sp, color = colors.onBackground,
                            modifier = Modifier.fillMaxWidth().clickable { sendTo(dir) }.padding(vertical = 13.dp)
                        )
                    }
                }
            }
        } else if (tagging) {
            Column(Modifier.padding(horizontal = 24.dp, vertical = 14.dp)) {
                Text("给 ${picked.size} 张照片加标签", fontSize = 12.sp, letterSpacing = 0.12.em, color = colors.onSurfaceVariant)
                BasicTextField(
                    value = input, onValueChange = { input = it }, singleLine = true,
                    textStyle = TextStyle(fontFamily = FontFamily.Serif, fontSize = 16.sp, color = colors.onBackground),
                    cursorBrush = SolidColor(colors.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { addTag(input) }),
                    decorationBox = { inner -> Box { if (input.isEmpty()) Text("输入新标签，或在下面挑一个", fontSize = 16.sp, color = colors.outline); inner() } },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                )
                val list = allTags.filter { input.isBlank() || it.name.contains(input.trim(), true) }.take(16)
                if (list.isNotEmpty()) FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    list.forEach { t -> TagChip(t, onClick = { addTag(t.name) }) }
                }
                Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.End) {
                    Text("取消", color = colors.onSurfaceVariant, fontSize = 14.sp, modifier = Modifier.clickable { tagging = false }.padding(8.dp))
                    if (input.isNotBlank()) Text(
                        "添加", color = colors.primary, fontSize = 14.sp,
                        modifier = Modifier.clickable { addTag(input) }.padding(8.dp)
                    )
                }
            }
        } else {
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                BarAction("分享", picked.isNotEmpty()) {
                    val uris = ArrayList<Uri>(picked.map { it.getUri() })
                    val i = if (uris.size == 1) Intent(Intent.ACTION_SEND).apply {
                        type = picked.first().mimeType; putExtra(Intent.EXTRA_STREAM, uris.first())
                    } else Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                        type = if (picked.all { it.mimeType.startsWith("image/") }) "image/*" else "*/*"
                        putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                    }
                    i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    context.startActivity(Intent.createChooser(i, null))
                }
                BarAction("标签", picked.isNotEmpty()) { tagging = true }
                BarAction("移动", picked.isNotEmpty()) { moving = true }
                BarAction("收藏", picked.isNotEmpty()) {
                    scope.launch { handler.toggleFavorite(favoriteRequest, picked, true); onDone() }
                }
                BarAction("删除", picked.isNotEmpty(), danger = true) {
                    scope.launch {
                        if (handler.trashMedia(trashRequest, picked, true) == MediaMutationResult.COMPLETED) onDone()
                    }
                }
            }
        }
    }
}

@Composable
private fun BarAction(label: String, enabled: Boolean, danger: Boolean = false, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Text(
        label,
        color = when { !enabled -> colors.outline; danger -> colors.primary; else -> colors.onBackground },
        fontFamily = FontFamily.Serif, fontSize = 15.sp, letterSpacing = 0.15.em,
        modifier = Modifier.clickable(enabled = enabled, onClick = onClick).padding(horizontal = 11.dp, vertical = 14.dp)
    )
}

/** 多选时右上角的勾选圈。 */
@Composable
fun SelectMark(selected: Boolean, modifier: Modifier = Modifier) {
    val accent = androidx.compose.ui.graphics.Color(0xFFB22A20)
    Box(
        modifier
            .border(1.4.dp, androidx.compose.ui.graphics.Color.White, androidx.compose.foundation.shape.CircleShape)
            .background(if (selected) accent else androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.18f), androidx.compose.foundation.shape.CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (selected) Text("✓", color = androidx.compose.ui.graphics.Color.White, fontSize = 11.sp)
    }
}
