/*
 * SPDX-FileCopyrightText: 2026 手写相册 contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.dot.gallery.handwritten

import com.dot.gallery.data.InkCodec
import com.dot.gallery.data.InkPoint
import com.dot.gallery.data.InkStroke
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class InkCodecTest {

    private fun stroke(n: Int, seed: Int): InkStroke = InkStroke(List(n) { i ->
        InkPoint(
            x = ((i * 37 + seed) % 1000) / 1000f,
            y = ((i * 53 + seed * 7) % 1000) / 1000f,
            w = 1.6f + (i % 9) * 0.5f,
            t = i * 8 + (i % 3)
        )
    })

    @Test
    fun roundTrip_keepsPointsWithinQuantization() {
        val input = listOf(stroke(1, 1), stroke(120, 2), stroke(400, 3))
        val output = InkCodec.decode(InkCodec.encode(input))
        assertEquals(input.size, output.size)
        input.zip(output).forEach { (a, b) ->
            assertEquals(a.points.size, b.points.size)
            a.points.zip(b.points).forEach { (p, q) ->
                assertTrue(abs(p.x - q.x) < 1f / 65535f + 1e-6f)
                assertTrue(abs(p.y - q.y) < 1f / 65535f + 1e-6f)
                assertTrue(abs(p.w - q.w) <= 0.005f + 1e-6f)
                assertEquals(p.t, q.t)
            }
        }
    }

    @Test
    fun emptyInputs() {
        assertEquals(emptyList<InkStroke>(), InkCodec.decode(ByteArray(0)))
        assertEquals(emptyList<InkStroke>(), InkCodec.decode(InkCodec.encode(emptyList())))
        assertEquals(listOf(InkStroke(emptyList())), InkCodec.decode(InkCodec.encode(listOf(InkStroke(emptyList())))))
    }

    @Test
    fun clampsOutOfRangeCoordinates() {
        val out = InkCodec.decode(InkCodec.encode(listOf(InkStroke(listOf(InkPoint(-0.2f, 1.7f, 3f, 0))))))
        val p = out.single().points.single()
        assertEquals(0f, p.x, 0f)
        assertEquals(1f, p.y, 0f)
    }

    @Test
    fun isCompact() {
        // 一句手写光注大约 10 笔、每笔 60 个点：应在 6KB 以内
        val bytes = InkCodec.encode(List(10) { stroke(60, it) })
        assertTrue("size=${bytes.size}", bytes.size < 6_000)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsUnknownVersion() {
        InkCodec.decode(byteArrayOf(9, 0))
    }
}
