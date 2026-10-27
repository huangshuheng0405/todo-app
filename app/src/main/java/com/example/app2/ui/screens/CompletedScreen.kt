package com.example.app2.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.app2.data.Task
import com.example.app2.ui.TodoUiState
import com.example.app2.ui.components.EmptyState
import com.example.app2.ui.components.PageHeader
import com.example.app2.ui.components.TaskRow
import com.example.app2.ui.icons.TodoIcons
import com.example.app2.ui.theme.TodoTheme

/** 已完成：按完成时间倒序，点圆圈可以撤销完成 */
@Composable
fun CompletedScreen(
    state: TodoUiState,
    onToggle: (Task) -> Unit,
    onOpen: (Task) -> Unit,
    onClear: () -> Unit
) {
    val palette = TodoTheme.palette

    Column(modifier = Modifier.fillMaxSize()) {
        PageHeader(
            title = "已完成",
            subtitle = if (state.completed.isEmpty()) "还没有完成记录" else "共 ${state.completed.size} 项",
            actions = {
                if (state.completed.isNotEmpty()) {
                    TextButton(onClick = onClear) {
                        Text(
                            text = "清空",
                            style = MaterialTheme.typography.labelMedium,
                            color = palette.brand
                        )
                    }
                }
            }
        )

        if (state.completed.isEmpty()) {
            EmptyState(
                icon = TodoIcons.DoneCircle,
                title = "还没有完成的任务",
                message = "完成任务后会归档到这里，方便回头看看自己干了多少活。"
            )
        } else {
            LazyColumn(contentPadding = PaddingValues(start = 6.dp, end = 6.dp, bottom = 24.dp)) {
                items(items = state.completed, key = { it.id }) { task ->
                    TaskRow(
                        task = task,
                        project = state.projects.firstOrNull { it.id == task.projectId },
                        onToggle = { onToggle(task) },
                        onClick = { onOpen(task) },
                        onLongClick = {},
                        showCompletedTime = true,
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }
}
