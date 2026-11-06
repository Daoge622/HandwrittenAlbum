/*
 * SPDX-FileCopyrightText: 2026 手写相册 contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.dot.gallery.data

import kotlin.math.roundToInt

/** 手写笔迹的一个采样点。x、y 为 0..1 归一化坐标；w 为笔画宽度（dp）；t 为相对笔画开始的毫秒数。 */
data class InkPoint(val x: Float, val y: Float, val w: Float, val t: Int)

data class InkStroke(val points: List<InkPoint>)

/**
 * 笔迹的紧凑二进制编码（存进 ink_drawing.strokes）。
 *
 * 格式 v1：
 *   [版本 1 字节] [笔画数 varint]
 *   每个笔画：[点数 varint]，每个点：x u16、y u16（0..65535 对应 0..1）、w u16（宽度 × 100）、Δt varint（毫秒）
 * 一个点约 7–8 字节；一句手写光注一般几 KB。
 */
object InkCodec {
    private const val VERSION: Int = 1
    private const val Q = 65535f

    fun encode(strokes: List<InkStroke>): ByteArray {
        val out = Bytes()
        out.byte(VERSION)
        out.varint(strokes.size)
        for (s in strokes) {
            out.varint(s.points.size)
            var lastT = 0
            for (p in s.points) {
                out.u16((p.x.coerceIn(0f, 1f) * Q).roundToInt())
                out.u16((p.y.coerceIn(0f, 1f) * Q).roundToInt())
                out.u16((p.w * 100f).roundToInt().coerceIn(0, 65535))
                out.varint((p.t - lastT).coerceAtLeast(0))
                lastT = p.t
            }
        }
        return out.toByteArray()
    }

    fun decode(bytes: ByteArray): List<InkStroke> {
        if (bytes.isEmpty()) return emptyList()
        val r = Reader(bytes)
        val version = r.byte()
        require(version == VERSION) { "不支持的笔迹格式版本：$version" }
        val n = r.varint()
        return List(n) {
            val count = r.varint()
            var t = 0
            InkStroke(List(count) {
                val x = r.u16() / Q
                val y = r.u16() / Q
                val w = r.u16() / 100f
                t += r.varint()
                InkPoint(x, y, w, t)
            })
        }
    }

    private class Bytes {
        private var buf = ByteArray(256)
        private var size = 0
        private fun ensure(n: Int) { if (size + n > buf.size) buf = buf.copyOf(maxOf(buf.size * 2, size + n)) }
        fun byte(v: Int) { ensure(1); buf[size++] = v.toByte() }
        fun u16(v: Int) { byte(v and 0xFF); byte((v ushr 8) and 0xFF) }
        fun varint(value: Int) {
            var v = value
            while (v and 0x7F.inv() != 0) { byte((v and 0x7F) or 0x80); v = v ushr 7 }
            byte(v)
        }
        fun toByteArray() = buf.copyOf(size)
    }

    private class Reader(private val b: ByteArray) {
        private var i = 0
        fun byte(): Int = b[i++].toInt() and 0xFF
        fun u16(): Int = byte() or (byte() shl 8)
        fun varint(): Int {
            var shift = 0; var result = 0
            while (true) {
                val x = byte()
                result = result or ((x and 0x7F) shl shift)
                if (x and 0x80 == 0) return result
                shift += 7
                require(shift < 35) { "笔迹数据损坏" }
            }
        }
    }
}
