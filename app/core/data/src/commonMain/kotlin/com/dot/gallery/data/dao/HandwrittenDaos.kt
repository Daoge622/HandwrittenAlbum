/*
 * SPDX-FileCopyrightText: 2026 手写相册 contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.dot.gallery.data.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import com.dot.gallery.data.entity.InkDrawingEntity
import com.dot.gallery.data.entity.JournalBlockEntity
import com.dot.gallery.data.entity.JournalEntryEntity
import com.dot.gallery.data.entity.MediaIdentityEntity
import com.dot.gallery.data.entity.MediaTagEntity
import com.dot.gallery.data.entity.PhotoNoteEntity
import com.dot.gallery.data.entity.TagEntity
import kotlinx.coroutines.flow.Flow

/** 照片身份 + 标签。 */
@Dao
interface TagDao {

    // ---- 照片身份 ----
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertIdentity(identity: MediaIdentityEntity): Long

    @Query("UPDATE media_identity SET mediaId = :mediaId, uri = :uri, lastSeenAt = :now WHERE id = :id")
    suspend fun relinkIdentity(id: Long, mediaId: Long, uri: String, now: Long)

    @Query("SELECT * FROM media_identity WHERE mediaId = :mediaId LIMIT 1")
    suspend fun identityByMediaId(mediaId: Long): MediaIdentityEntity?

    @Query("SELECT * FROM media_identity WHERE stableUuid = :uuid LIMIT 1")
    suspend fun identityByUuid(uuid: String): MediaIdentityEntity?

    @Query("SELECT * FROM media_identity WHERE sha256 = :sha256")
    suspend fun identitiesBySha256(sha256: String): List<MediaIdentityEntity>

    /** 弱指纹候选：拍摄时间 + 尺寸 + 大小相同。调用方再比对 quickHash，并给出置信度。 */
    @Query("SELECT * FROM media_identity WHERE dateTaken = :dateTaken AND width = :width AND height = :height AND size = :size")
    suspend fun identityCandidates(dateTaken: Long, width: Int, height: Int, size: Long): List<MediaIdentityEntity>

    // ---- 标签 ----
    @Query("SELECT * FROM hw_tag ORDER BY pinned DESC, sortOrder ASC, name ASC")
    fun tags(): Flow<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTag(tag: TagEntity): Long

    @Query("UPDATE hw_tag SET name = :name, color = :color, icon = :icon WHERE id = :id")
    suspend fun updateTag(id: Long, name: String, color: Int, icon: String?)

    @Query("DELETE FROM hw_tag WHERE id = :id")
    suspend fun deleteTag(id: Long)

    @Query("SELECT * FROM hw_tag WHERE name LIKE '%' || :query || '%' ORDER BY name")
    suspend fun searchTags(query: String): List<TagEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTags(links: List<MediaTagEntity>)

    @Query("DELETE FROM hw_media_tag WHERE tagId = :tagId AND identityId IN (:identityIds)")
    suspend fun removeTag(tagId: Long, identityIds: List<Long>)

    @Query("SELECT t.* FROM hw_tag t JOIN hw_media_tag m ON m.tagId = t.id WHERE m.identityId = :identityId ORDER BY t.sortOrder, t.name")
    fun tagsOf(identityId: Long): Flow<List<TagEntity>>

    /** 某张照片（按 MediaStore id）的标签。 */
    @Query(
        "SELECT t.* FROM hw_tag t JOIN hw_media_tag m ON m.tagId = t.id JOIN media_identity i ON i.id = m.identityId " +
            "WHERE i.mediaId = :mediaId ORDER BY m.addedAt"
    )
    fun tagsOfMedia(mediaId: Long): Flow<List<TagEntity>>

    @Query("SELECT * FROM hw_tag WHERE name = :name LIMIT 1")
    suspend fun tagByName(name: String): TagEntity?

    /** 标签名包含关键字的照片（MediaStore id），用于搜索。 */
    @Query(
        "SELECT DISTINCT i.mediaId FROM hw_media_tag m JOIN hw_tag t ON t.id = m.tagId JOIN media_identity i ON i.id = m.identityId " +
            "WHERE t.name LIKE '%' || :q || '%'"
    )
    suspend fun mediaIdsByTagName(q: String): List<Long>

    /** 同时带有全部给定标签的照片（MediaStore id）。 */
    @Query(
        "SELECT i.mediaId FROM hw_media_tag m JOIN media_identity i ON i.id = m.identityId " +
            "WHERE m.tagId IN (:tagIds) GROUP BY i.mediaId HAVING COUNT(DISTINCT m.tagId) = :count"
    )
    fun mediaIdsWithAllTags(tagIds: List<Long>, count: Int): Flow<List<Long>>

    @Query("SELECT tagId, COUNT(*) AS count FROM hw_media_tag GROUP BY tagId")
    fun tagCounts(): Flow<List<TagCount>>

    /**
     * 组合筛选：
     * - [all]：必须同时带有的标签（“与”），可以为空；
     * - [any]：至少带有其中一个（“或”），可以为空；
     * - [none]：不能带有的标签（“排除”）。
     * 返回照片身份 id，按最近加标签的时间倒序。
     */
    @Query(
        """
        SELECT m.identityId FROM hw_media_tag m
        WHERE (:allCount = 0 OR m.identityId IN (
                SELECT identityId FROM hw_media_tag WHERE tagId IN (:all)
                GROUP BY identityId HAVING COUNT(DISTINCT tagId) = :allCount))
          AND (:anyCount = 0 OR m.identityId IN (SELECT identityId FROM hw_media_tag WHERE tagId IN (:any)))
          AND m.identityId NOT IN (SELECT identityId FROM hw_media_tag WHERE tagId IN (:none))
        GROUP BY m.identityId
        ORDER BY MAX(m.addedAt) DESC
        """
    )
    fun filter(all: List<Long>, allCount: Int, any: List<Long>, anyCount: Int, none: List<Long>): Flow<List<Long>>
}

fun TagDao.filter(all: List<Long> = emptyList(), any: List<Long> = emptyList(), none: List<Long> = emptyList()) =
    filter(all, all.size, any, any.size, none)

data class TagCount(val tagId: Long, val count: Int)

/** 光注 + 手写笔迹。 */
@Dao
interface PhotoNoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(note: PhotoNoteEntity)

    @Query("SELECT * FROM photo_note WHERE identityId = :identityId ORDER BY updatedAt DESC")
    fun notesOf(identityId: Long): Flow<List<PhotoNoteEntity>>

    @Query("SELECT * FROM photo_note WHERE id = :id")
    suspend fun note(id: String): PhotoNoteEntity?

    /** 某张照片（按 MediaStore id）当前的光注；一张照片只保留一条附言。 */
    @Query(
        "SELECT n.* FROM photo_note n JOIN media_identity i ON i.id = n.identityId " +
            "WHERE i.mediaId = :mediaId ORDER BY n.updatedAt DESC LIMIT 1"
    )
    fun noteForMedia(mediaId: Long): Flow<PhotoNoteEntity?>

    /** 有光注的照片的 MediaStore id，网格里用来显示小标记。 */
    @Query("SELECT DISTINCT i.mediaId FROM photo_note n JOIN media_identity i ON i.id = n.identityId")
    fun mediaIdsWithNotes(): Flow<List<Long>>

    /** 最近写过附言（有正文）的照片，今日页用。 */
    @Query(
        "SELECT i.mediaId FROM photo_note n JOIN media_identity i ON i.id = n.identityId " +
            "WHERE trim(n.text) != '' ORDER BY n.updatedAt DESC LIMIT :limit"
    )
    fun recentNotedMediaIds(limit: Int): Flow<List<Long>>

    /** 搜索：附言正文、自定义栏目（名称和内容）中包含关键字的照片（MediaStore id）。 */
    @Query(
        "SELECT DISTINCT i.mediaId FROM photo_note n JOIN media_identity i ON i.id = n.identityId " +
            "WHERE n.text LIKE '%' || :q || '%' OR n.fieldsJson LIKE '%' || :q || '%'"
    )
    suspend fun searchMediaIds(q: String): List<Long>

    @Query("DELETE FROM photo_note WHERE id = :id")
    suspend fun delete(id: String)

    @Query("UPDATE photo_note SET writtenToFile = 1, writtenAt = :at WHERE id = :id")
    suspend fun markWritten(id: String, at: Long)

    /** 最近写的光注（首页“今日”用）。 */
    @Query("SELECT * FROM photo_note ORDER BY updatedAt DESC LIMIT :limit")
    fun recent(limit: Int): Flow<List<PhotoNoteEntity>>

    /** 简单的全文查找；第 3 轮会换成 FTS 加中文二元切分。 */
    @Query("SELECT * FROM photo_note WHERE text LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    suspend fun search(query: String): List<PhotoNoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertInk(ink: InkDrawingEntity)

    @Query("SELECT * FROM ink_drawing WHERE id = :id")
    suspend fun ink(id: String): InkDrawingEntity?

    @Query("DELETE FROM ink_drawing WHERE ownerType = :ownerType AND ownerId = :ownerId")
    suspend fun deleteInkOf(ownerType: String, ownerId: String)

    @Transaction
    suspend fun deleteWithInk(id: String) {
        deleteInkOf("note", id)
        delete(id)
    }
}

/** 随笔。 */
@Dao
interface JournalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEntry(entry: JournalEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBlocks(blocks: List<JournalBlockEntity>)

    @Query("DELETE FROM journal_block WHERE entryId = :entryId")
    suspend fun deleteBlocks(entryId: String)

    /** 整篇保存：先清空旧块再写入，保证块的顺序和内容一致。 */
    @Transaction
    suspend fun save(entry: JournalEntryEntity, blocks: List<JournalBlockEntity>) {
        upsertEntry(entry)
        deleteBlocks(entry.id)
        upsertBlocks(blocks)
    }

    @Query("DELETE FROM journal_entry WHERE id = :id")
    suspend fun deleteEntry(id: String)

    @Query("SELECT * FROM journal_entry WHERE id = :id")
    suspend fun entry(id: String): JournalEntryEntity?

    @Query("SELECT * FROM journal_block WHERE entryId = :entryId ORDER BY position")
    suspend fun blocks(entryId: String): List<JournalBlockEntity>

    /** 时间线：按创建时间倒序分页（keyset 分页，[before] 传上一页最后一条的 createdAt）。 */
    @Query("SELECT * FROM journal_entry WHERE createdAt < :before ORDER BY createdAt DESC LIMIT :limit")
    suspend fun page(before: Long, limit: Int): List<JournalEntryEntity>

    @Query("SELECT * FROM journal_entry ORDER BY createdAt DESC")
    fun all(): Flow<List<JournalEntryEntity>>

    /** 日历视图：某个时间段内的随笔。 */
    @Query("SELECT * FROM journal_entry WHERE createdAt BETWEEN :from AND :to ORDER BY createdAt")
    fun between(from: Long, to: Long): Flow<List<JournalEntryEntity>>

    /** 那年今日：同月同日、往年的随笔。[monthDay] 形如 "11-06"，[year] 为今年。 */
    @Query(
        """
        SELECT * FROM journal_entry
        WHERE strftime('%m-%d', createdAt / 1000, 'unixepoch', 'localtime') = :monthDay
          AND strftime('%Y', createdAt / 1000, 'unixepoch', 'localtime') != :year
        ORDER BY createdAt DESC
        """
    )
    fun onThisDay(monthDay: String, year: String): Flow<List<JournalEntryEntity>>

    /** 简单查找标题和正文；第 3 轮会换成 FTS 加中文二元切分。 */
    @Query(
        """
        SELECT DISTINCT e.* FROM journal_entry e LEFT JOIN journal_block b ON b.entryId = e.id
        WHERE e.title LIKE '%' || :query || '%' OR b.payloadJson LIKE '%' || :query || '%'
        ORDER BY e.createdAt DESC
        """
    )
    suspend fun search(query: String): List<JournalEntryEntity>
}
