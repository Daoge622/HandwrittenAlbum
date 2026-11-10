/*
 * SPDX-FileCopyrightText: 2026 手写相册 contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.dot.gallery.app.presentation.hw

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.exifinterface.media.ExifInterface
import com.dot.gallery.core.LocalEventHandler
import com.dot.gallery.core.toggleNavigationBar
import com.dot.gallery.domain.model.UriMedia
import com.dot.gallery.domain.model.definedTimestamp
import com.dot.gallery.domain.util.getUri
import com.dot.gallery.domain.util.isVideo
import com.github.panpf.sketch.AsyncImage
import com.github.panpf.sketch.request.ComposableImageRequest
import com.github.panpf.zoomimage.SketchZoomAsyncImage
import com.github.panpf.zoomimage.rememberSketchZoomState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/*
 * 手写相册自己的照片查看器（动效规格见 docs/08 与原型 art/motion/proto_src.html）：
 * - 浮在当前页面之上，从缩略图在屏幕上的真实位置 **等比** 展开，裁切框从方块平滑打开到整张照片；
 * - 展开后左右翻页、双指缩放（zoomimage，ReFra 已集成）；
 * - 未放大时下拉跟手，照片随距离缩小、背景变淡；松手超过 110dp 或速度够快就缩回原缩略图，否则弹回；
 * - 所有动画都是弹簧，可中途被手指打断。
 */

/** 原型里的弹簧（stiffness / damping，质量 1）换算成 Compose 的阻尼比。 */
private val OpenSpring = spring<Float>(dampingRatio = 0.92f, stiffness = 380f)
private val CloseSpring = spring<Float>(dampingRatio = 0.90f, stiffness = 360f)
private val SnapBackSpring = spring<Float>(dampingRatio = 0.89f, stiffness = 500f)
private val CardSpring = spring<Float>(dampingRatio = 0.86f, stiffness = 320f)

@Stable
class HwViewerState {
    var items by mutableStateOf<List<UriMedia>>(emptyList())
        internal set
    var index by mutableIntStateOf(0)
        internal set
    var visible by mutableStateOf(false)
        internal set
    internal val progress = Animatable(0f)
    internal var settled by mutableStateOf(false)
    /** 缩略图在根坐标系里的位置，key = media id。 */
    internal val thumbs = mutableStateMapOf<Long, Rect>()
    internal val sizes = mutableStateMapOf<Long, IntSize>()
    internal var dragX by mutableFloatStateOf(0f)
    internal var dragY by mutableFloatStateOf(0f)
    internal var openRequest by mutableIntStateOf(0)

    fun open(list: List<UriMedia>, media: UriMedia) {
        items = list
        index = list.indexOfFirst { it.id == media.id }.coerceAtLeast(0)
        dragX = 0f; dragY = 0f
        settled = false
        visible = true
        openRequest++
    }

    val current: UriMedia? get() = items.getOrNull(index)
}

val LocalHwViewer = staticCompositionLocalOf<HwViewerState?> { null }

/** 给缩略图登记位置，查看器据此从原位展开、缩回原位。 */
fun Modifier.hwThumb(state: HwViewerState?, media: UriMedia): Modifier =
    if (state == null) this else onGloballyPositioned { state.thumbs[media.id] = it.boundsInRoot() }

private suspend fun readSize(context: Context, media: UriMedia): IntSize = withContext(Dispatchers.IO) {
    runCatching {
        val uri = media.getUri()
        val o = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, o) }
        var w = o.outWidth; var h = o.outHeight
        val rot = context.contentResolver.openInputStream(uri)?.use {
            ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        } ?: ExifInterface.ORIENTATION_NORMAL
        if (rot == ExifInterface.ORIENTATION_ROTATE_90 || rot == ExifInterface.ORIENTATION_ROTATE_270 ||
            rot == ExifInterface.ORIENTATION_TRANSPOSE || rot == ExifInterface.ORIENTATION_TRANSVERSE) { val t = w; w = h; h = t }
        if (w > 0 && h > 0) IntSize(w, h) else IntSize(3, 4)
    }.getOrDefault(IntSize(3, 4))
}

/** 裁切框：相对图层自身（未缩放）坐标的内缩矩形 + 圆角。 */
private class InsetShape(val l: Float, val t: Float, val r: Float, val b: Float, val radius: Float) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline =
        Outline.Rounded(RoundRect(l, t, size.width - r, size.height - b, CornerRadius(radius, radius)))
}

private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t

/** 把当前页面包起来：提供查看器状态，并在最上层绘制查看器。 */
@Composable
fun HwViewerHost(content: @Composable () -> Unit) {
    val state = remember { HwViewerState() }
    CompositionLocalProvider(LocalHwViewer provides state) {
        Box(Modifier.fillMaxSize()) {
            content()
            if (state.visible) HwViewerOverlay(state)
        }
    }
}

@Composable
private fun HwViewerOverlay(state: HwViewerState) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val eventHandler = LocalEventHandler.current
    val scope = rememberCoroutineScope()
    val card = remember { Animatable(0f) }
    val handler = com.dot.gallery.core.LocalMediaHandler.current
    val favOverride = remember { androidx.compose.runtime.mutableStateMapOf<Long, Boolean>() }
    var moreOpen by remember { mutableStateOf(false) }
    var pendingTrash by remember { mutableStateOf<UriMedia?>(null) }
    fun removeCurrent() {
        val list = state.items.toMutableList()
        val i = state.index
        if (i in list.indices) list.removeAt(i)
        if (list.isEmpty()) { state.visible = false; eventHandler.toggleNavigationBar(true); return }
        state.items = list
        state.index = i.coerceAtMost(list.lastIndex)
        state.openRequest++ // 让翻页器按新列表重建
    }
    val favoriteRequest = com.dot.gallery.core.ui.util.rememberActivityResult()
    val trashRequest = com.dot.gallery.core.ui.util.rememberActivityResult(onResultOk = { if (pendingTrash != null) removeCurrent() })
    val notesRepo = rememberNotesRepository()
    val withNotes by remember { notesRepo.mediaIdsWithNotes() }.collectAsState(initial = emptyList())
    val paper = MaterialTheme.colorScheme.background
    var origin by remember { mutableStateOf(Offset.Zero) }
    var box by remember { mutableStateOf(IntSize.Zero) }
    val dismissPx = with(density) { 110.dp.toPx() }
    val flingPx = with(density) { 0.7.dp.toPx() } * 1000f // 0.7dp/ms → px/s

    fun fitRect(m: UriMedia): Rect {
        val s = state.sizes[m.id] ?: IntSize(3, 4)
        val W = box.width.toFloat(); val H = box.height.toFloat()
        val r = s.width.toFloat() / s.height
        var w = W; var h = W / r
        if (h > H) { h = H; w = H * r }
        return Rect(Offset((W - w) / 2, (H - h) / 2), Size(w, h))
    }

    fun thumbRect(m: UriMedia): Rect {
        val t = state.thumbs[m.id]
        return if (t != null && t.width > 0) t.translate(-origin) else {
            // 缩略图不在屏幕上（例如翻到了别的照片）：从中心缩小淡出
            val f = fitRect(m); val c = f.center
            Rect(Offset(c.x - f.width * 0.3f, c.y - f.height * 0.3f), Size(f.width * 0.6f, f.height * 0.6f))
        }
    }

    fun endDragImpl(v: Float, closeFn: (Float) -> Unit) {
        if (state.dragY < -with(density) { 56.dp.toPx() } || v < -flingPx) {
            val from = state.dragY
            scope.launch { card.animateTo(1f, CardSpring) }
            scope.launch { Animatable(1f).animateTo(0f, SnapBackSpring) { state.dragY = from * value } }
        } else if (card.value > 0.5f && state.dragY > 0f) {
            val from = state.dragY
            scope.launch { card.animateTo(0f, CardSpring) }
            scope.launch { Animatable(1f).animateTo(0f, SnapBackSpring) { state.dragY = from * value } }
        } else if (state.dragY > dismissPx || v > flingPx) closeFn(-v / max(box.height.toFloat(), 1f))
        else scope.launch {
            val from = state.dragY
            Animatable(1f).animateTo(0f, SnapBackSpring) { state.dragY = from * value }
        }
    }

    fun close(velocity: Float = 0f) {
        state.settled = false
        scope.launch {
            state.progress.animateTo(0f, CloseSpring, initialVelocity = velocity)
            state.visible = false
            state.dragX = 0f; state.dragY = 0f
            eventHandler.toggleNavigationBar(true)
        }
    }

    fun endDrag(v: Float) = endDragImpl(v) { close(it) }
    val zoomStates = remember { mutableMapOf<Int, com.github.panpf.zoomimage.SketchZoomState>() }

    // 打开：先读出尺寸，再弹簧展开
    LaunchedEffect(state.openRequest) {
        eventHandler.toggleNavigationBar(false)
        state.current?.let { m -> if (state.sizes[m.id] == null) state.sizes[m.id] = readSize(context, m) }
        state.progress.animateTo(1f, OpenSpring)
        state.settled = true
    }
    BackHandler(enabled = state.visible) { close() }
    BackHandler(enabled = state.visible && card.targetValue > 0f) { scope.launch { card.animateTo(0f, CardSpring) } }

    Box(
        Modifier
            .fillMaxSize()
            .onGloballyPositioned { origin = it.positionInRoot(); box = it.size }
    ) {
        val t = state.progress.value
        val m = state.current ?: return@Box
        if (box == IntSize.Zero) return@Box
        val dragS = 1f - (state.dragY / (box.height * 1.7f)).coerceIn(0f, 0.35f)
        val bgAlpha = t * (1f - min(abs(state.dragY) / with(density) { 500.dp.toPx() }, 0.6f))
        Box(Modifier.fillMaxSize().graphicsLayer { alpha = bgAlpha }.background(androidx.compose.ui.graphics.lerp(Color.Black, paper, card.value)))

        if (state.settled && state.dragY == 0f && state.dragX == 0f) {
            // ---------- 展开后：翻页 + 缩放 ----------
            val pager = rememberPagerState(initialPage = state.index) { state.items.size }
            LaunchedEffect(pager) {
                snapshotFlow { pager.currentPage }.collect { p ->
                    state.index = p
                    state.items.getOrNull(p)?.let { mm -> if (state.sizes[mm.id] == null) state.sizes[mm.id] = readSize(context, mm) }
                }
            }
            HorizontalPager(
                state = pager,
                beyondViewportPageCount = 1,
                key = { state.items[it].id },
                modifier = Modifier.fillMaxSize().graphicsLayer {
                    val c = card.value
                    transformOrigin = TransformOrigin(0.5f, 0f)
                    translationY = c * box.height * 0.06f + with(density) { 24.dp.toPx() } * c
                    val sc = 1f - 0.40f * c; scaleX = sc; scaleY = sc
                }.pointerInput(Unit) {
                    // 在子组件（缩放、翻页）之前判断手势：未放大时，竖向滑动归查看器（上滑开附言、下拉关闭）
                    val slop = viewConfiguration.touchSlop
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                        val zs = zoomStates[pager.currentPage]
                        if ((zs?.zoomable?.userTransform?.scaleX ?: 1f) > 1.01f) return@awaitEachGesture
                        val tracker = VelocityTracker()
                        var total = Offset.Zero
                        var dragging = false
                        while (true) {
                            val ev = awaitPointerEvent(PointerEventPass.Initial)
                            if (ev.changes.size > 1) return@awaitEachGesture // 双指交给缩放
                            val c = ev.changes.first()
                            if (!c.pressed) {
                                if (dragging) endDrag(tracker.calculateVelocity().y)
                                return@awaitEachGesture
                            }
                            val d = c.positionChange()
                            total += d
                            if (!dragging && abs(total.y) > slop && abs(total.y) > abs(total.x) * 1.3f) dragging = true
                            if (dragging) {
                                c.consume()
                                tracker.addPosition(c.uptimeMillis, c.position)
                                state.dragY = (state.dragY + d.y).coerceAtLeast(-with(density) { 160.dp.toPx() })
                            }
                        }
                    }
                }
            ) { page ->
                val pm = state.items[page]
                if (pm.isVideo) HwVideo(pm, active = pager.currentPage == page && card.value < 0.01f, uiAlpha = 1f - card.value)
                else SketchZoomAsyncImage(
                    uri = pm.getUri().toString(),
                    contentDescription = pm.label,
                    zoomState = rememberSketchZoomState().also { zoomStates[page] = it },
                    alignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize(),
                    scrollBar = null,
                )
            }
        } else {
            // ---------- 展开 / 收回 / 下拉中：共享元素图层 ----------
            val from = thumbRect(m); val to = fitRect(m)
            val s0 = max(from.width / to.width, from.height / to.height)
            var s = lerp(s0, 1f, t)
            var cx = lerp(from.center.x, to.center.x, t); var cy = lerp(from.center.y, to.center.y, t)
            var cl = lerp(from.left, to.left, t); var ct = lerp(from.top, to.top, t)
            var cw = lerp(from.width, to.width, t); var ch = lerp(from.height, to.height, t)
            val k = if (state.settled) 1f else t // 收回时拖拽偏移随进度衰减
            val dx = state.dragX * k; val dy = state.dragY * k; val ds = lerp(1f, dragS, k)
            val ncx = cx + dx; val ncy = cy + dy
            cl = ncx + (cl - cx) * ds; ct = ncy + (ct - cy) * ds; cw *= ds; ch *= ds
            s *= ds; cx = ncx; cy = ncy
            val ew = to.width * s; val eh = to.height * s; val ex = cx - ew / 2; val ey = cy - eh / 2
            val il = max(0f, (cl - ex) / s); val itp = max(0f, (ct - ey) / s)
            val ir = max(0f, (ex + ew - cl - cw) / s); val ib = max(0f, (ey + eh - ct - ch) / s)
            val radius = with(density) { 10.dp.toPx() } * (1f - t) / s
            Box(
                Modifier
                    .size(with(density) { to.width.toDp() }, with(density) { to.height.toDp() })
                    .graphicsLayer {
                        transformOrigin = TransformOrigin(0f, 0f)
                        translationX = ex; translationY = ey
                        scaleX = s; scaleY = s
                        clip = true
                        shape = InsetShape(il, itp, ir, ib, radius)
                    }
            ) {
                AsyncImage(
                    request = ComposableImageRequest(m.getUri().toString()),
                    contentDescription = m.label,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // ---------- 附言卡（照片背面） ----------
        if (card.value > 0.001f || card.targetValue > 0f) {
            val h = box.height * 0.64f
            HwNoteCard(
                media = m,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .height(with(density) { h.toDp() })
                    .graphicsLayer { translationY = (1f - card.value) * h }
            )
        }

        // ---------- 顶部信息 + 底部操作（展开稳定后淡入） ----------
        val uiAlpha = if (state.settled && state.dragY == 0f) 1f - card.value else 0f
        val date = Instant.ofEpochSecond(m.definedTimestamp).atZone(ZoneId.systemDefault()).toLocalDate()
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 20.dp, vertical = 14.dp).graphicsLayer { alpha = uiAlpha },
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("${date.year}.${date.monthValue}.${"%02d".format(date.dayOfMonth)}", color = Color.White, fontSize = 13.sp, letterSpacing = 0.15.em)
            Text(m.albumLabel, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp, letterSpacing = 0.15.em)
        }
        Row(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().navigationBarsPadding().padding(bottom = 18.dp)
                .graphicsLayer { alpha = uiAlpha },
            horizontalArrangement = Arrangement.Center
        ) {
            val fav = favOverride[m.id] ?: (m.favorite == 1)
            ViewerAction("附言", dot = m.id in withNotes) { scope.launch { card.animateTo(1f, CardSpring) } }
            ViewerAction(if (fav) "已收藏" else "收藏", tint = if (fav) Color(0xFFE8A99F) else Color.White) {
                favOverride[m.id] = !fav
                scope.launch { handler.toggleFavorite(favoriteRequest, listOf(m), !fav) }
            }
            if (!m.isVideo) ViewerAction("编辑") { com.dot.gallery.app.presentation.edit.EditActivity.launchEditor(context, m.getUri()) }
            ViewerAction("删除") {
                pendingTrash = m
                scope.launch {
                    when (handler.trashMedia(trashRequest, listOf(m), true)) {
                        com.dot.gallery.data.repository.MediaMutationResult.COMPLETED -> removeCurrent()
                        else -> Unit // 需要系统确认时，确认后在回调里移除
                    }
                }
            }
            val paperColor = MaterialTheme.colorScheme.background
            Box {
                ViewerAction("更多") { moreOpen = true }
                androidx.compose.material3.DropdownMenu(expanded = moreOpen, onDismissRequest = { moreOpen = false }) {
                    androidx.compose.material3.DropdownMenuItem(text = { Text("分享") }, onClick = {
                        moreOpen = false
                        val i = Intent(Intent.ACTION_SEND).apply {
                            type = m.mimeType
                            putExtra(Intent.EXTRA_STREAM, m.getUri())
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(i, null))
                    })
                    if (!m.isVideo && m.id in withNotes) androidx.compose.material3.DropdownMenuItem(text = { Text("分享附言卡") }, onClick = {
                        moreOpen = false
                        val dark = paperColor.luminance() < 0.4f
                        scope.launch {
                            val n = notesRepo.noteFlow(m.id).first()
                            if (n != null) HwShareCard.share(context, m, n.text, parseFields(n.fieldsJson), dark)
                        }
                    })
                    if (!m.isVideo) androidx.compose.material3.DropdownMenuItem(text = { Text("设为壁纸 / 联系人头像") }, onClick = {
                        moreOpen = false
                        val i = Intent(Intent.ACTION_ATTACH_DATA).apply {
                            setDataAndType(m.getUri(), m.mimeType)
                            putExtra("mimeType", m.mimeType)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(i, null))
                    })
                    if (!m.isVideo) androidx.compose.material3.DropdownMenuItem(text = { Text("向右旋转") }, onClick = {
                        moreOpen = false
                        scope.launch { handler.rotateImage(m, 90) }
                    })
                    androidx.compose.material3.DropdownMenuItem(text = { Text("用其他应用打开") }, onClick = {
                        moreOpen = false
                        val i = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(m.getUri(), m.mimeType)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(i, null))
                    })
                }
            }
        }
    }
}

@Composable
private fun ViewerAction(label: String, dot: Boolean = false, tint: Color = Color.White, onClick: () -> Unit) {
    Box(Modifier.clickable(onClick = onClick).padding(horizontal = 13.dp, vertical = 10.dp)) {
        Text(label, color = tint, fontSize = 15.sp, letterSpacing = 0.15.em)
        // 已有附言：右上角一个朱红小点
        if (dot) Box(Modifier.align(Alignment.TopEnd).offset(x = 7.dp, y = (-2).dp).size(5.dp).background(Color(0xFFB22A20), androidx.compose.foundation.shape.CircleShape))
    }
}
