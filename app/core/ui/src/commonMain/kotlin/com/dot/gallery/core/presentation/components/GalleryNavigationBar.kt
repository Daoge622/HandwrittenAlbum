/*
 * SPDX-FileCopyrightText: 2023-2026 IacobIacob01
 * SPDX-License-Identifier: Apache-2.0
 */

package com.dot.gallery.core.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.dot.gallery.core.presentation.util.LocalHazeState
import com.dot.gallery.core.presentation.util.NavigationItem
import com.dot.gallery.core.presentation.util.hazeEffectScaled
import com.dot.gallery.core.ui.settings.rememberPreference
import com.dot.gallery.core.ui.util.supportsBlur
import com.dot.gallery.domain.settings.SettingsKeys
import com.dot.gallery.ui.theme.ComponentSize
import com.dot.gallery.ui.theme.Spacing
import dev.chrisbanes.haze.blur.LocalHazeBlurStyle

/**
 * The floating Material-style bottom bar. Pure rendering — the caller supplies
 * the selected route and handles clicks. Extracted verbatim from the app's
 * `GalleryNavBar` so docs previews render the same bar without a NavController.
 */
@Composable
fun GalleryNavigationBarContent(
    modifier: Modifier = Modifier,
    selectedRoute: String?,
    navigationItems: List<NavigationItem>,
    onClick: (route: String) -> Unit,
) {
    // 手写相册「画报」导航：通栏纸色底 + 顶部细线；只用文字；当前页下方一个朱红小圆点，切换时弹簧滑动。
    val colors = MaterialTheme.colorScheme
    val selectedIndex = navigationItems.indexOfFirst { it.route == selectedRoute }.coerceAtLeast(0)
    val dotFraction by androidx.compose.animation.core.animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.72f, stiffness = 520f),
        label = "navDot"
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.background.copy(alpha = 0.96f))
            .then(modifier)
    ) {
        androidx.compose.material3.HorizontalDivider(color = colors.outline, thickness = 0.5.dp)
        androidx.compose.foundation.layout.BoxWithConstraints(
            Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 12.dp)
        ) {
            val segment = maxWidth / navigationItems.size.coerceAtLeast(1)
            Row(Modifier.fillMaxWidth().height(58.dp), verticalAlignment = Alignment.CenterVertically) {
                navigationItems.forEach { item ->
                    GalleryNavBarItem(navItem = item, isSelected = item.route == selectedRoute, onClick = onClick)
                }
            }
            Box(
                Modifier
                    .offset(x = segment * dotFraction + segment / 2 - 3.dp, y = 44.dp)
                    .size(6.dp)
                    .background(colors.primary, CircleShape)
            )
        }
    }
}

@Composable
fun RowScope.GalleryNavBarItem(
    navItem: NavigationItem,
    isSelected: Boolean,
    onClick: (route: String) -> Unit,
) {
    val color by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "navLabel"
    )
    Box(
        modifier = Modifier
            .height(58.dp)
            .weight(1f)
            .selectable(selected = isSelected, role = Role.Tab, onClick = { onClick(navItem.route) }),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = navItem.name,
            color = color,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
            fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal,
            fontSize = 15.sp,
            letterSpacing = 0.1.em,
            modifier = Modifier.padding(bottom = 6.dp)
        )
    }
}
