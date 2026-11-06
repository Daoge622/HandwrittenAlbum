/*
 * SPDX-FileCopyrightText: 2026 手写相册 contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.dot.gallery.data.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

/*
 * 手写相册：标签、光注、随笔、手写笔迹的数据表（数据库 v54 起）。
 *
 * 照片身份不能只靠 MediaStore id：文件被移动、复制或换机后 id 会变。
 * 所以先把照片登记成 [MediaIdentityEntity]（多因子指纹），标签和光注都挂在它上面；
 * 文件变动后由重连逻辑按 stableUuid → sha256 → (拍摄时间 + 尺寸 + 大小 + quickHash) 重新匹配。
 */

/** 一张照片的多因子身份指纹。 */
@Entity(
    tableName = "media_identity",
    indices = [
        Index(value = ["mediaId"]),
        Index(value = ["stableUuid"], unique = true),
        Index(value = ["sha256"]),
        Index(value = ["dateTaken", "width", "height"])
    ]
)
data class MediaIdentityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** 当前对应的 MediaStore id；文件移动后可能失效，由重连逻辑更新。 */
    val mediaId: Long,
    val uri: String,
    val size: Long,
    val dateTaken: Long,
    val width: Int,
    val height: Int,
    /** 文件首尾各 64KB 的快速哈希，十六进制。 */
    val quickHash: String? = null,
    /** 全量 SHA-256，只在需要确认身份时才计算。 */
    val sha256: String? = null,
    /** 写进照片 XMP 的稳定 UUID（hw:uuid）；写入文件之前为空。 */
    val stableUuid: String? = null,
    val lastSeenAt: Long
)

@Entity(tableName = "hw_tag", indices = [Index(value = ["name"], unique = true)])
data class TagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** ARGB 颜色。 */
    val color: Int,
    val icon: String? = null,
    val sortOrder: Int = 0,
    val pinned: Boolean = false,
    val createdAt: Long
)

@Entity(
    tableName = "hw_media_tag",
    primaryKeys = ["identityId", "tagId"],
    foreignKeys = [
        ForeignKey(entity = MediaIdentityEntity::class, parentColumns = ["id"], childColumns = ["identityId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = TagEntity::class, parentColumns = ["id"], childColumns = ["tagId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index(value = ["tagId"])]
)
data class MediaTagEntity(
    val identityId: Long,
    val tagId: Long,
    val addedAt: Long
)

/** 光注：写在一张照片上的一段话（和/或一笔手写）。 */
@Entity(
    tableName = "photo_note",
    foreignKeys = [
        ForeignKey(entity = MediaIdentityEntity::class, parentColumns = ["id"], childColumns = ["identityId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index(value = ["identityId"]), Index(value = ["updatedAt"])]
)
data class PhotoNoteEntity(
    /** UUID 字符串，也会写进照片 XMP，方便换机后对上。 */
    @PrimaryKey val id: String,
    val identityId: Long,
    val text: String,
    /** 对应的 [InkDrawingEntity.id]，没有手写时为空。 */
    val inkId: String? = null,
    /** 文字在照片上的位置，0..1 归一化坐标（左上角为原点）。 */
    val x: Float = 0.06f,
    val y: Float = 0.82f,
    val fontKey: String = "hand",
    val createdAt: Long,
    val updatedAt: Long,
    /** 是否已写进照片文件的 XMP/EXIF。 */
    val writtenToFile: Boolean = false,
    val writtenAt: Long? = null
)

/** 随笔（图文日记）。正文由有序的 [JournalBlockEntity] 组成。 */
@Entity(tableName = "journal_entry", indices = [Index(value = ["createdAt"])])
data class JournalEntryEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val weather: String? = null,
    val mood: String? = null,
    val placeName: String? = null,
    val templateKey: String = "plain",
    val locked: Boolean = false
)

@Entity(
    tableName = "journal_block",
    foreignKeys = [
        ForeignKey(entity = JournalEntryEntity::class, parentColumns = ["id"], childColumns = ["entryId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index(value = ["entryId", "position"])]
)
data class JournalBlockEntity(
    @PrimaryKey val id: String,
    val entryId: String,
    val position: Int,
    /** 见 [JournalBlockType]。 */
    val type: String,
    /** 块内容的 JSON：段落文字、图片 identityId 列表、引用、卡片数据等。 */
    val payloadJson: String
)

object JournalBlockType {
    const val PARAGRAPH = "paragraph"
    const val HEADING = "heading"
    const val QUOTE = "quote"
    const val IMAGES = "images"
    const val DIVIDER = "divider"
    const val INK = "ink"
    const val CARD = "card"
}

/** 一幅手写笔迹（光注或随笔手写块）。笔画点序列用 [com.dot.gallery.data.InkCodec] 紧凑编码。 */
@Entity(tableName = "ink_drawing", indices = [Index(value = ["ownerType", "ownerId"])])
data class InkDrawingEntity(
    @PrimaryKey val id: String,
    /** "note" 或 "block"。 */
    val ownerType: String,
    val ownerId: String,
    val strokes: ByteArray,
    val color: Int,
    val baseWidth: Float,
    val updatedAt: Long
) {
    override fun equals(other: Any?): Boolean =
        other is InkDrawingEntity && id == other.id && ownerType == other.ownerType && ownerId == other.ownerId &&
            strokes.contentEquals(other.strokes) && color == other.color && baseWidth == other.baseWidth && updatedAt == other.updatedAt

    override fun hashCode(): Int = id.hashCode() * 31 + strokes.contentHashCode()
}
