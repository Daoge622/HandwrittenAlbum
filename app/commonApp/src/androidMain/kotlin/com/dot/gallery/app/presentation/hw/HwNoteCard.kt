/*
 * SPDX-FileCopyrightText: 2026 手写相册 contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.dot.gallery.app.presentation.hw

import android.app.Activity
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.dot.gallery.data.entity.PhotoNoteEntity
import com.dot.gallery.domain.model.UriMedia
import com.dot.gallery.domain.model.definedTimestamp
import com.dot.gallery.domain.util.getUri
import com.dot.gallery.injection.ComponentOwner
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

/*
 * 光注信息卡：像照片的“背面”。不盖在照片上，从下方升起；
 * 一段自由文字 + 用户自己命名的栏目；自动保存到手写相册；“写入照片”由用户手动决定。
 */

private val CN_WEEK = arrayOf("星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日")
private val DateFamily = FontFamily(androidx.compose.ui.text.font.Font(com.dot.gallery.R.font.hw_cormorant_medium))

/** 照片详情（只读）：文件名、分辨率、大小、相机、镜头参数。 */
private suspend fun loadDetails(context: android.content.Context, m: UriMedia): List<Pair<String, String>> =
    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val out = ArrayList<Pair<String, String>>()
        out += "文件" to m.label
        val mb = m.size / 1024.0 / 1024.0
        runCatching {
            context.contentResolver.openInputStream(m.getUri())?.use { input ->
                val e = androidx.exifinterface.media.ExifInterface(input)
                val w = e.getAttributeInt(androidx.exifinterface.media.ExifInterface.TAG_IMAGE_WIDTH, 0)
                val h = e.getAttributeInt(androidx.exifinterface.media.ExifInterface.TAG_IMAGE_LENGTH, 0)
                out += "尺寸" to (if (w > 0 && h > 0) "$w × $h · " else "") + "%.1f MB".format(mb)
                val make = e.getAttribute(androidx.exifinterface.media.ExifInterface.TAG_MAKE).orEmpty().trim()
                val model = e.getAttribute(androidx.exifinterface.media.ExifInterface.TAG_MODEL).orEmpty().trim()
                val camera = if (model.startsWith(make, true)) model else "$make $model".trim()
                if (camera.isNotEmpty()) out += "相机" to camera
                val f = e.getAttributeDouble(androidx.exifinterface.media.ExifInterface.TAG_F_NUMBER, 0.0)
                val t = e.getAttributeDouble(androidx.exifinterface.media.ExifInterface.TAG_EXPOSURE_TIME, 0.0)
                val iso = e.getAttributeInt(androidx.exifinterface.media.ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY, 0)
                val fl = e.getAttributeDouble(androidx.exifinterface.media.ExifInterface.TAG_FOCAL_LENGTH_IN_35MM_FILM, 0.0)
                    .takeIf { it > 0 } ?: e.getAttributeDouble(androidx.exifinterface.media.ExifInterface.TAG_FOCAL_LENGTH, 0.0)
                val parts = buildList {
                    if (fl > 0) add("%.0fmm".format(fl))
                    if (f > 0) add("ƒ/%.1f".format(f))
                    if (t > 0) add(if (t >= 1) "%.1fs".format(t) else "1/%.0fs".format(1 / t))
                    if (iso > 0) add("ISO $iso")
                }
                if (parts.isNotEmpty()) out += "参数" to parts.joinToString("  ")
                e.latLong?.let { out += "坐标" to "%.5f, %.5f".format(it[0], it[1]) }
            }
        }.onFailure { out += "大小" to "%.1f MB".format(mb) }
        out += "位置" to m.relativePath.trimEnd('/')
        out
    }

@Composable
fun rememberNotesRepository(): HwNotesRepository {
    val context = LocalContext.current
    return remember { HwNotesRepository((context.applicationContext as ComponentOwner).component.database) }
}

@OptIn(FlowPreview::class)
@Composable
fun HwNoteCard(media: UriMedia, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val repo = rememberNotesRepository()
    val scope = rememberCoroutineScope()
    val colors = MaterialTheme.colorScheme
    val note by remember(media.id) { repo.noteFlow(media.id) }.collectAsState(initial = null)
    val latestNote by androidx.compose.runtime.rememberUpdatedState(note)

    var text by remember(media.id) { mutableStateOf("") }
    val fields = remember(media.id) { mutableStateListOf<NoteField>() }
    var loaded by remember(media.id) { mutableStateOf(false) }
    var writing by remember { mutableStateOf(false) }
    var message by remember(media.id) { mutableStateOf<String?>(null) }

    // 首次拿到数据库里的附言时填入编辑框
    LaunchedEffect(media.id, note) {
        if (!loaded && note != null) {
            text = note!!.text
            fields.clear(); fields.addAll(parseFields(note!!.fieldsJson))
            loaded = true
        }
    }
    LaunchedEffect(media.id) { kotlinx.coroutines.delay(350); loaded = true }

    // 自动保存（停笔 0.5 秒后）
    LaunchedEffect(media.id) {
        snapshotFlow { text to fields.toList() }
            .drop(1)
            .debounce(500)
            .collect { (t, f) -> if (loaded) repo.save(media, latestNote, t, f) }
    }

    fun doWrite(current: PhotoNoteEntity) {
        writing = true; message = null
        scope.launch {
            repo.save(media, note, text, fields.toList())
            when (val r = HwXmpWriter.write(context, media, current.copy(text = text, fieldsJson = fields.toList().toJson()), repo.tagsOf(media.id).first().map { it.name })) {
                WriteResult.Ok -> { repo.markWritten(current.id); message = null }
                WriteResult.Unsupported -> message = "这种格式暂不支持写入照片，附言已保存在手写相册"
                is WriteResult.Failed -> message = r.reason
                is WriteResult.NeedsPermission -> message = "需要你的授权才能修改这张照片"
            }
            writing = false
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { res ->
        if (res.resultCode == Activity.RESULT_OK) note?.let { doWrite(it) }
    }

    val date = Instant.ofEpochSecond(media.definedTimestamp).atZone(ZoneId.systemDefault())
    Column(
        modifier
            .fillMaxWidth()
            .background(colors.background, RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
            .navigationBarsPadding()
            .imePadding()
    ) {
        Box(Modifier.fillMaxWidth().padding(top = 10.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.width(36.dp).height(4.dp).background(colors.outline, CircleShape))
        }
        Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 26.dp)) {
            // 日期页眉
            Row(Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 14.dp), verticalAlignment = Alignment.Bottom) {
                Text("${date.monthValue}.${"%02d".format(date.dayOfMonth)}", fontFamily = DateFamily, fontSize = 38.sp, lineHeight = 38.sp, color = colors.onBackground)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.padding(bottom = 4.dp)) {
                    Text("${date.year}", style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                    Text(CN_WEEK[date.dayOfWeek.value - 1] + " · " + "%02d:%02d".format(date.hour, date.minute), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                }
                Spacer(Modifier.weight(1f))
                Text(media.albumLabel, style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
            }
            HorizontalDivider(color = colors.outline, thickness = 0.5.dp)

            // 照片详情（只读，可收起）
            var details by remember(media.id) { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
            var showDetails by remember { mutableStateOf(false) }
            LaunchedEffect(media.id) { details = loadDetails(context, media) }
            Row(
                Modifier.fillMaxWidth().clickable { showDetails = !showDetails }.padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("照片信息", style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                Spacer(Modifier.weight(1f))
                Text(
                    details.firstOrNull { it.first == "相机" }?.second ?: details.firstOrNull { it.first == "尺寸" }?.second.orEmpty(),
                    style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant, maxLines = 1
                )
                Text(if (showDetails) "  收起" else "  展开", style = MaterialTheme.typography.labelSmall, color = colors.primary)
            }
            if (showDetails) {
                details.forEach { (k, v) ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                        Text(k, fontSize = 12.sp, letterSpacing = 0.1.em, color = colors.onSurfaceVariant, modifier = Modifier.width(56.dp))
                        Text(v, fontSize = 13.sp, color = colors.onBackground)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
            HorizontalDivider(color = colors.outlineVariant, thickness = 0.5.dp)

            // 一段话
            BasicTextField(
                value = text, onValueChange = { text = it },
                textStyle = TextStyle(fontFamily = FontFamily.Serif, fontSize = 18.sp, lineHeight = 31.sp, color = colors.onBackground),
                cursorBrush = SolidColor(colors.primary),
                decorationBox = { inner ->
                    Box {
                        if (text.isEmpty()) Text("写下这张照片的故事……", fontFamily = FontFamily.Serif, fontSize = 18.sp, lineHeight = 31.sp, color = colors.outline)
                        inner()
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp)
            )

            // 手写
            HorizontalDivider(color = colors.outlineVariant, thickness = 0.5.dp)
            HwNoteInk(media, repo, note)

            // 标签
            HorizontalDivider(color = colors.outlineVariant, thickness = 0.5.dp)
            HwTagRow(media, repo)

            // 自定义栏目
            fields.forEachIndexed { i, f ->
                HorizontalDivider(color = colors.outlineVariant, thickness = 0.5.dp)
                Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    BasicTextField(
                        value = f.key, onValueChange = { fields[i] = f.copy(key = it) }, singleLine = true,
                        textStyle = TextStyle(fontSize = 13.sp, letterSpacing = 0.12.em, color = colors.onSurfaceVariant),
                        cursorBrush = SolidColor(colors.primary),
                        decorationBox = { inner -> Box { if (f.key.isEmpty()) Text(if (f.value.isNotBlank()) "填栏目名" else "栏目", fontSize = 13.sp, color = if (f.value.isNotBlank()) colors.primary else colors.outline); inner() } },
                        modifier = Modifier.width(84.dp)
                    )
                    BasicTextField(
                        value = f.value, onValueChange = { fields[i] = f.copy(value = it) },
                        textStyle = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, color = colors.onBackground),
                        cursorBrush = SolidColor(colors.primary),
                        decorationBox = { inner -> Box { if (f.value.isEmpty()) Text("内容", fontSize = 16.sp, color = colors.outline); inner() } },
                        modifier = Modifier.weight(1f)
                    )
                    Text("×", color = colors.onSurfaceVariant, fontSize = 18.sp, modifier = Modifier.clickable { fields.removeAt(i) }.padding(start = 12.dp, end = 2.dp))
                }
            }
            HorizontalDivider(color = colors.outlineVariant, thickness = 0.5.dp)
            Text(
                "＋ 添加一栏", color = colors.primary, fontSize = 14.sp, letterSpacing = 0.1.em,
                modifier = Modifier.clickable { fields.add(NoteField("", "")) }.padding(vertical = 14.dp)
            )

            // 写入照片：由用户决定
            HorizontalDivider(color = colors.outline, thickness = 0.5.dp)
            val n = note
            val supported = HwXmpWriter.supports(media.mimeType)
            val status = when {
                message != null -> message!!
                n == null -> "写下的内容只保存在手写相册"
                n.writtenToFile && n.writtenAt != null -> Instant.ofEpochMilli(n.writtenAt!!).atZone(ZoneId.systemDefault()).let {
                    "已写入照片 · ${it.monthValue}.${"%02d".format(it.dayOfMonth)} ${"%02d:%02d".format(it.hour, it.minute)}"
                }
                n.writtenAt != null -> "有改动尚未写入照片"
                !supported -> "这种格式暂不支持写入照片"
                else -> "只保存在手写相册，未写入照片"
            }
            Row(Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 22.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(6.dp).background(if (n?.writtenToFile == true) colors.secondary else colors.outline, CircleShape))
                Spacer(Modifier.width(8.dp))
                Text(status, style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant, modifier = Modifier.weight(1f))
                if (n != null && supported && !n.writtenToFile) {
                    Text(
                        if (writing) "写入中…" else "写入照片",
                        color = colors.primary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, letterSpacing = 0.12.em,
                        modifier = Modifier
                            .border(0.8.dp, colors.primary, RoundedCornerShape(50))
                            .clickable(enabled = !writing) {
                                scope.launch {
                                    repo.save(media, n, text, fields.toList())
                                    val r = HwXmpWriter.write(context, media, n.copy(text = text, fieldsJson = fields.toList().toJson()), repo.tagsOf(media.id).first().map { it.name })
                                    if (r is WriteResult.NeedsPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                        val pi = MediaStore.createWriteRequest(context.contentResolver, listOf(media.getUri()))
                                        permissionLauncher.launch(IntentSenderRequest.Builder(pi.intentSender).build())
                                    } else if (r == WriteResult.Ok) repo.markWritten(n.id)
                                    else message = when (r) {
                                        WriteResult.Unsupported -> "这种格式暂不支持写入照片"
                                        is WriteResult.Failed -> r.reason
                                        else -> null
                                    }
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}


/** 附言卡里的手写：没有时是一行“＋ 手写”；有了就显示笔迹，点一下重写一遍，“改”进写字板。 */
@Composable
private fun HwNoteInk(media: UriMedia, repo: HwNotesRepository, note: PhotoNoteEntity?) {
    val colors = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    var strokes by remember(media.id) { mutableStateOf<List<com.dot.gallery.data.InkStroke>>(emptyList()) }
    var colorCode by remember(media.id) { mutableStateOf(0) }
    var editing by remember { mutableStateOf(false) }
    var replay by remember(media.id) { mutableStateOf(0) }
    LaunchedEffect(media.id, note?.inkId, note?.updatedAt) {
        val ink = repo.inkOf(note)
        strokes = ink?.let { runCatching { com.dot.gallery.data.InkCodec.decode(it.strokes) }.getOrNull() }.orEmpty()
        colorCode = ink?.color ?: 0
    }
    if (strokes.isEmpty()) {
        Text(
            "＋ 手写", color = colors.primary, fontSize = 14.sp, letterSpacing = 0.1.em,
            modifier = Modifier.clickable { editing = true }.padding(vertical = 14.dp)
        )
    } else {
        Column(Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
            HwInkView(strokes, colorCode, Modifier.fillMaxWidth().clickable { replay++ }, replay)
            Text(
                "改", color = colors.onSurfaceVariant, fontSize = 12.sp, letterSpacing = 0.12.em,
                modifier = Modifier.align(Alignment.End).clickable { editing = true }.padding(start = 12.dp, top = 6.dp, bottom = 2.dp)
            )
        }
    }
    if (editing) androidx.compose.ui.window.Dialog(
        onDismissRequest = { editing = false },
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        HwInkPad(strokes, colorCode, onCancel = { editing = false }) { s, c ->
            editing = false
            strokes = s; colorCode = c
            scope.launch { repo.saveInk(media, note, s, c) }
        }
    }
}
