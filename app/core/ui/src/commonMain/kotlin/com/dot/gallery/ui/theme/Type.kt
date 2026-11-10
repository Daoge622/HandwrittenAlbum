/*
 * SPDX-FileCopyrightText: 2023-2026 IacobIacob01
 * SPDX-License-Identifier: Apache-2.0
 */

package com.dot.gallery.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily

/*
 * 手写相册：不再内置 Google Sans（约 14 MB × 2 份），沿用系统字体——
 * 用户在系统里换的字体（比如手写体）会自然贯穿整个应用。
 */
val GoogleSansFontFamily: FontFamily
    @Composable
    get() = FontFamily.Default

private val defaultTypography = Typography()

val Typography: Typography
    @Composable
    get() = Typography(
        displayLarge = defaultTypography.displayLarge.copy(fontFamily = GoogleSansFontFamily),
        displayMedium = defaultTypography.displayMedium.copy(fontFamily = GoogleSansFontFamily),
        displaySmall = defaultTypography.displaySmall.copy(fontFamily = GoogleSansFontFamily),
        headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = GoogleSansFontFamily),
        headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = GoogleSansFontFamily),
        headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = GoogleSansFontFamily),
        titleLarge = defaultTypography.titleLarge.copy(fontFamily = GoogleSansFontFamily),
        titleMedium = defaultTypography.titleMedium.copy(fontFamily = GoogleSansFontFamily),
        titleSmall = defaultTypography.titleSmall.copy(fontFamily = GoogleSansFontFamily),
        bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = GoogleSansFontFamily),
        bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = GoogleSansFontFamily),
        bodySmall = defaultTypography.bodySmall.copy(fontFamily = GoogleSansFontFamily),
        labelLarge = defaultTypography.labelLarge.copy(fontFamily = GoogleSansFontFamily),
        labelMedium = defaultTypography.labelMedium.copy(fontFamily = GoogleSansFontFamily),
        labelSmall = defaultTypography.labelSmall.copy(fontFamily = GoogleSansFontFamily),
    )

val SystemTypography = Typography()
