/*
 * SPDX-FileCopyrightText: 2026 手写相册 contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.dot.gallery.app.presentation.hw

import android.content.Context
import android.graphics.BitmapFactory
import com.dot.gallery.data.InternalDatabase
import com.dot.gallery.data.entity.MediaIdentityEntity
import com.dot.gallery.data.entity.PhotoNoteEntity
import com.dot.gallery.domain.model.UriMedia
import com.dot.gallery.domain.util.getUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/*
 * 光注（照片附言）：
 * - 默认只存在手写相册的数据库里；
 * - 用户手动点“写入照片”时，才写进照片文件的 XMP（并同步 EXIF ImageDescription），原图像素不动；
 * - 写入流程：写前备份 → 合并 XMP（保留实况照片等已有字段）→ 写后校验（我们的 UUID 在、照片仍能解码、尺寸不变）→ 失败用备份还原。
 */

data class NoteField(val key: String, val value: String)

// 用 kotlinx.serialization 的 Json 树（不依赖 Android 的 org.json，电脑上也能跑单元测试）
fun List<NoteField>.toJson(): String = kotlinx.serialization.json.JsonArray(map {
    kotlinx.serialization.json.JsonObject(mapOf("k" to kotlinx.serialization.json.JsonPrimitive(it.key), "v" to kotlinx.serialization.json.JsonPrimitive(it.value)))
}).toString()

fun parseFields(json: String?): List<NoteField> = runCatching {
    kotlinx.serialization.json.Json.parseToJsonElement(json ?: "[]").let { it as kotlinx.serialization.json.JsonArray }.map { e ->
        val o = e as kotlinx.serialization.json.JsonObject
        NoteField((o["k"] as? kotlinx.serialization.json.JsonPrimitive)?.content.orEmpty(), (o["v"] as? kotlinx.serialization.json.JsonPrimitive)?.content.orEmpty())
    }
}.getOrDefault(emptyList())

internal fun stringsToJson(list: List<String>): String = kotlinx.serialization.json.JsonArray(list.map { kotlinx.serialization.json.JsonPrimitive(it) }).toString()
internal fun jsonToStrings(json: String): List<String>? = runCatching {
    (kotlinx.serialization.json.Json.parseToJsonElement(json) as kotlinx.serialization.json.JsonArray).map { (it as kotlinx.serialization.json.JsonPrimitive).content }
}.getOrNull()

class HwNotesRepository(private val db: InternalDatabase) {
    private val tags = db.getTagDao()
    private val notes = db.getPhotoNoteDao()

    fun noteFlow(mediaId: Long) = notes.noteForMedia(mediaId)
    fun mediaIdsWithNotes() = notes.mediaIdsWithNotes()
    fun recentNotedMediaIds(limit: Int) = notes.recentNotedMediaIds(limit)
    suspend fun searchMediaIds(q: String) = notes.searchMediaIds(q)

    private suspend fun identityFor(m: UriMedia): Long {
        tags.identityByMediaId(m.id)?.let { return it.id }
        return tags.insertIdentity(
            MediaIdentityEntity(
                mediaId = m.id, uri = m.getUri().toString(), size = m.size,
                dateTaken = m.takenTimestamp ?: (m.timestamp * 1000), width = 0, height = 0,
                lastSeenAt = System.currentTimeMillis()
            )
        )
    }

    /** 保存（或清空）附言。文字和栏目都为空时删除这条附言。内容变化后“已写入照片”标记失效。 */
    suspend fun save(m: UriMedia, existing: PhotoNoteEntity?, text: String, fields: List<NoteField>) {
        val cleanFields = fields.filter { it.key.isNotBlank() || it.value.isNotBlank() }
        if (text.isBlank() && cleanFields.isEmpty()) {
            existing?.let { notes.delete(it.id) }
            return
        }
        val now = System.currentTimeMillis()
        val fieldsJson = cleanFields.toJson()
        val changed = existing == null || existing.text != text || (existing.fieldsJson ?: "[]") != fieldsJson
        if (!changed) return
        notes.upsert(
            PhotoNoteEntity(
                id = existing?.id ?: UUID.randomUUID().toString(),
                identityId = existing?.identityId ?: identityFor(m),
                text = text,
                createdAt = existing?.createdAt ?: now,
                updatedAt = now,
                writtenToFile = false,
                writtenAt = existing?.writtenAt,
                fieldsJson = fieldsJson,
            )
        )
    }

    suspend fun markWritten(noteId: String) = notes.markWritten(noteId, System.currentTimeMillis())

    // ---- 标签 ----
    fun allTags() = tags.tags()
    fun tagsOf(mediaId: Long) = tags.tagsOfMedia(mediaId)
    fun mediaIdsWithAllTags(ids: List<Long>) = tags.mediaIdsWithAllTags(ids, ids.size)
    suspend fun mediaIdsByTagName(q: String) = tags.mediaIdsByTagName(q)

    /** 给照片加标签：标签不存在就新建（颜色按名字固定分配）。 */
    suspend fun addTag(m: UriMedia, rawName: String) {
        val name = rawName.trim().removePrefix("#").trim()
        if (name.isEmpty()) return
        val tag = tags.tagByName(name) ?: run {
            val palette = intArrayOf(0xFFB22A20.toInt(), 0xFF5E7A6E.toInt(), 0xFFC9785B.toInt(), 0xFF3E6FB0.toInt(), 0xFF8A6D3B.toInt(), 0xFF6B5B95.toInt())
            val id = tags.insertTag(
                com.dot.gallery.data.entity.TagEntity(
                    name = name, color = palette[Math.floorMod(name.hashCode(), palette.size)],
                    createdAt = System.currentTimeMillis()
                )
            )
            tags.tagByName(name) ?: return
        }
        val identity = identityFor(m)
        tags.addTags(listOf(com.dot.gallery.data.entity.MediaTagEntity(identity, tag.id, System.currentTimeMillis())))
        notes.markUnwritten(identity)
    }

    suspend fun removeTag(m: UriMedia, tagId: Long) {
        val identity = tags.identityByMediaId(m.id) ?: return
        tags.removeTag(tagId, listOf(identity.id))
        notes.markUnwritten(identity.id)
    }
}

sealed interface WriteResult {
    data object Ok : WriteResult
    data object Unsupported : WriteResult
    /** 需要系统授权才能修改这张照片（Android 11+ 的 MediaStore 写入请求）。 */
    data class NeedsPermission(val error: SecurityException) : WriteResult
    data class Failed(val reason: String) : WriteResult
}

object HwXmpWriter {
    private const val NS = "https://github.com/Daoge622/HandwrittenAlbum/ns/1.0/"

    // 目前只写 JPEG（手机照片的主流格式；HEIC 暂不支持写入）。PNG/WebP 后续单独实现。
    fun supports(mime: String) = mime == "image/jpeg"

    private fun esc(s: String) = s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")

    /** 我们要写的属性（dc:description 是通用字段，hw:* 是手写相册自己的命名空间）。 */
    private fun ourProps(note: PhotoNoteEntity, fields: List<NoteField>, subjects: List<String>, ours: List<String>): String = buildString {
        if (subjects.isNotEmpty()) {
            append("<dc:subject><rdf:Bag>")
            subjects.forEach { append("<rdf:li>").append(esc(it)).append("</rdf:li>") }
            append("</rdf:Bag></dc:subject>")
        }
        if (ours.isNotEmpty()) append("<hw:tags>").append(esc(stringsToJson(ours))).append("</hw:tags>")
        append("<dc:description><rdf:Alt><rdf:li xml:lang=\"x-default\">").append(esc(note.text)).append("</rdf:li></rdf:Alt></dc:description>")
        append("<hw:uuid>").append(note.id).append("</hw:uuid>")
        append("<hw:note>").append(esc(note.text)).append("</hw:note>")
        append("<hw:fields>").append(esc(fields.toJson())).append("</hw:fields>")
        append("<hw:updated>").append(note.updatedAt).append("</hw:updated>")
    }

    /** 合并进已有 XMP：先去掉旧的 dc:description 和 hw:*，再插入新的；没有 XMP 就新建一个包。 */
    private fun unesc(s: String) = s.replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&apos;", "'").replace("&amp;", "&")

    /**
     * 标签写进通用的 dc:subject（关键词，别的相册/电脑软件也认）。
     * 照片里原有的关键词保留；我们上次写进去、现在已删掉的标签会被移除（上次写的记在 hw:tags）。
     */
    fun merge(existing: String?, note: PhotoNoteEntity, fields: List<NoteField>, tags: List<String> = emptyList()): String {
        val oldSubjects = existing?.let { x ->
            Regex("<dc:subject>(.*?)</dc:subject>", RegexOption.DOT_MATCHES_ALL).find(x)?.groupValues?.get(1)?.let { body ->
                Regex("<rdf:li[^>]*>(.*?)</rdf:li>", RegexOption.DOT_MATCHES_ALL).findAll(body).map { unesc(it.groupValues[1].trim()) }.toList()
            }
        }.orEmpty()
        val oldOurs = existing?.let { x ->
            Regex("<hw:tags>(.*?)</hw:tags>", RegexOption.DOT_MATCHES_ALL).find(x)?.groupValues?.get(1)?.let { j ->
                jsonToStrings(unesc(j))
            }
        }.orEmpty()
        val subjects = (oldSubjects.filterNot { it in oldOurs } + tags).filter { it.isNotBlank() }.distinct()
        val props = ourProps(note, fields, subjects, tags)
        val ns = "xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:hw=\"$NS\""
        if (existing.isNullOrBlank() || !existing.contains("rdf:Description")) {
            return "<?xpacket begin=\"﻿\" id=\"W5M0MpCehiHzreSzNTczkc9d\"?>" +
                "<x:xmpmeta xmlns:x=\"adobe:ns:meta/\"><rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">" +
                "<rdf:Description rdf:about=\"\" $ns>$props</rdf:Description></rdf:RDF></x:xmpmeta><?xpacket end=\"w\"?>"
        }
        var x = existing
            .replace(Regex("<dc:description>.*?</dc:description>", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("<dc:subject>.*?</dc:subject>", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("<hw:(\\w+)>.*?</hw:\\1>", RegexOption.DOT_MATCHES_ALL), "")
        // 第一个 rdf:Description：补命名空间；自闭合的改成成对标签，再把属性放进去
        val open = Regex("<rdf:Description\\b([^>]*?)(/?)>").find(x) ?: return x
        var attrs = open.groupValues[1]
        if (!attrs.contains("xmlns:dc=")) attrs += " xmlns:dc=\"http://purl.org/dc/elements/1.1/\""
        if (!attrs.contains("xmlns:hw=")) attrs += " xmlns:hw=\"$NS\""
        val replacement = if (open.groupValues[2] == "/") "<rdf:Description$attrs>$props</rdf:Description>"
        else "<rdf:Description$attrs>$props"
        x = x.replaceRange(open.range, replacement)
        return x
    }

    suspend fun write(context: Context, m: UriMedia, note: PhotoNoteEntity, tags: List<String> = emptyList()): WriteResult = withContext(Dispatchers.IO) {
        if (!supports(m.mimeType)) return@withContext WriteResult.Unsupported
        val uri = m.getUri()
        val cr = context.contentResolver
        val fields = parseFields(note.fieldsJson)
        // 1. 读入原图并备份
        val original = runCatching { cr.openInputStream(uri)?.use { it.readBytes() } }.getOrNull()
            ?: return@withContext WriteResult.Failed("读取原图失败")
        val dir = File(context.filesDir, "hw_backups").apply { mkdirs() }
        dir.listFiles()?.filter { System.currentTimeMillis() - it.lastModified() > 7L * 24 * 3600 * 1000 }?.forEach { it.delete() }
        val backup = File(dir, "${m.id}_${System.currentTimeMillis()}.bak").apply { writeBytes(original) }
        // 2. 合并 XMP，生成新文件内容（只替换 XMP 段，其余字节不动）
        val xmp = merge(JpegXmp.read(original), note, fields, tags)
        val updated = JpegXmp.write(original, xmp)
            ?: return@withContext WriteResult.Failed("这张照片的结构无法安全写入，未做任何修改")
        // 3. 写回
        try {
            cr.openOutputStream(uri, "wt")?.use { it.write(updated) } ?: return@withContext WriteResult.Failed("无法打开照片")
        } catch (e: SecurityException) {
            backup.delete()
            return@withContext WriteResult.NeedsPermission(e)
        } catch (e: Exception) {
            restore(context, uri, backup)
            return@withContext WriteResult.Failed("写入失败，已还原原图")
        }
        // 4. 写后校验：XMP 里有我们的 UUID；照片能解码且尺寸不变；文件尾（HDR 增益图、实况视频）逐字节一致
        val ok = runCatching {
            val after = cr.openInputStream(uri)?.use { it.readBytes() } ?: return@runCatching false
            val tail = minOf(original.size, after.size, 4 * 1024 * 1024)
            val sameTail = (0 until tail).all { original[original.size - 1 - it] == after[after.size - 1 - it] }
            val o1 = BitmapFactory.Options().apply { inJustDecodeBounds = true }.also { BitmapFactory.decodeByteArray(original, 0, original.size, it) }
            val o2 = BitmapFactory.Options().apply { inJustDecodeBounds = true }.also { BitmapFactory.decodeByteArray(after, 0, after.size, it) }
            JpegXmp.read(after)?.contains(note.id) == true && sameTail && o2.outWidth > 0 &&
                o1.outWidth == o2.outWidth && o1.outHeight == o2.outHeight
        }.getOrDefault(false)
        if (!ok) {
            restore(context, uri, backup)
            return@withContext WriteResult.Failed("校验未通过，已还原原图")
        }
        WriteResult.Ok
    }

    private fun restore(context: Context, uri: android.net.Uri, backup: File) {
        runCatching {
            if (backup.exists()) context.contentResolver.openOutputStream(uri, "wt")?.use { out -> backup.inputStream().use { it.copyTo(out) } }
        }
    }
}
