/*
 * SPDX-FileCopyrightText: 2026 手写相册 contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.dot.gallery.handwritten

import com.dot.gallery.app.presentation.hw.JpegXmp
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.ByteArrayOutputStream

class JpegXmpTest {

    private fun seg(marker: Int, body: ByteArray) = ByteArrayOutputStream().apply {
        val len = body.size + 2
        write(0xFF); write(marker); write(len shr 8); write(len and 0xFF); write(body)
    }.toByteArray()

    private fun xmpSeg(x: String) = seg(0xE1, "http://ns.adobe.com/xap/1.0/\u0000".toByteArray(Charsets.ISO_8859_1) + x.toByteArray(Charsets.UTF_8))

    /** SOI + APP0 + EXIF + [XMP] + MPF + SOS…扫描数据 + EOI + 附加数据（模拟增益图/实况视频）。 */
    private fun jpeg(xmp: String?, tail: ByteArray): ByteArray = ByteArrayOutputStream().apply {
        write(0xFF); write(0xD8)
        write(seg(0xE0, "JFIF\u0000".toByteArray()))
        write(seg(0xE1, "Exif\u0000\u0000II*\u0000".toByteArray(Charsets.ISO_8859_1)))
        if (xmp != null) write(xmpSeg(xmp))
        write(seg(0xE2, "MPF\u0000payload".toByteArray()))
        write(0xFF); write(0xDA); write(byteArrayOf(0, 2, 1, 2, 3, 4, 5))
        write(0xFF); write(0xD9)
        write(tail)
    }.toByteArray()

    private val tail = ByteArray(5000) { (it * 31 % 251).toByte() }

    @Test
    fun replacesExistingXmp_keepsChineseAndTail() {
        val src = jpeg("<x:xmpmeta>old</x:xmpmeta>", tail)
        val out = JpegXmp.write(src, "<x:xmpmeta>新的附言：海边的风很大</x:xmpmeta>")!!
        assertEquals("<x:xmpmeta>新的附言：海边的风很大</x:xmpmeta>", JpegXmp.read(out))
        assertArrayEquals(tail, out.copyOfRange(out.size - tail.size, out.size))
    }

    @Test
    fun insertsWhenMissing_afterExif() {
        val src = jpeg(null, tail)
        assertNull(JpegXmp.read(src))
        val out = JpegXmp.write(src, "<a>你好</a>")!!
        assertEquals("<a>你好</a>", JpegXmp.read(out))
        assertArrayEquals(tail, out.copyOfRange(out.size - tail.size, out.size))
        assertEquals(src.size + 2 + 2 + 29 + "<a>你好</a>".toByteArray().size, out.size)
    }

    @Test
    fun rejectsNonJpegAndOversizedXmp() {
        assertNull(JpegXmp.write(byteArrayOf(1, 2, 3), "x"))
        assertNull(JpegXmp.write(jpeg(null, tail), "x".repeat(70_000)))
        assertNotNull(JpegXmp.write(jpeg(null, tail), "x".repeat(60_000)))
    }
}
