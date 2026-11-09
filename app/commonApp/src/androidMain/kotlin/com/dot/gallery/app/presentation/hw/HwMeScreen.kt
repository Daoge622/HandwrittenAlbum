/*
 * SPDX-FileCopyrightText: 2026 手写相册 contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.dot.gallery.app.presentation.hw

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.dot.gallery.core.LocalEventHandler
import com.dot.gallery.core.navigate
import com.dot.gallery.core.presentation.util.Screen
import com.dot.gallery.core.ui.settings.rememberPreference
import com.dot.gallery.domain.settings.SettingsKeys
import com.dot.gallery.ui.theme.THEME_SEED_SUJIAN
import com.dot.gallery.ui.theme.THEME_SEED_SYSTEM
import kotlinx.coroutines.launch
import kotlin.math.hypot

/*
 * 手写相册「我」：主题（换主题时从点击处墨晕展开）、常用入口、设置、创作人信息。
 * 回收站 / 收藏 / 私密相册 / 隐藏相册 目前仍进入内核页面，后续逐个重做界面。
 */

private data class ThemeChoice(val key: String, val name: String, val desc: String, val bg: Color, val ink: Color, val accent: Color)

private val Choices = listOf(
    ThemeChoice("sujian", "素笺", "随系统明暗", Color(0xFFF6F3EC), Color(0xFF1C1C1A), Color(0xFFB22A20)),
    ThemeChoice("moye", "墨夜", "始终深色", Color(0xFF0F1012), Color(0xFFECE8DC), Color(0xFFD4B47A)),
    ThemeChoice("paper", "白昼", "始终浅色", Color(0xFFF6F3EC), Color(0xFF1C1C1A), Color(0xFFB22A20)),
    ThemeChoice("system", "壁纸取色", "跟随系统配色", Color(0xFFDDE5DA), Color(0xFF1B1C19), Color(0xFF4C6A4F)),
)

private val InkEase = CubicBezierEasing(0.65f, 0f, 0.15f, 1f)

@Composable
fun HwMeScreen(paddingValues: PaddingValues) {
    val context = LocalContext.current
    val eventHandler = LocalEventHandler.current
    val scope = rememberCoroutineScope()
    val colors = MaterialTheme.colorScheme

    var seed by rememberPreference(SettingsKeys.themeColorSeed)
    var force by rememberPreference(SettingsKeys.forceTheme)
    var dark by rememberPreference(SettingsKeys.darkMode)
    val current = when {
        seed == THEME_SEED_SYSTEM -> "system"
        seed == THEME_SEED_SUJIAN && force && dark -> "moye"
        seed == THEME_SEED_SUJIAN && force && !dark -> "paper"
        else -> "sujian"
    }

    // 墨晕：先把当前画面录下来盖在上面，换主题后在覆盖层上从点击处挖出一个不断扩大的圆
    val layer = rememberGraphicsLayer()
    var snapshot by remember { mutableStateOf<ImageBitmap?>(null) }
    val reveal = remember { Animatable(0f) }
    var center by remember { mutableStateOf(Offset.Zero) }

    fun apply(key: String) {
        when (key) {
            "sujian" -> { seed = THEME_SEED_SUJIAN; force = false }
            "moye" -> { seed = THEME_SEED_SUJIAN; force = true; dark = true }
            "paper" -> { seed = THEME_SEED_SUJIAN; force = true; dark = false }
            "system" -> { seed = THEME_SEED_SYSTEM; force = false }
        }
    }

    fun choose(key: String, at: Offset) {
        if (key == current || snapshot != null) return
        scope.launch {
            snapshot = layer.toImageBitmap()
            center = at
            reveal.snapTo(0f)
            apply(key)
            reveal.animateTo(1f, tween(720, easing = InkEase))
            snapshot = null
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .drawWithContent { layer.record { this@drawWithContent.drawContent() }; drawLayer(layer) }
                .background(colors.background)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, bottom = paddingValues.calculateBottomPadding() + 120.dp)
        ) {
            Text("我", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 28.sp, modifier = Modifier.padding(top = 22.dp))

            Section("主题")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Choices.forEach { c ->
                    var origin by remember { mutableStateOf(Offset.Zero) }
                    Column(
                        Modifier.weight(1f)
                            .onGloballyPositioned { origin = it.boundsInRoot().topLeft }
                            .pointerInput(c.key, current) { detectTapGestures { p -> choose(c.key, origin + p) } }
                    ) {
                        Box(
                            Modifier.fillMaxWidth().aspectRatio(0.72f)
                                .background(c.bg, RoundedCornerShape(6.dp))
                                .border(if (c.key == current) 1.6.dp else 0.6.dp, if (c.key == current) colors.primary else colors.outline, RoundedCornerShape(6.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Box(Modifier.fillMaxWidth(0.7f).height(5.dp).background(c.ink, RoundedCornerShape(2.dp)))
                                Spacer(Modifier.height(5.dp))
                                Box(Modifier.fillMaxWidth(0.45f).height(3.dp).background(c.ink.copy(alpha = 0.35f), RoundedCornerShape(2.dp)))
                            }
                            Box(Modifier.align(Alignment.BottomEnd).size(10.dp).background(c.accent, RoundedCornerShape(2.dp)))
                        }
                        Text(c.name, fontFamily = FontFamily.Serif, fontWeight = if (c.key == current) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
                        Text(c.desc, style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                    }
                }
            }

            Section("相册")
            Entry("收藏") { eventHandler.navigate(Screen.FavoriteScreen()) }
            Entry("回收站") { eventHandler.navigate(Screen.TrashedScreen()) }
            Entry("私密相册") { eventHandler.navigate(Screen.VaultScreen()) }
            Entry("隐藏的相册") { eventHandler.navigate(Screen.IgnoredScreen()) }

            Section("设置")
            Entry("更多设置") { eventHandler.navigate(Screen.SettingsScreen()) }

            Section("创作人信息")
            Text(
                "手写相册是开源、免费的离线相册，不申请联网权限。\n\n" +
                    "底座：ReFra（IacobIonut01，Apache-2.0）\n" +
                    "字体：Cormorant Garamond（SIL OFL）\n" +
                    "图标与美术：手写相册原创（CC0）\n" +
                    "播放与解码：AndroidX Media3、Sketch、ZoomImage（Apache-2.0）",
                style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, modifier = Modifier.padding(top = 12.dp)
            )
            Text(
                "源码 github.com/Daoge622/HandwrittenAlbum",
                style = MaterialTheme.typography.bodySmall, color = colors.primary,
                modifier = Modifier.padding(top = 10.dp).clickable {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Daoge622/HandwrittenAlbum")))
                }
            )
        }

        snapshot?.let { img ->
            Canvas(Modifier.fillMaxSize()) {
                val r = hypot(maxOf(center.x, size.width - center.x), maxOf(center.y, size.height - center.y)) * reveal.value
                val hole = Path().apply { addOval(androidx.compose.ui.geometry.Rect(center, r)) }
                clipPath(hole, clipOp = ClipOp.Difference) { drawImage(img) }
            }
        }
    }
}

@Composable
private fun Section(title: String) {
    Column(Modifier.padding(top = 32.dp)) {
        Text(title, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 0.5.em)
        Spacer(Modifier.height(10.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
        Spacer(Modifier.height(14.dp))
    }
}

@Composable
private fun Entry(label: String, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.fillMaxWidth().padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, fontSize = 16.sp)
            Spacer(Modifier.weight(1f))
            Text("›", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
    }
}
