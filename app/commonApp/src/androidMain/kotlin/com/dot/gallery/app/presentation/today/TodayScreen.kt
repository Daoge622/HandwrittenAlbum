/*
 * SPDX-FileCopyrightText: 2026 手写相册 contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.dot.gallery.app.presentation.today

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.dot.gallery.core.LocalEventHandler
import com.dot.gallery.core.navigate
import com.dot.gallery.core.presentation.util.Screen
import com.dot.gallery.domain.model.MediaState
import com.dot.gallery.domain.model.UriMedia
import com.dot.gallery.domain.model.definedTimestamp
import com.dot.gallery.domain.util.getUri
import com.github.panpf.sketch.AsyncImage
import com.github.panpf.sketch.request.ComposableImageRequest
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

/*
 * 手写相册「今日」首页（画报方向，见 docs/08）。
 * - 最新一张照片全出血铺满首屏，随滚动轻微视差；
 * - 巨大的日期数字随滚动缩小、淡出，收进顶部小刊头；
 * - 本周：杂志式不对称拼排（1 大 2 小）；
 * - 那年今日：往年同月同日的照片。
 * 动效只改 graphicsLayer（transform / alpha），不触发重新布局。
 */

// 日期数字专用字体：Cormorant Garamond（SIL OFL），只含数字和拉丁字母，固定中等字重，约 27KB
private val DateFamily = FontFamily(androidx.compose.ui.text.font.Font(com.dot.gallery.R.font.hw_cormorant_medium))

private val zone: ZoneId = ZoneId.systemDefault()
private fun UriMedia.localDate(): LocalDate = Instant.ofEpochSecond(definedTimestamp).atZone(zone).toLocalDate()

private val CN_MONTH = arrayOf("一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月")
private val CN_WEEK = arrayOf("星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日")

@Composable
fun TodayScreen(
    paddingValues: PaddingValues,
    mediaState: State<MediaState<UriMedia>>,
) {
    val eventHandler = LocalEventHandler.current
    val media = mediaState.value.media
    val today = remember { LocalDate.now(zone) }
    val open: (UriMedia) -> Unit = { eventHandler.navigate(Screen.MediaViewScreen.idAndAlbum(it.id, -1L)) }

    val hero = media.firstOrNull()
    val weekStart = today.minusDays(6)
    val thisWeek = remember(media) {
        media.drop(1).filter { !it.localDate().isBefore(weekStart) }.take(3)
            .ifEmpty { media.drop(1).take(3) }
    }
    val weekCount = remember(media) { media.count { !it.localDate().isBefore(weekStart) } }
    val onThisDay = remember(media) {
        media.filter { val d = it.localDate(); d.monthValue == today.monthValue && d.dayOfMonth == today.dayOfMonth && d.year < today.year }
            .groupBy { it.localDate().year }.toSortedMap(compareByDescending { it })
            .map { (year, list) -> year to list.first() }
    }

    val listState = rememberLazyListState()
    val density = LocalDensity.current

    BoxWithConstraints(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        val heroHeight = maxHeight * 0.80f
        val heroPx = with(density) { heroHeight.toPx() }
        // 首屏滚动进度 0..1（只在第一项可见时有意义）
        val progress by remember {
            derivedStateOf {
                if (listState.firstVisibleItemIndex > 0) 1f
                else (listState.firstVisibleItemScrollOffset / (heroPx * 0.75f)).coerceIn(0f, 1f)
            }
        }
        val scrollPx by remember {
            derivedStateOf { if (listState.firstVisibleItemIndex > 0) heroPx else listState.firstVisibleItemScrollOffset.toFloat() }
        }

        LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
            // ---------- 首屏：最新照片 ----------
            item(key = "hero") {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(heroHeight)
                        .clip(RoundedCornerShape(0.dp))
                        .background(Color(0xFF222222))
                ) {
                    if (hero != null) {
                        AsyncImage(
                            request = ComposableImageRequest(hero.getUri().toString()),
                            contentDescription = hero.label,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    translationY = scrollPx * 0.35f
                                    val s = 1f + progress * 0.06f
                                    scaleX = s; scaleY = s
                                }
                                .clickable(remember { MutableInteractionSource() }, indication = null) { open(hero) }
                        )
                    }
                    Box(Modifier.fillMaxWidth().height(heroHeight * 0.28f).background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.45f), Color.Transparent))))
                    Box(
                        Modifier.fillMaxWidth().height(heroHeight * 0.55f).align(Alignment.BottomStart)
                            .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f))))
                    )
                    // 刊头
                    Column(Modifier.statusBarsPadding().padding(horizontal = 24.dp, vertical = 14.dp)) {
                        Text("手写相册", color = Color.White, fontSize = 12.sp, letterSpacing = 0.55.em, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Serif)
                        Spacer(Modifier.height(10.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.55f), thickness = 0.5.dp)
                    }
                    // 巨大的日期
                    Column(
                        Modifier.align(Alignment.BottomStart).padding(start = 18.dp, bottom = 24.dp)
                            .graphicsLayer {
                                transformOrigin = TransformOrigin(0f, 1f)
                                translationY = -scrollPx * 0.15f
                                val s = 1f - progress * 0.55f
                                scaleX = s; scaleY = s
                                alpha = 1f - ((progress - 0.55f) / 0.35f).coerceIn(0f, 1f)
                            }
                    ) {
                        Text(
                            text = "%02d".format(today.dayOfMonth),
                            color = Color.White,
                            fontFamily = DateFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 150.sp,
                            lineHeight = 132.sp,
                            letterSpacing = (-0.02).em,
                        )
                        Text(
                            text = "${CN_MONTH[today.monthValue - 1]} · ${CN_WEEK[today.dayOfWeek.value - 1]}",
                            color = Color.White,
                            fontSize = 13.sp,
                            letterSpacing = 0.2.em,
                            modifier = Modifier.padding(start = 6.dp).graphicsLayer { alpha = 1f - (progress / 0.5f).coerceIn(0f, 1f) }
                        )
                    }
                }
            }

            // ---------- 本周 ----------
            if (thisWeek.isNotEmpty()) item(key = "week") {
                Column(Modifier.padding(start = 24.dp, end = 24.dp, top = 34.dp)) {
                    SectionHead(title = "本周", trailing = "$weekCount 张")
                    Spacer(Modifier.height(18.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Photo(thisWeek[0], open, Modifier.weight(1.65f).aspectRatio(3f / 4f))
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                            thisWeek.getOrNull(1)?.let { Photo(it, open, Modifier.fillMaxWidth().aspectRatio(1f)) }
                            thisWeek.getOrNull(2)?.let { Photo(it, open, Modifier.fillMaxWidth().aspectRatio(1f)) }
                        }
                    }
                    val d = thisWeek[0].localDate()
                    Text(
                        "${d.monthValue}.${"%02d".format(d.dayOfMonth)} · ${thisWeek[0].albumLabel}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }

            // ---------- 那年今日 ----------
            if (onThisDay.isNotEmpty()) item(key = "onthisday") {
                Column(Modifier.padding(top = 38.dp)) {
                    Box(Modifier.padding(horizontal = 24.dp)) { SectionHead(title = "那年今日", trailing = "${today.monthValue} 月 ${today.dayOfMonth} 日") }
                    Spacer(Modifier.height(18.dp))
                    LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(onThisDay, key = { it.first }) { (year, m) ->
                            Column(Modifier.width(150.dp)) {
                                Photo(m, open, Modifier.fillMaxWidth().aspectRatio(4f / 5f))
                                Text(
                                    "${today.year - year} 年前",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(top = 10.dp)
                                )
                                Text("$year", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            item(key = "bottom") { Spacer(Modifier.height(paddingValues.calculateBottomPadding() + 120.dp)) }
        }

        // ---------- 收起后的小刊头 ----------
        val dockAlpha = ((progress - 0.8f) / 0.2f).coerceIn(0f, 1f)
        if (dockAlpha > 0f) {
            Column(
                Modifier.fillMaxWidth()
                    .graphicsLayer { alpha = dockAlpha; translationY = (1f - dockAlpha) * -8f * density.density }
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.94f))
                    .statusBarsPadding()
            ) {
                Row(Modifier.padding(horizontal = 24.dp, vertical = 12.dp), verticalAlignment = Alignment.Bottom) {
                    Text("%02d".format(today.dayOfMonth), fontFamily = DateFamily, fontWeight = FontWeight.SemiBold, fontSize = 26.sp, lineHeight = 26.sp)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "${CN_MONTH[today.monthValue - 1]} · ${CN_WEEK[today.dayOfWeek.value - 1]}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
            }
        }
    }
}

@Composable
private fun SectionHead(title: String, trailing: String) {
    Column {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            Text(title, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 0.5.em)
            Spacer(Modifier.weight(1f))
            Text(trailing, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
    }
}

@Composable
private fun Photo(media: UriMedia, open: (UriMedia) -> Unit, modifier: Modifier) {
    Box(modifier.background(MaterialTheme.colorScheme.surfaceVariant).clickable { open(media) }) {
        AsyncImage(
            request = ComposableImageRequest(media.getUri().toString()),
            contentDescription = media.label,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}
