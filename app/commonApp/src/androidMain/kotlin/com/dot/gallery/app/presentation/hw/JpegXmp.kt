/*
 * SPDX-FileCopyrightText: 2026 手写相册 contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.dot.gallery.app.presentation.hw

import java.io.ByteArrayOutputStream

/*
 * JPEG 的 XMP 段读写（不依赖 ExifInterface）。
 *
 * 为什么自己写：
 * - 手机拍的 Ultra HDR / 实况照片通常已有一个独立的 XMP APP1 段。ExifInterface 写 TAG_XMP 时会另外写进
 *   EXIF 的 0x02BC 标签，读取时却仍优先返回原来的独立段，导致“写了但读不到”；
 * - ExifInterface 把 TAG_XMP 按 ASCII 编码，中文会变成问号。
 *
 * 做法：只替换（或新增）主图里 SOS 之前的标准 XMP APP1 段（"http://ns.adobe.com/xap/1.0/\0"），
 * 其余字节原样保留：EXIF、ICC、MPF（APP2）、扫描数据，以及主图 EOI 之后附加的 HDR 增益图、实况视频。
 * XMP 段总在 MPF 段之前，所以 MPF 里“相对 MPF 头”的偏移量不受影响；
 * 实况照片和 Ultra HDR 的容器长度是从文件尾倒算的，也不受影响。
 */
object JpegXmp {
    private val XMP_NS = "http://ns.adobe.com/xap/1.0/\u0000".toByteArray(Charsets.ISO_8859_1)
    private const val MAX_PAYLOAD = 65533 - 29 // APP1 段最大长度减去命名空间头

    private class Segment(val marker: Int, val start: Int, val end: Int) // [start, end) 含 FF xx 与长度

    private fun segments(b: ByteArray): List<Segment>? {
        if (b.size < 4 || (b[0].toInt() and 0xFF) != 0xFF || (b[1].toInt() and 0xFF) != 0xD8) return null
        val out = ArrayList<Segment>()
        var i = 2
        while (i + 4 <= b.size) {
            if ((b[i].toInt() and 0xFF) != 0xFF) return null
            val marker = b[i + 1].toInt() and 0xFF
            if (marker == 0xD8 || marker in 0xD0..0xD7 || marker == 0x01 || marker == 0xFF) { i++; continue }
            if (marker == 0xDA || marker == 0xD9) { out += Segment(marker, i, i); return out } // SOS/EOI：头部结束
            val len = ((b[i + 2].toInt() and 0xFF) shl 8) or (b[i + 3].toInt() and 0xFF)
            if (len < 2 || i + 2 + len > b.size) return null
            out += Segment(marker, i, i + 2 + len)
            i += 2 + len
        }
        return null
    }

    private fun isXmp(b: ByteArray, s: Segment): Boolean {
        if (s.marker != 0xE1) return false
        val p = s.start + 4
        if (p + XMP_NS.size > s.end) return false
        for (k in XMP_NS.indices) if (b[p + k] != XMP_NS[k]) return false
        return true
    }

    /** 读出主图的标准 XMP（UTF-8）；没有则返回 null。 */
    fun read(b: ByteArray): String? {
        val segs = segments(b) ?: return null
        val s = segs.firstOrNull { isXmp(b, it) } ?: return null
        val p = s.start + 4 + XMP_NS.size
        return String(b, p, s.end - p, Charsets.UTF_8)
    }

    /** 返回替换（或新增）XMP 段后的完整文件字节；不是合法 JPEG 或 XMP 过大时返回 null。 */
    fun write(b: ByteArray, xmp: String): ByteArray? {
        val segs = segments(b) ?: return null
        val payload = xmp.toByteArray(Charsets.UTF_8)
        if (payload.size > MAX_PAYLOAD) return null
        val segLen = 2 + XMP_NS.size + payload.size
        val newSeg = ByteArrayOutputStream(segLen + 2).apply {
            write(0xFF); write(0xE1); write((segLen shr 8) and 0xFF); write(segLen and 0xFF)
            write(XMP_NS); write(payload)
        }.toByteArray()
        val old = segs.firstOrNull { isXmp(b, it) }
        val out = ByteArrayOutputStream(b.size + newSeg.size)
        if (old != null) {
            out.write(b, 0, old.start); out.write(newSeg); out.write(b, old.end, b.size - old.end)
        } else {
            // 放在 APP0（JFIF）和 EXIF APP1 之后、其他段之前
            var at = 2
            for (s in segs) { if (s.marker == 0xE0 || (s.marker == 0xE1 && !isXmp(b, s))) at = s.end else break }
            out.write(b, 0, at); out.write(newSeg); out.write(b, at, b.size - at)
        }
        return out.toByteArray()
    }
}
