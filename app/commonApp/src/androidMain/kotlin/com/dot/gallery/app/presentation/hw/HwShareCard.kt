/*
 * SPDX-FileCopyrightText: 2026 手写相册 contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.dot.gallery.app.presentation.hw

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import com.dot.gallery.core.AppIdentity
import com.dot.gallery.domain.model.UriMedia
import com.dot.gallery.domain.model.definedTimestamp
import com.dot.gallery.domain.util.getUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Instant
import java.time.ZoneId

/*
 * 附言卡：把照片和附言排成一张“纸”，生成一张新图拿去分享。
 * 只在用户点“分享附言卡”时生成，存在缓存目录；原照片一个字节都不动。
 */
object HwShareCard {
    private const val W = 1080
    private const val MARGIN = 72

    suspend fun share(context: Context, m: UriMedia, text: String, fields: List<NoteField>, dark: Boolean): Boolean {
        val file = withContext(Dispatchers.IO) { runCatching { render(context, m, text, fields, dark) }.getOrNull() } ?: return false
        val uri = FileProvider.getUriForFile(context, AppIdentity.contentAuthority, file)
        val i = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(i, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        return true
    }

    private fun decodeScaled(context: Context, m: UriMedia, targetW: Int): Bitmap? {
        val cr = context.contentResolver
        val uri = m.getUri()
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        cr.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0) return null
        var sample = 1
        while (bounds.outWidth / (sample * 2) >= targetW) sample *= 2
        val bmp = cr.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample }) } ?: return null
        val rotation = runCatching { cr.openInputStream(uri)?.use { ExifInterface(it).rotationDegrees } ?: 0 }.getOrDefault(0)
        val rotated = if (rotation != 0) Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, Matrix().apply { postRotate(rotation.toFloat()) }, true) else bmp
        val h = (rotated.height * targetW.toFloat() / rotated.width).toInt()
        return Bitmap.createScaledBitmap(rotated, targetW, h, true)
    }

    private fun layout(text: CharSequence, paint: TextPaint, width: Int, spacing: Float) =
        StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL).setLineSpacing(0f, spacing).setIncludePad(false).build()

    private fun render(context: Context, m: UriMedia, text: String, fields: List<NoteField>, dark: Boolean): File? {
        val paper = if (dark) 0xFF141412.toInt() else 0xFFF6F3EC.toInt()
        val ink = if (dark) 0xFFECE7DC.toInt() else 0xFF1C1C1A.toInt()
        val muted = if (dark) 0xFF9A958A.toInt() else 0xFF7A766D.toInt()
        val accent = if (dark) 0xFFD4B47A.toInt() else 0xFFB22A20.toInt()
        val inner = W - MARGIN * 2

        var photo = decodeScaled(context, m, inner) ?: return null
        // 太高的竖图裁到 4:5，避免卡片过长
        val maxH = (inner * 1.25f).toInt()
        if (photo.height > maxH) photo = Bitmap.createBitmap(photo, 0, (photo.height - maxH) / 2, photo.width, maxH)

        val serif = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        val body = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { color = ink; textSize = 44f; typeface = serif }
        val small = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { color = muted; textSize = 30f; letterSpacing = 0.12f }
        val key = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { color = muted; textSize = 32f }
        val value = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { color = ink; textSize = 34f; typeface = serif }

        val d = Instant.ofEpochSecond(m.definedTimestamp).atZone(ZoneId.systemDefault()).toLocalDate()
        val dateLine = "${d.year} · ${d.monthValue} 月 ${d.dayOfMonth} 日"
        val textLayout = text.trim().takeIf { it.isNotEmpty() }?.let { layout(it, body, inner, 1.45f) }
        val cleanFields = fields.filter { it.key.isNotBlank() && it.value.isNotBlank() }
        val keyW = 220
        val fieldLayouts = cleanFields.map { layout(it.key, key, keyW - 20, 1.3f) to layout(it.value, value, inner - keyW, 1.35f) }

        var h = MARGIN + photo.height + 64 + 40 // 照片 + 日期
        textLayout?.let { h += 48 + it.height }
        if (fieldLayouts.isNotEmpty()) h += 56 + fieldLayouts.sumOf { maxOf(it.first.height, it.second.height) + 28 }
        h += MARGIN + 8

        val out = Bitmap.createBitmap(W, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(out)
        c.drawColor(paper)
        var y = MARGIN.toFloat()
        c.drawBitmap(photo, null, Rect(MARGIN, y.toInt(), MARGIN + photo.width, y.toInt() + photo.height), Paint(Paint.FILTER_BITMAP_FLAG))
        y += photo.height + 64
        c.drawText(dateLine, MARGIN.toFloat(), y + 10, small)
        y += 40
        textLayout?.let { l ->
            y += 48
            c.save(); c.translate(MARGIN.toFloat(), y); l.draw(c); c.restore()
            y += l.height
        }
        if (fieldLayouts.isNotEmpty()) {
            y += 28
            c.drawRect(MARGIN.toFloat(), y, (MARGIN + 48).toFloat(), y + 3f, Paint().apply { color = accent })
            y += 28
            fieldLayouts.forEach { (k, v) ->
                c.save(); c.translate(MARGIN.toFloat(), y + 2); k.draw(c); c.restore()
                c.save(); c.translate((MARGIN + keyW).toFloat(), y); v.draw(c); c.restore()
                y += maxOf(k.height, v.height) + 28
            }
        }

        val dir = File(context.cacheDir, "share_cards").apply { mkdirs(); listFiles()?.forEach { it.delete() } }
        val f = File(dir, "note_${m.id}.jpg")
        f.outputStream().use { out.compress(Bitmap.CompressFormat.JPEG, 92, it) }
        return f
    }
}
