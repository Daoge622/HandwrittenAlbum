/*
 * SPDX-FileCopyrightText: 2026 手写相册 contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.dot.gallery.app.presentation.hw

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.dot.gallery.core.AppConstants
import com.dot.gallery.core.presentation.components.util.MediaAccessState
import com.dot.gallery.core.presentation.components.util.mediaAccessState
import com.dot.gallery.core.presentation.util.RepeatOnResume
import com.dot.gallery.core.ui.settings.rememberPreference
import com.dot.gallery.domain.settings.SettingsKeys
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

/*
 * 第一次打开：一页纸。只要一个权限——读照片；给了就进「今日」。
 * 被拒绝两次后系统不再弹窗，按钮改成去系统设置里打开。
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HwWelcomeScreen(onComplete: () -> Unit) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val colors = MaterialTheme.colorScheme
    var completedVersion by rememberPreference(SettingsKeys.setupCompletedVersion)
    var access by remember { mutableStateOf(context.mediaAccessState()) }
    var asked by remember { mutableIntStateOf(0) }
    val perms = rememberMultiplePermissionsState(AppConstants.PERMISSIONS) {
        access = context.mediaAccessState(); asked++
    }
    RepeatOnResume { access = context.mediaAccessState() }

    val granted = access != MediaAccessState.NONE
    LaunchedEffect(granted) {
        if (granted) { completedVersion = 1; onComplete() }
    }
    // 拒绝过一次且系统不再给理由 → 只能去设置
    val blocked = asked >= 1 && !granted && !perms.shouldShowRationale

    Box(Modifier.fillMaxSize().background(colors.background).statusBarsPadding().navigationBarsPadding()) {
        Column(Modifier.align(Alignment.CenterStart).padding(horizontal = 36.dp)) {
            Text("手写相册", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 40.sp, letterSpacing = 0.12.em, color = colors.onBackground)
            Spacer(Modifier.height(14.dp))
            Box(Modifier.width(36.dp).height(2.dp).background(colors.primary))
            Spacer(Modifier.height(22.dp))
            Text(
                "照片、附言和随笔，都只留在这台手机里。",
                fontFamily = FontFamily.Serif, fontSize = 16.sp, lineHeight = 28.sp, color = colors.onSurfaceVariant
            )
        }
        Column(Modifier.align(Alignment.BottomCenter).padding(bottom = 56.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            val source = remember { MutableInteractionSource() }
            val pressed by source.collectIsPressedAsState()
            val s by animateFloatAsState(if (pressed) 0.94f else 1f, spring(dampingRatio = 0.6f, stiffness = 600f), label = "press")
            Text(
                if (blocked) "去设置里允许访问照片" else "允许访问照片",
                color = colors.onPrimary, fontWeight = FontWeight.SemiBold, letterSpacing = 0.2.em, fontSize = 15.sp,
                modifier = Modifier
                    .graphicsLayer { scaleX = s; scaleY = s }
                    .background(colors.primary, MaterialTheme.shapes.extraLarge)
                    .clickable(interactionSource = source, indication = null) {
                        if (blocked) context.startActivity(
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                        ) else perms.launchMultiplePermissionRequest()
                    }
                    .padding(horizontal = 34.dp, vertical = 14.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "先不用了", color = colors.outline, fontSize = 13.sp, letterSpacing = 0.12.em,
                modifier = Modifier.clickable { activity?.finish() }.padding(8.dp)
            )
        }
    }
}
