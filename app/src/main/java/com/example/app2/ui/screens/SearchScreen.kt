package com.example.app2.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.example.app2.ui.TodoUiState
import com.example.app2.ui.components.EmptyState
import com.example.app2.ui.components.HairLine
import com.example.app2.ui.components.TaskActions
import com.example.app2.ui.components.TaskRowWithMenu
import com.example.app2.ui.icons.TodoIcons
import com.example.app2.ui.theme.TodoTheme

/** 搜索：标题与描述都参与匹配，结果点开还是同一个编辑面板 */
@Composable
fun SearchScreen(
    state: TodoUiState,
    actions: TaskActions,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit
) {
    val palette = TodoTheme.palette
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        runCatching { focusRequester.requestFocus() }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 6.dp, end = 12.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = TodoIcons.ChevronLeft,
                    contentDescription = "返回",
                    tint = palette.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
            BasicTextField(
                value = state.searchQuery,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = palette.textPrimary),
                cursorBrush = SolidColor(palette.brand),
                decorationBox = { inner ->
                    Box {
                        if (state.searchQuery.isEmpty()) {
                            Text(
                                text = "搜索任务…",
                                style = MaterialTheme.typography.bodyLarge,
                                color = palette.textTertiary
                            )
                        }
                        inner()
                    }
                }
            )
            if (state.searchQuery.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = TodoIcons.Close,
                        contentDescription = "清空",
                        tint = palette.textTertiary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }

        HairLine()

        when {
            state.searchQuery.isBlank() -> EmptyState(
                icon = TodoIcons.Search,
                title = "搜点什么",
                message = "任务标题和描述都会参与匹配。"
            )

            state.searchResults.isEmpty() -> EmptyState(
                icon = TodoIcons.Search,
                title = "没有匹配的任务",
                message = "换个关键词，或者去添加一个新的待办。"
            )

            else -> {
                Text(
                    text = "${state.searchResults.size} 个结果",
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.textTertiary,
                    modifier = Modifier.padding(start = 20.dp, top = 12.dp, bottom = 4.dp)
                )
                LazyColumn {
                    items(items = state.searchResults, key = { it.id }) { task ->
                        TaskRowWithMenu(
                            task = task,
                            project = state.projects.firstOrNull { it.id == task.projectId },
                            actions = actions,
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }
}
