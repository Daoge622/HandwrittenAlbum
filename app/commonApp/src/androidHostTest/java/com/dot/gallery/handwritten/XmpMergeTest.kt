/*
 * SPDX-FileCopyrightText: 2026 手写相册 contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.dot.gallery.handwritten

import com.dot.gallery.app.presentation.hw.HwXmpWriter
import com.dot.gallery.data.entity.PhotoNoteEntity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class XmpMergeTest {
    private val note = PhotoNoteEntity(
        id = "u-1", identityId = 1, text = "海边 & 风", createdAt = 1, updatedAt = 2,
        writtenToFile = false, writtenAt = null, fieldsJson = "[]"
    )

    private fun subjects(x: String) = Regex("<dc:subject>(.*?)</dc:subject>", RegexOption.DOT_MATCHES_ALL).find(x)?.groupValues?.get(1)
        ?.let { b -> Regex("<rdf:li>(.*?)</rdf:li>").findAll(b).map { it.groupValues[1] }.toList() }.orEmpty()

    @Test
    fun newPacket_hasTagsAndEscapedText() {
        val x = HwXmpWriter.merge(null, note, emptyList(), listOf("海", "猫"))
        assertTrue(x.contains("海边 &amp; 风"))
        assertTrue(subjects(x) == listOf("海", "猫"))
    }

    @Test
    fun keepsForeignKeywords_dropsOurRemovedTags() {
        val existing = """<x:xmpmeta xmlns:x="adobe:ns:meta/"><rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">""" +
            """<rdf:Description rdf:about="" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:hw="https://github.com/Daoge622/HandwrittenAlbum/ns/1.0/">""" +
            """<dc:subject><rdf:Bag><rdf:li>旅行</rdf:li><rdf:li>猫</rdf:li></rdf:Bag></dc:subject>""" +
            """<hw:tags>[&quot;猫&quot;]</hw:tags><hw:uuid>old</hw:uuid>""" +
            """</rdf:Description></rdf:RDF></x:xmpmeta>"""
        val x = HwXmpWriter.merge(existing, note, emptyList(), listOf("海"))
        assertTrue(subjects(x) == listOf("旅行", "海"))
        assertFalse(x.contains("<hw:uuid>old</hw:uuid>"))
        assertTrue(x.contains("<hw:uuid>u-1</hw:uuid>"))
        assertTrue(Regex("<dc:subject>").findAll(x).count() == 1)
    }

    @Test
    fun noTags_keepsForeignKeywordsOnly() {
        val existing = """<x:xmpmeta xmlns:x="adobe:ns:meta/"><rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">""" +
            """<rdf:Description rdf:about="" xmlns:dc="http://purl.org/dc/elements/1.1/"><dc:subject><rdf:Bag><rdf:li>旅行</rdf:li></rdf:Bag></dc:subject>""" +
            """</rdf:Description></rdf:RDF></x:xmpmeta>"""
        val x = HwXmpWriter.merge(existing, note, emptyList(), emptyList())
        assertTrue(subjects(x) == listOf("旅行"))
        assertFalse(x.contains("<hw:tags>"))
    }
}
