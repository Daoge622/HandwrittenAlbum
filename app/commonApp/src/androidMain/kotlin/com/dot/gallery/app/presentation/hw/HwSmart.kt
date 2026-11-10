/*
 * SPDX-FileCopyrightText: 2026 手写相册 contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.dot.gallery.app.presentation.hw

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.dot.gallery.app.presentation.search.helpers.SearchVisionHelper
import com.dot.gallery.core.ml.ModelGroup
import com.dot.gallery.injection.ComponentOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.exp

/*
 * 画面识别（全程在手机上，不联网）：
 * - 底层用 ReFra 已有的后台索引：它用内置的 CLIP 模型给每张照片算一个 512 维的“画面向量”；
 * - 这里准备一份中文词表，每个词配一句英文描述，用同一模型的文字部分算出“词向量”（只算一次，存成文件）；
 * - 一张照片和哪些词最像，就认为画面里有什么——这就是自动标签。它们不进用户的标签表，也不会写进照片；
 * - 搜索：中文按词表匹配；纯英文的一句话直接算向量去比。
 */
class HwSmart private constructor(private val context: Context) {
    private val component = (context as ComponentOwner).component
    private val dao get() = component.database.getImageEmbeddingDao()
    private val models get() = component.modelManager
    private val helper by lazy { SearchVisionHelper(models) }

    private val mutex = Mutex()
    @Volatile private var labelVecs: Array<FloatArray>? = null
    private val tags = ConcurrentHashMap<Long, IntArray>()

    /** 每归类完一批加一，界面据此刷新。 */
    val version = MutableStateFlow(0)

    val ready: Boolean get() = models.isReady(ModelGroup.SEARCH)

    fun labelsOf(mediaId: Long): List<String> = tags[mediaId]?.map { LABELS[it].first }.orEmpty()

    /** 画面里有“和 q 对得上的词”的照片。 */
    fun idsMatching(q: String): Set<Long> {
        val query = q.trim()
        if (query.isEmpty() || tags.isEmpty()) return emptySet()
        val hit = LABELS.indices.filter { i ->
            val name = LABELS[i].first
            name == query || name.contains(query) || (name.length >= 2 && query.contains(name))
        }.toHashSet()
        if (hit.isEmpty()) return emptySet()
        return tags.entries.filter { e -> e.value.any { it in hit } }.mapTo(HashSet()) { it.key }
    }

    /** 英文的一句话直接搜（模型的文字部分只懂英文）。 */
    suspend fun textSearch(q: String): Set<Long> = withContext(Dispatchers.Default) {
        val query = q.trim()
        if (!ready || query.length < 3 || !query.all { it.code < 128 } || query.none { it.isLetter() }) return@withContext emptySet()
        runCatching {
            val t = helper.setupTextSession().use { helper.getTextEmbedding(it, "a photo of $query") }
            val all = dao.getRecords().first()
            val scored = all.map { it.id to dot(it.embedding, t) }.filter { it.second >= 0.225f }.sortedByDescending { it.second }
            scored.take(300).mapTo(HashSet()) { it.first }
        }.getOrDefault(emptySet())
    }

    /** 把新索引好的照片归类。没有新照片时只查一次 id，很轻。 */
    suspend fun refresh() = withContext(Dispatchers.Default) {
        if (!ready) return@withContext
        mutex.withLock {
            runCatching {
                val vecs = labelVecs ?: loadOrBuildLabelVectors().also { labelVecs = it }
                val ids = dao.getIds()
                val alive = ids.toHashSet()
                val removed = tags.keys.filter { it !in alive }
                removed.forEach { tags.remove(it) }
                val fresh = ids.filter { !tags.containsKey(it) }
                fresh.chunked(400).forEach { chunk ->
                    dao.getRecords(chunk).forEach { r -> tags[r.id] = classify(r.embedding, vecs) }
                }
                if (fresh.isNotEmpty() || removed.isNotEmpty()) version.value++
            }
        }
    }

    private fun dot(a: FloatArray, b: FloatArray): Float {
        var s = 0f
        val n = minOf(a.size, b.size)
        for (i in 0 until n) s += a[i] * b[i]
        return s
    }

    /** 和各个词的相似度做 softmax，取概率够高、本身也够像的前三个。 */
    private fun classify(e: FloatArray, vecs: Array<FloatArray>): IntArray {
        val sims = FloatArray(vecs.size) { dot(e, vecs[it]) }
        val max = sims.max()
        var sum = 0.0
        val p = DoubleArray(sims.size) { exp(((sims[it] - max) * 100f).toDouble()).also { v -> sum += v } }
        return sims.indices
            .filter { p[it] / sum >= 0.12 && sims[it] >= 0.2f }
            .sortedByDescending { p[it] }
            .take(3).toIntArray()
    }

    private fun loadOrBuildLabelVectors(): Array<FloatArray> {
        val key = LABELS.joinToString("|") { it.second }.hashCode().toUInt().toString(16)
        val file = File(context.filesDir, "hw_label_vectors_$key.bin")
        if (file.length() == LABELS.size * DIM * 4L) {
            val buf = ByteBuffer.wrap(file.readBytes()).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer()
            return Array(LABELS.size) { FloatArray(DIM).also { buf.get(it) } }
        }
        val vecs = helper.setupTextSession().use { session ->
            Array(LABELS.size) { helper.getTextEmbedding(session, "a photo of ${LABELS[it].second}") }
        }
        check(vecs.all { it.size == DIM }) { "unexpected embedding size" }
        context.filesDir.listFiles { f -> f.name.startsWith("hw_label_vectors_") }?.forEach { it.delete() }
        val out = ByteBuffer.allocate(LABELS.size * DIM * 4).order(ByteOrder.LITTLE_ENDIAN)
        vecs.forEach { v -> v.forEach { out.putFloat(it) } }
        file.writeBytes(out.array())
        return vecs
    }

    companion object {
        private const val DIM = 512
        @Volatile private var instance: HwSmart? = null
        fun get(context: Context): HwSmart = instance ?: synchronized(this) {
            instance ?: HwSmart(context.applicationContext).also { instance = it }
        }

        /** 词表：中文名 → 给模型看的英文描述（只用字母和空格）。 */
        val LABELS: List<Pair<String, String>> = listOf(
            // 人
            "自拍" to "a selfie", "合影" to "a group of people posing together", "人像" to "a portrait of a person",
            "婴儿" to "a baby", "小孩" to "a child playing", "婚礼" to "a wedding", "聚会" to "a party with friends",
            "毕业" to "a graduation ceremony", "演出" to "a concert or stage performance", "运动" to "people playing sports",
            // 动物
            "猫" to "a cat", "狗" to "a dog", "鸟" to "a bird", "鱼" to "fish in water", "昆虫" to "an insect",
            "兔子" to "a rabbit", "马" to "a horse", "牛羊" to "cows or sheep in a field",
            // 吃喝
            "美食" to "a plate of food", "火锅" to "a hot pot meal", "面条" to "a bowl of noodles", "米饭" to "a bowl of rice with dishes",
            "烧烤" to "barbecue skewers", "甜点" to "a dessert", "蛋糕" to "a cake", "水果" to "fresh fruit",
            "咖啡" to "a cup of coffee", "奶茶" to "a cup of bubble tea", "饮料" to "a drink in a glass", "酒" to "beer or wine",
            // 自然
            "海边" to "a beach and the sea", "山" to "mountains", "湖" to "a lake", "河流" to "a river", "瀑布" to "a waterfall",
            "森林" to "a forest", "草地" to "a grass field", "沙漠" to "a desert", "雪景" to "a snowy landscape",
            "天空" to "the sky with clouds", "日落" to "a sunset", "日出" to "a sunrise", "星空" to "a starry night sky",
            "月亮" to "the moon", "彩虹" to "a rainbow", "雨天" to "a rainy day", "花" to "flowers", "树" to "a tree",
            "植物" to "a potted plant", "田野" to "farmland",
            // 城市与建筑
            "城市" to "a city skyline", "夜景" to "a city at night with lights", "街道" to "a street", "建筑" to "a building",
            "高楼" to "a skyscraper", "桥" to "a bridge", "寺庙" to "a temple", "古建筑" to "ancient chinese architecture",
            "公园" to "a park", "游乐园" to "an amusement park", "商场" to "a shopping mall", "餐厅" to "inside a restaurant",
            "学校" to "a school campus", "教室" to "a classroom", "办公室" to "an office", "医院" to "a hospital",
            "博物馆" to "a museum exhibit", "烟花" to "fireworks",
            // 室内与物品
            "房间" to "a bedroom", "客厅" to "a living room", "厨房" to "a kitchen", "家具" to "furniture",
            "电脑" to "a computer", "手机" to "a mobile phone", "键盘" to "a keyboard", "相机" to "a camera",
            "书" to "books", "玩具" to "toys", "手办" to "an anime figure", "乐器" to "a musical instrument",
            "衣服" to "clothes", "鞋" to "shoes", "包" to "a handbag", "首饰" to "jewelry", "手表" to "a wrist watch",
            "化妆品" to "cosmetics", "礼物" to "a gift box", "钱" to "money",
            // 交通
            "汽车" to "a car", "自行车" to "a bicycle", "摩托车" to "a motorcycle", "火车" to "a train", "地铁" to "a subway station",
            "飞机" to "an airplane", "机场" to "an airport", "船" to "a boat", "公路" to "a road trip highway",
            // 文字与屏幕
            "截图" to "a screenshot of a phone screen", "聊天记录" to "a screenshot of a chat conversation",
            "文档" to "a document with text", "手写" to "handwritten notes", "证件" to "an identity card",
            "票据" to "a receipt", "二维码" to "a qr code", "试卷" to "an exam paper", "白板" to "a whiteboard or blackboard",
            "幻灯片" to "a presentation slide", "地图" to "a map", "表格" to "a spreadsheet", "海报" to "a poster",
            "菜单" to "a restaurant menu", "车票" to "a ticket",
            // 画与其他
            "绘画" to "a painting", "插画" to "an illustration", "动漫" to "an anime drawing", "游戏" to "a video game screenshot",
            "表情包" to "a meme", "黑白" to "a black and white photo", "夜晚" to "a dark night scene",
        )
    }
}

@Composable
fun rememberSmart(): HwSmart {
    val context = LocalContext.current
    return remember { HwSmart.get(context) }
}
