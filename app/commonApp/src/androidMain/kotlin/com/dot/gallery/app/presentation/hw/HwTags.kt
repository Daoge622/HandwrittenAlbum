/*
 * SPDX-FileCopyrightText: 2026 手写相册 contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.dot.gallery.app.presentation.hw

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.dot.gallery.data.entity.TagEntity
import com.dot.gallery.domain.model.UriMedia
import kotlinx.coroutines.launch

/** 一枚标签：细线描边 + 颜色小点 + 宋体名字；selected 时填充为浅色底。 */
@Composable
fun TagChip(tag: TagEntity, selected: Boolean = false, trailing: String? = null, onTrailing: (() -> Unit)? = null, onClick: (() -> Unit)? = null) {
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier
            .border(0.7.dp, if (selected) Color(tag.color) else colors.outline, RoundedCornerShape(50))
            .background(if (selected) Color(tag.color).copy(alpha = 0.12f) else Color.Transparent, RoundedCornerShape(50))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(start = 10.dp, end = if (trailing != null) 4.dp else 12.dp, top = 5.dp, bottom = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(6.dp).background(Color(tag.color), CircleShape))
        Spacer(Modifier.width(6.dp))
        Text(tag.name, fontFamily = FontFamily.Serif, fontSize = 13.sp, color = colors.onBackground)
        if (trailing != null) Text(
            trailing, fontSize = 13.sp, color = colors.onSurfaceVariant,
            modifier = Modifier.clickable { onTrailing?.invoke() }.padding(horizontal = 6.dp)
        )
    }
}

/** 附言卡里的“标签”一行：已有标签 + 输入新标签 + 从已有标签中挑选。 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HwTagRow(media: UriMedia, repo: HwNotesRepository) {
    val colors = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    val mine by remember(media.id) { repo.tagsOf(media.id) }.collectAsState(initial = emptyList())
    val all by remember { repo.allTags() }.collectAsState(initial = emptyList())
    var adding by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }
    val focus = remember { FocusRequester() }

    fun commit(name: String) {
        if (name.isBlank()) { adding = false; return }
        scope.launch { repo.addTag(media, name) }
        input = ""
    }

    Column(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Text("标签", fontSize = 12.sp, letterSpacing = 0.12.em, color = colors.onSurfaceVariant)
        FlowRow(
            Modifier.fillMaxWidth().padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            mine.forEach { t -> TagChip(t, trailing = "×", onTrailing = { scope.launch { repo.removeTag(media, t.id) } }) }
            if (adding) {
                BasicTextField(
                    value = input, onValueChange = { input = it }, singleLine = true,
                    textStyle = TextStyle(fontFamily = FontFamily.Serif, fontSize = 13.sp, color = colors.onBackground),
                    cursorBrush = SolidColor(colors.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { commit(input) }),
                    decorationBox = { inner -> Box { if (input.isEmpty()) Text("新标签", fontSize = 13.sp, color = colors.outline); inner() } },
                    modifier = Modifier
                        .border(0.7.dp, colors.primary, RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .widthIn(min = 64.dp, max = 160.dp)
                        .focusRequester(focus)
                )
                LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }
            } else {
                Text(
                    "＋ 标签", color = colors.primary, fontSize = 13.sp, letterSpacing = 0.08.em,
                    modifier = Modifier.clickable { adding = true }.padding(horizontal = 4.dp, vertical = 6.dp)
                )
            }
        }
        // 从已有标签里挑（输入时按关键字过滤）
        if (adding) {
            val mineIds = mine.map { it.id }.toSet()
            val suggestions = all.filter { it.id !in mineIds && (input.isBlank() || it.name.contains(input.trim(), true)) }.take(12)
            if (suggestions.isNotEmpty()) FlowRow(
                Modifier.fillMaxWidth().padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                suggestions.forEach { t -> TagChip(t, onClick = { commit(t.name) }) }
            }
            Text(
                "完成", color = colors.onSurfaceVariant, fontSize = 12.sp,
                modifier = Modifier.clickable { commit(input); adding = false }.padding(top = 10.dp)
            )
        }
    }
}
