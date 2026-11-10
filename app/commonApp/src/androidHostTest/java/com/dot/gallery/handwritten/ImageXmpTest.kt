/*
 * SPDX-FileCopyrightText: 2026 手写相册 contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.dot.gallery.handwritten

import com.dot.gallery.app.presentation.hw.ImageXmp
import com.dot.gallery.app.presentation.hw.JpegXmp
import com.dot.gallery.app.presentation.hw.PngXmp
import com.dot.gallery.app.presentation.hw.WebpXmp
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.util.zip.CRC32

class ImageXmpTest {
    private val text = "<x:xmpmeta>海边，风很大 🌊</x:xmpmeta>"

    // ---------- PNG ----------
    private fun pngChunk(type: String, data: ByteArray) = ByteArrayOutputStream().apply {
        val t = type.toByteArray(Charsets.ISO_8859_1)
        val crc = CRC32().apply { update(t); update(data) }.value.toInt()
        fun int(v: Int) { write(v ushr 24); write(v ushr 16); write(v ushr 8); write(v) }
        int(data.size); write(t); write(data); int(crc)
    }.toByteArray()

    private fun png() = ByteArrayOutputStream().apply {
        write(byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A))
        write(pngChunk("IHDR", ByteArray(13) { it.toByte() }))
        write(pngChunk("IDAT", ByteArray(3000) { (it * 7 % 251).toByte() }))
        write(pngChunk("IEND", ByteArray(0)))
    }.toByteArray()

    @Test
    fun png_insertThenReplace_keepsEverythingElse() {
        val original = png()
        assertNull(PngXmp.read(original))
        val once = ImageXmp.write(original, text)!!
        assertEquals(text, ImageXmp.read(once))
        assertTrue(ImageXmp.intact(original, once))
        val twice = ImageXmp.write(once, "second")!!
        assertEquals("second", ImageXmp.read(twice))
        assertTrue(ImageXmp.intact(once, twice))
        // 写两次也只有一个 XMP 块
        assertEquals(original.size + 12 + 22 + "second".length, twice.size)
    }

    @Test
    fun png_damagedPixels_notIntact() {
        val original = png()
        val after = ImageXmp.write(original, text)!!
        after[after.size - 40] = (after[after.size - 40] + 1).toByte()
        after[60] = (after[60] + 1).toByte()
        // 头尾都有改动且相隔很远 → 不是“一小段窗口”
        assertFalse(ImageXmp.singleWindow(original, after, maxWindow = 512))
    }

    // ---------- WebP ----------
    private fun riff(vararg chunks: Pair<String, ByteArray>) = ByteArrayOutputStream().apply {
        val body = ByteArrayOutputStream().apply {
            write("WEBP".toByteArray())
            chunks.forEach { (cc, d) ->
                write(cc.toByteArray()); write(d.size); write(d.size ushr 8); write(d.size ushr 16); write(d.size ushr 24)
                write(d); if (d.size and 1 == 1) write(0)
            }
        }.toByteArray()
        write("RIFF".toByteArray()); write(body.size); write(body.size ushr 8); write(body.size ushr 16); write(body.size ushr 24); write(body)
    }.toByteArray()

    /** VP8L 头：0x2F + 14 位宽-1 + 14 位高-1 + alpha 位。 */
    private fun vp8l(w: Int, h: Int, alpha: Boolean): ByteArray {
        val bits = (w - 1) or ((h - 1) shl 14) or ((if (alpha) 1 else 0) shl 28)
        return byteArrayOf(0x2F, bits.toByte(), (bits ushr 8).toByte(), (bits ushr 16).toByte(), (bits ushr 24).toByte()) + ByteArray(501) { (it % 200).toByte() }
    }

    @Test
    fun webp_simpleLossless_getsVp8xHeaderAndXmp() {
        val original = riff("VP8L" to vp8l(640, 480, alpha = true))
        val after = ImageXmp.write(original, text)!!
        assertEquals(text, ImageXmp.read(after))
        assertTrue(ImageXmp.intact(original, after))
        val chunks = WebpXmp.chunks(after)!!
        assertEquals(listOf("VP8X", "VP8L", "XMP "), chunks.map { it.fourcc })
        val x = chunks[0].dataStart
        assertEquals(0x14, after[x].toInt())                     // XMP + alpha
        assertEquals(639, (after[x + 4].toInt() and 0xFF) or ((after[x + 5].toInt() and 0xFF) shl 8))
        assertEquals(479, (after[x + 7].toInt() and 0xFF) or ((after[x + 8].toInt() and 0xFF) shl 8))
        // RIFF 长度字段正确
        val size = (after[4].toInt() and 0xFF) or ((after[5].toInt() and 0xFF) shl 8) or ((after[6].toInt() and 0xFF) shl 16)
        assertEquals(after.size - 8, size)
        // 再写一次：仍然只有一个 XMP 块
        val twice = ImageXmp.write(after, "b")!!
        assertEquals(listOf("VP8X", "VP8L", "XMP "), WebpXmp.chunks(twice)!!.map { it.fourcc })
        assertEquals("b", ImageXmp.read(twice))
    }

    @Test
    fun webp_extended_keepsExifAndSetsFlag() {
        val vp8x = ByteArray(10).also { it[0] = 0x08 } // 已有 EXIF 标志
        val original = riff("VP8X" to vp8x, "VP8 " to ByteArray(300) { 1 }, "EXIF" to ByteArray(77) { 2 })
        val after = ImageXmp.write(original, text)!!
        val chunks = WebpXmp.chunks(after)!!
        assertEquals(listOf("VP8X", "VP8 ", "EXIF", "XMP "), chunks.map { it.fourcc })
        assertEquals(0x0C, after[chunks[0].dataStart].toInt())
        assertTrue(ImageXmp.intact(original, after))
        after[chunks[1].dataStart + 5] = 9
        assertFalse(ImageXmp.intact(original, after))
    }

    // ---------- JPEG：小文件也要能通过写后校验 ----------
    @Test
    fun jpeg_smallFile_intactAfterWrite() {
        fun seg(marker: Int, body: ByteArray) = ByteArrayOutputStream().apply {
            val len = body.size + 2
            write(0xFF); write(marker); write(len shr 8); write(len and 0xFF); write(body)
        }.toByteArray()
        val original = ByteArrayOutputStream().apply {
            write(0xFF); write(0xD8)
            write(seg(0xE0, "JFIF\u0000".toByteArray()))
            write(seg(0xE1, "Exif\u0000\u0000II*\u0000".toByteArray(Charsets.ISO_8859_1)))
            write(0xFF); write(0xDA); write(ByteArray(9000) { (it % 250).toByte() })
            write(0xFF); write(0xD9)
        }.toByteArray()
        val after = JpegXmp.write(original, text)!!
        assertTrue(ImageXmp.intact(original, after))
        assertArrayEquals(original.copyOfRange(original.size - 9000, original.size), after.copyOfRange(after.size - 9000, after.size))
    }
}
