/*
 * SPDX-FileCopyrightText: 2026 手写相册 contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.dot.gallery.app.presentation.hw

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.dot.gallery.data.InkPoint
import com.dot.gallery.data.InkStroke

/*
 * 手写：
 * - 写字板固定 3:2；点坐标归一化到 0..1，笔宽以“写字板宽 = 360 单位”计，所以缩小显示时比例不变；
 * - 手指写：写得快笔画细、写得慢笔画粗；手写笔：按压力定粗细；
 * - 每一笔记着自己的颜色代号（0 = 墨色，1 = 主题强调色），换主题时笔迹跟着变。
 */
const val INK_ASPECT = 1.5f
private const val REF_W = 360f
private const val W_MIN = 1.1f
private const val W_MAX = 3.6f

@Composable
fun inkColor(code: Int): Color = if (code == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground

/** 画出前 [upTo] 个点（用于回放）。 */
fun DrawScope.drawInk(strokes: List<InkStroke>, colorOf: (Int) -> Color, upTo: Int = Int.MAX_VALUE) {
    val scale = size.width / REF_W
    var drawn = 0
    for (s in strokes) {
        val pts = s.points
        if (pts.isEmpty()) continue
        val color = colorOf(s.color)
        if (drawn >= upTo) return
        val first = pts[0]
        if (pts.size == 1) {
            drawCircle(color, first.w * scale / 2f, Offset(first.x * size.width, first.y * size.height))
            drawn++
            continue
        }
        drawn++
        for (i in 1 until pts.size) {
            if (drawn >= upTo) return
            val a = pts[i - 1]; val b = pts[i]
            drawLine(
                color, Offset(a.x * size.width, a.y * size.height), Offset(b.x * size.width, b.y * size.height),
                strokeWidth = (a.w + b.w) / 2f * scale, cap = StrokeCap.Round
            )
            drawn++
        }
    }
}

/** 只读的笔迹。[replay] 每加一，就按书写顺序重写一遍。 */
@Composable
fun HwInkView(strokes: List<InkStroke>, modifier: Modifier = Modifier, replay: Int = 0) {
    val ink = inkColor(0); val accent = inkColor(1)
    val color: (Int) -> Color = { if (it == 1) accent else ink }
    val total = remember(strokes) { strokes.sumOf { it.points.size } }
    val progress = remember { Animatable(1f) }
    LaunchedEffect(replay) {
        if (replay > 0 && total > 0) {
            val ms = strokes.sumOf { it.points.lastOrNull()?.t ?: 0 }.coerceIn(500, 4000)
            progress.snapTo(0f)
            progress.animateTo(1f, tween(ms, easing = LinearEasing))
        }
    }
    Canvas(modifier.aspectRatio(INK_ASPECT)) {
        drawInk(strokes, color, if (progress.value >= 1f) Int.MAX_VALUE else (total * progress.value).toInt())
    }
}

/** 全屏写字板。 */
@Composable
fun HwInkPad(initial: List<InkStroke>, initialColor: Int, onCancel: () -> Unit, onDone: (List<InkStroke>, Int) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val strokes = remember { mutableStateListOf<InkStroke>().apply { addAll(initial) } }
    val current = remember { mutableStateListOf<InkPoint>() }
    var colorCode by remember { mutableIntStateOf(initialColor) }
    val ink = inkColor(0); val accent = inkColor(1)
    val color: (Int) -> Color = { if (it == 1) accent else ink }
    val penColor by androidx.compose.runtime.rememberUpdatedState(colorCode)
    BackHandler { onCancel() }

    Column(Modifier.fillMaxSize().background(colors.background).statusBarsPadding().navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("取消", color = colors.onSurfaceVariant, modifier = Modifier.clickable(onClick = onCancel).padding(10.dp))
            Spacer(Modifier.weight(1f))
            Text("完成", color = colors.primary, fontWeight = FontWeight.SemiBold, letterSpacing = 0.15.em,
                modifier = Modifier.clickable { onDone(strokes.toList(), colorCode) }.padding(10.dp))
        }
        Box(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 18.dp), contentAlignment = Alignment.Center) {
            Canvas(
                Modifier.fillMaxWidth().aspectRatio(INK_ASPECT)
                    .border(0.6.dp, colors.outline)
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            val stylus = down.type == PointerType.Stylus
                            val t0 = down.uptimeMillis
                            var w = if (stylus) W_MIN + down.pressure.coerceIn(0f, 1f) * (W_MAX - W_MIN) else 2.6f
                            var last = down.position
                            var lastT = t0
                            current.clear()
                            current.add(InkPoint(last.x / size.width, last.y / size.height, w, 0))
                            down.consume()
                            while (true) {
                                val e = awaitPointerEvent()
                                val c = e.changes.firstOrNull { it.id == down.id } ?: break
                                if (!c.pressed) break
                                val samples = c.historical.map { it.position to it.uptimeMillis } + (c.position to c.uptimeMillis)
                                for ((pos, t) in samples) {
                                    val dist = (pos - last).getDistance()
                                    if (dist < 1.5f) continue
                                    val target = if (stylus) W_MIN + c.pressure.coerceIn(0f, 1f) * (W_MAX - W_MIN)
                                    else {
                                        val speed = dist / (t - lastT).coerceAtLeast(1) / density // dp / ms
                                        (W_MAX - speed * 1.3f).coerceIn(W_MIN, W_MAX)
                                    }
                                    w += (target - w) * 0.3f
                                    current.add(
                                        InkPoint((pos.x / size.width).coerceIn(0f, 1f), (pos.y / size.height).coerceIn(0f, 1f), w, (t - t0).toInt())
                                    )
                                    last = pos; lastT = t
                                }
                                c.consume()
                            }
                            if (current.isNotEmpty()) strokes.add(InkStroke(current.toList(), penColor))
                            current.clear()
                        }
                    }
            ) {
                drawInk(strokes, color)
                if (current.isNotEmpty()) drawInk(listOf(InkStroke(current, colorCode)), color)
            }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            listOf(0, 1).forEach { code ->
                Box(
                    Modifier.padding(end = 14.dp).size(30.dp)
                        .border(if (colorCode == code) 1.4.dp else 0.dp, if (colorCode == code) colors.onBackground else Color.Transparent, CircleShape)
                        .clickable { colorCode = code },
                    contentAlignment = Alignment.Center
                ) { Box(Modifier.size(18.dp).background(inkColor(code), CircleShape)) }
            }
            Spacer(Modifier.weight(1f))
            val has = strokes.isNotEmpty()
            Text("撤销", color = if (has) colors.onBackground else colors.outline, fontFamily = FontFamily.Serif, fontSize = 15.sp, letterSpacing = 0.12.em,
                modifier = Modifier.clickable(enabled = has) { strokes.removeAt(strokes.lastIndex) }.padding(10.dp))
            Spacer(Modifier.width(6.dp))
            Text("清空", color = if (has) colors.onBackground else colors.outline, fontFamily = FontFamily.Serif, fontSize = 15.sp, letterSpacing = 0.12.em,
                modifier = Modifier.clickable(enabled = has) { strokes.clear() }.padding(10.dp))
        }
    }
}
