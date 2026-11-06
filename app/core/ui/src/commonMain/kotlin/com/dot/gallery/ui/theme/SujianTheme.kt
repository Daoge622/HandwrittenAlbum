/*
 * SPDX-FileCopyrightText: 2026 手写相册 contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.dot.gallery.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/*
 * 手写相册「素笺」主题（默认主题）。设计方向见 docs/08「画报」：
 * - 浅色：暖白纸色、墨色文字、朱红只作唯一强调色；层级靠细线和留白，不靠彩色容器。
 * - 深色即「墨夜」：近黑底、象牙白文字、香槟金强调。
 * - 标题用系统宋体（Android 自带 Noto Serif CJK，不额外打包字库），正文用系统无衬线。
 */

const val THEME_SEED_SUJIAN = "sujian"

private object Sujian {
    val paper = Color(0xFFF6F3EC)
    val paper2 = Color(0xFFF1EDE4)
    val paper3 = Color(0xFFEBE6DB)
    val paper4 = Color(0xFFE4DED1)
    val ink = Color(0xFF1C1C1A)
    val ink2 = Color(0xFF6E6A62)
    val hair = Color(0xFFD6D0C4)
    val hair2 = Color(0xFFE6E0D4)
    val vermilion = Color(0xFFB22A20)
    val vermilionSoft = Color(0xFFF3DED9)
    val sage = Color(0xFF5E7A6E)
    val sageSoft = Color(0xFFDCE5DF)
    val ochre = Color(0xFFC9785B)
}

private object Moye {
    val night = Color(0xFF0F1012)
    val night2 = Color(0xFF15161A)
    val night3 = Color(0xFF1B1D22)
    val night4 = Color(0xFF22252B)
    val ivory = Color(0xFFECE8DC)
    val ivory2 = Color(0xFF9A978F)
    val hair = Color(0xFF2C2F36)
    val gold = Color(0xFFD4B47A)
    val goldDeep = Color(0xFF3A3122)
    val sage = Color(0xFF9DB5AA)
}

private val SujianLight: ColorScheme = lightColorScheme(
    primary = Sujian.vermilion,
    onPrimary = Color(0xFFFAF0E6),
    primaryContainer = Sujian.vermilionSoft,
    onPrimaryContainer = Color(0xFF4A0F0A),
    inversePrimary = Color(0xFFFFB4A8),
    secondary = Sujian.sage,
    onSecondary = Color.White,
    secondaryContainer = Sujian.paper3,
    onSecondaryContainer = Sujian.ink,
    tertiary = Sujian.ochre,
    onTertiary = Color.White,
    tertiaryContainer = Sujian.sageSoft,
    onTertiaryContainer = Color(0xFF1E2B25),
    background = Sujian.paper,
    onBackground = Sujian.ink,
    surface = Sujian.paper,
    onSurface = Sujian.ink,
    surfaceVariant = Sujian.paper3,
    onSurfaceVariant = Sujian.ink2,
    surfaceTint = Color.Transparent,
    inverseSurface = Sujian.ink,
    inverseOnSurface = Sujian.paper,
    outline = Sujian.hair,
    outlineVariant = Sujian.hair2,
    scrim = Color.Black,
    surfaceBright = Sujian.paper,
    surfaceDim = Sujian.paper4,
    surfaceContainerLowest = Color(0xFFFBF9F4),
    surfaceContainerLow = Sujian.paper2,
    surfaceContainer = Sujian.paper2,
    surfaceContainerHigh = Sujian.paper3,
    surfaceContainerHighest = Sujian.paper4,
)

private val SujianDark: ColorScheme = darkColorScheme(
    primary = Moye.gold,
    onPrimary = Color(0xFF241B0C),
    primaryContainer = Moye.goldDeep,
    onPrimaryContainer = Color(0xFFF2E3C3),
    inversePrimary = Color(0xFF7A5F2E),
    secondary = Moye.sage,
    onSecondary = Color(0xFF13201B),
    secondaryContainer = Moye.night4,
    onSecondaryContainer = Moye.ivory,
    tertiary = Color(0xFFE0A58C),
    onTertiary = Color(0xFF2B140B),
    tertiaryContainer = Moye.night4,
    onTertiaryContainer = Moye.ivory,
    background = Moye.night,
    onBackground = Moye.ivory,
    surface = Moye.night,
    onSurface = Moye.ivory,
    surfaceVariant = Moye.night3,
    onSurfaceVariant = Moye.ivory2,
    surfaceTint = Color.Transparent,
    inverseSurface = Moye.ivory,
    inverseOnSurface = Moye.night,
    outline = Moye.hair,
    outlineVariant = Color(0xFF24272D),
    scrim = Color.Black,
    surfaceBright = Moye.night4,
    surfaceDim = Moye.night,
    surfaceContainerLowest = Color(0xFF0A0B0D),
    surfaceContainerLow = Moye.night2,
    surfaceContainer = Moye.night2,
    surfaceContainerHigh = Moye.night3,
    surfaceContainerHighest = Moye.night4,
)

fun sujianColorScheme(isDark: Boolean, amoled: Boolean): ColorScheme =
    if (isDark) SujianDark.maybeAmoled(amoled) else SujianLight

/** 「画报」排版：标题宋体、字距略收；正文系统无衬线，行高放松。 */
val EditorialTypography: Typography = Typography().let { t ->
    val serif = FontFamily.Serif
    val sans = FontFamily.Default
    Typography(
        displayLarge = t.displayLarge.copy(fontFamily = serif, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.01).em),
        displayMedium = t.displayMedium.copy(fontFamily = serif, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.01).em),
        displaySmall = t.displaySmall.copy(fontFamily = serif, fontWeight = FontWeight.SemiBold),
        headlineLarge = t.headlineLarge.copy(fontFamily = serif, fontWeight = FontWeight.SemiBold),
        headlineMedium = t.headlineMedium.copy(fontFamily = serif, fontWeight = FontWeight.SemiBold),
        headlineSmall = t.headlineSmall.copy(fontFamily = serif, fontWeight = FontWeight.SemiBold),
        titleLarge = t.titleLarge.copy(fontFamily = serif, fontWeight = FontWeight.SemiBold, fontSize = 21.sp),
        titleMedium = t.titleMedium.copy(fontFamily = serif, fontWeight = FontWeight.SemiBold),
        titleSmall = t.titleSmall.copy(fontFamily = sans, fontWeight = FontWeight.Medium, letterSpacing = 0.04.em),
        bodyLarge = t.bodyLarge.copy(fontFamily = sans, lineHeight = 26.sp),
        bodyMedium = t.bodyMedium.copy(fontFamily = sans, lineHeight = 22.sp),
        bodySmall = t.bodySmall.copy(fontFamily = sans, lineHeight = 18.sp),
        labelLarge = t.labelLarge.copy(fontFamily = sans, letterSpacing = 0.06.em),
        labelMedium = t.labelMedium.copy(fontFamily = sans, letterSpacing = 0.08.em),
        labelSmall = t.labelSmall.copy(fontFamily = sans, letterSpacing = 0.1.em),
    )
}
