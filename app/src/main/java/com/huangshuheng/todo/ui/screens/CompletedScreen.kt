package com.huangshuheng.todo.ui.screens

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.huangshuheng.todo.R
import com.huangshuheng.todo.data.Task
import com.huangshuheng.todo.ui.TodoUiState
import com.huangshuheng.todo.ui.components.EmptyState
import com.huangshuheng.todo.ui.components.PageHeader
import com.huangshuheng.todo.ui.components.TaskRow
import com.huangshuheng.todo.ui.icons.TodoIcons
import com.huangshuheng.todo.ui.theme.TodoTheme

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
            title = stringResource(R.string.tab_completed),
            subtitle = if (state.completed.isEmpty()) {
                stringResource(R.string.completed_subtitle_none)
            } else {
                stringResource(R.string.completed_subtitle_some, state.completed.size)
            },
            actions = {
                if (state.completed.isNotEmpty()) {
                    TextButton(onClick = onClear) {
                        Text(
                            text = stringResource(R.string.action_clear),
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
                title = stringResource(R.string.completed_empty_title),
                message = stringResource(R.string.completed_empty_message)
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
