/*
 * SPDX-FileCopyrightText: 2026 手写相册 contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.dot.gallery.app.presentation.hw

import java.io.ByteArrayOutputStream
import java.util.zip.CRC32

/*
 * 按文件真实格式（看文件头，不信扩展名）读写 XMP：JPEG、PNG、WebP。
 * 三种格式都只增删 XMP 自己那一块，图像数据原样保留；[intact] 用来在写后逐字节确认这一点。
 */
object ImageXmp {
    enum class Kind { JPEG, PNG, WEBP }

    fun supports(mime: String) = mime == "image/jpeg" || mime == "image/png" || mime == "image/webp"

    fun kind(b: ByteArray): Kind? = when {
        b.size > 3 && (b[0].toInt() and 0xFF) == 0xFF && (b[1].toInt() and 0xFF) == 0xD8 -> Kind.JPEG
        PngXmp.isPng(b) -> Kind.PNG
        WebpXmp.isWebp(b) -> Kind.WEBP
        else -> null
    }

    fun read(b: ByteArray): String? = when (kind(b)) {
        Kind.JPEG -> JpegXmp.read(b)
        Kind.PNG -> PngXmp.read(b)
        Kind.WEBP -> WebpXmp.read(b)
        null -> null
    }

    fun write(b: ByteArray, xmp: String): ByteArray? = when (kind(b)) {
        Kind.JPEG -> JpegXmp.write(b, xmp)
        Kind.PNG -> PngXmp.write(b, xmp)
        Kind.WEBP -> WebpXmp.write(b, xmp)
        null -> null
    }

    /** 写入前后，除 XMP 那一块以外的字节是否完全一致。 */
    fun intact(original: ByteArray, after: ByteArray): Boolean = when (kind(original)) {
        Kind.JPEG, Kind.PNG -> singleWindow(original, after)
        Kind.WEBP -> WebpXmp.sameImageChunks(original, after)
        null -> false
    }

    /** 两份文件只在一段连续的小窗口里不同（前缀、后缀都逐字节相同）。 */
    internal fun singleWindow(a: ByteArray, b: ByteArray, maxWindow: Int = 256 * 1024): Boolean {
        val min = minOf(a.size, b.size)
        var p = 0
        while (p < min && a[p] == b[p]) p++
        var s = 0
        while (s < min - p && a[a.size - 1 - s] == b[b.size - 1 - s]) s++
        return a.size - p - s <= maxWindow && b.size - p - s <= maxWindow
    }
}

/** PNG：XMP 放在关键字为 XML:com.adobe.xmp 的 iTXt 块里，紧跟 IHDR。 */
object PngXmp {
    private val SIG = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
    private val KEY = "XML:com.adobe.xmp\u0000".toByteArray(Charsets.ISO_8859_1)

    private class Chunk(val type: String, val start: Int, val dataStart: Int, val dataLen: Int) { val end get() = dataStart + dataLen + 4 }

    fun isPng(b: ByteArray) = b.size >= 8 && SIG.indices.all { b[it] == SIG[it] }

    private fun int(b: ByteArray, i: Int) =
        ((b[i].toInt() and 0xFF) shl 24) or ((b[i + 1].toInt() and 0xFF) shl 16) or ((b[i + 2].toInt() and 0xFF) shl 8) or (b[i + 3].toInt() and 0xFF)

    private fun chunks(b: ByteArray): List<Chunk>? {
        if (!isPng(b)) return null
        val out = ArrayList<Chunk>()
        var i = 8
        while (i + 12 <= b.size) {
            val len = int(b, i)
            if (len < 0 || i + 12L + len > b.size) return null
            val c = Chunk(String(b, i + 4, 4, Charsets.ISO_8859_1), i, i + 8, len)
            out += c
            i = c.end
            if (c.type == "IEND") return out
        }
        return null
    }

    private fun isXmp(b: ByteArray, c: Chunk) =
        c.type == "iTXt" && c.dataLen > KEY.size && KEY.indices.all { b[c.dataStart + it] == KEY[it] }

    fun read(b: ByteArray): String? {
        val c = chunks(b)?.firstOrNull { isXmp(b, it) } ?: return null
        var p = c.dataStart + KEY.size
        val end = c.dataStart + c.dataLen
        if (p + 2 > end || b[p].toInt() != 0) return null // 压缩的 iTXt 不读
        p += 2
        repeat(2) { while (p < end && b[p].toInt() != 0) p++; p++ } // 语言标记、翻译关键字
        if (p > end) return null
        return String(b, p, end - p, Charsets.UTF_8)
    }

    fun write(b: ByteArray, xmp: String): ByteArray? {
        val list = chunks(b) ?: return null
        if (list.firstOrNull()?.type != "IHDR") return null
        val data = ByteArrayOutputStream().apply { write(KEY); write(byteArrayOf(0, 0, 0, 0)); write(xmp.toByteArray(Charsets.UTF_8)) }.toByteArray()
        val type = "iTXt".toByteArray(Charsets.ISO_8859_1)
        val crc = CRC32().apply { update(type); update(data) }.value.toInt()
        val out = ByteArrayOutputStream(b.size + data.size + 12)
        fun writeInt(v: Int) { out.write(v ushr 24); out.write(v ushr 16); out.write(v ushr 8); out.write(v) }
        out.write(b, 0, 8)
        for (c in list) {
            if (isXmp(b, c)) continue
            out.write(b, c.start, c.end - c.start)
            if (c.type == "IHDR") { writeInt(data.size); out.write(type); out.write(data); writeInt(crc) }
        }
        return out.toByteArray()
    }
}

/** WebP：XMP 放在 RIFF 的 "XMP " 块里，并在 VP8X 头里置位；简单格式（只有 VP8/VP8L）会先补一个 VP8X 头。 */
object WebpXmp {
    internal class Chunk(val fourcc: String, val start: Int, val dataStart: Int, val dataLen: Int) { val end get() = dataStart + dataLen + (dataLen and 1) }

    fun isWebp(b: ByteArray) = b.size >= 12 && String(b, 0, 4, Charsets.ISO_8859_1) == "RIFF" && String(b, 8, 4, Charsets.ISO_8859_1) == "WEBP"

    private fun le32(b: ByteArray, i: Int) =
        (b[i].toInt() and 0xFF) or ((b[i + 1].toInt() and 0xFF) shl 8) or ((b[i + 2].toInt() and 0xFF) shl 16) or ((b[i + 3].toInt() and 0xFF) shl 24)

    internal fun chunks(b: ByteArray): List<Chunk>? {
        if (!isWebp(b)) return null
        val out = ArrayList<Chunk>()
        var i = 12
        while (i + 8 <= b.size) {
            val len = le32(b, i + 4)
            if (len < 0 || i + 8L + len > b.size) return null
            val c = Chunk(String(b, i, 4, Charsets.ISO_8859_1), i, i + 8, len)
            out += c
            i = c.end
        }
        return out.takeIf { it.isNotEmpty() }
    }

    fun read(b: ByteArray): String? {
        val c = chunks(b)?.firstOrNull { it.fourcc == "XMP " } ?: return null
        return String(b, c.dataStart, c.dataLen, Charsets.UTF_8)
    }

    fun write(b: ByteArray, xmp: String): ByteArray? {
        val list = chunks(b) ?: return null
        val first = list.first()
        val vp8x = ByteArray(10)
        when (first.fourcc) {
            "VP8X" -> {
                if (first.dataLen < 10) return null
                System.arraycopy(b, first.dataStart, vp8x, 0, 10)
            }
            "VP8 " -> {
                val p = first.dataStart
                if (first.dataLen < 10 || (b[p + 3].toInt() and 0xFF) != 0x9D || (b[p + 4].toInt() and 0xFF) != 0x01 || (b[p + 5].toInt() and 0xFF) != 0x2A) return null
                val w = ((b[p + 6].toInt() and 0xFF) or ((b[p + 7].toInt() and 0xFF) shl 8)) and 0x3FFF
                val h = ((b[p + 8].toInt() and 0xFF) or ((b[p + 9].toInt() and 0xFF) shl 8)) and 0x3FFF
                if (w == 0 || h == 0) return null
                canvas(vp8x, w, h)
            }
            "VP8L" -> {
                val p = first.dataStart
                if (first.dataLen < 5 || (b[p].toInt() and 0xFF) != 0x2F) return null
                val bits = le32(b, p + 1)
                canvas(vp8x, (bits and 0x3FFF) + 1, ((bits ushr 14) and 0x3FFF) + 1)
                if ((bits ushr 28) and 1 == 1) vp8x[0] = (vp8x[0].toInt() or 0x10).toByte()
            }
            else -> return null
        }
        vp8x[0] = (vp8x[0].toInt() or 0x04).toByte()

        val payload = xmp.toByteArray(Charsets.UTF_8)
        val out = ByteArrayOutputStream(b.size + payload.size + 32)
        fun le(v: Int) { out.write(v); out.write(v ushr 8); out.write(v ushr 16); out.write(v ushr 24) }
        out.write("RIFF".toByteArray(Charsets.ISO_8859_1)); le(0); out.write("WEBP".toByteArray(Charsets.ISO_8859_1))
        out.write("VP8X".toByteArray(Charsets.ISO_8859_1)); le(10); out.write(vp8x)
        for (c in list) {
            if (c.fourcc == "VP8X" || c.fourcc == "XMP ") continue
            out.write(b, c.start, 8 + c.dataLen)
            if (c.dataLen and 1 == 1) out.write(0)
        }
        out.write("XMP ".toByteArray(Charsets.ISO_8859_1)); le(payload.size); out.write(payload)
        if (payload.size and 1 == 1) out.write(0)
        val bytes = out.toByteArray()
        val size = bytes.size - 8
        bytes[4] = size.toByte(); bytes[5] = (size ushr 8).toByte(); bytes[6] = (size ushr 16).toByte(); bytes[7] = (size ushr 24).toByte()
        return bytes
    }

    private fun canvas(vp8x: ByteArray, w: Int, h: Int) {
        val cw = w - 1; val ch = h - 1
        vp8x[4] = cw.toByte(); vp8x[5] = (cw ushr 8).toByte(); vp8x[6] = (cw ushr 16).toByte()
        vp8x[7] = ch.toByte(); vp8x[8] = (ch ushr 8).toByte(); vp8x[9] = (ch ushr 16).toByte()
    }

    /** 除 VP8X 头和 XMP 块外，其余块的类型、顺序、内容都一致。 */
    fun sameImageChunks(a: ByteArray, b: ByteArray): Boolean {
        val ca = chunks(a)?.filter { it.fourcc != "VP8X" && it.fourcc != "XMP " } ?: return false
        val cb = chunks(b)?.filter { it.fourcc != "VP8X" && it.fourcc != "XMP " } ?: return false
        if (ca.size != cb.size) return false
        for (i in ca.indices) {
            val x = ca[i]; val y = cb[i]
            if (x.fourcc != y.fourcc || x.dataLen != y.dataLen) return false
            for (k in 0 until x.dataLen) if (a[x.dataStart + k] != b[y.dataStart + k]) return false
        }
        return true
    }
}
