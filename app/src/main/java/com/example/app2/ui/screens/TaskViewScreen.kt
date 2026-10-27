package com.example.app2.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.app2.ui.TodoUiState
import com.example.app2.ui.TodoView
import com.example.app2.ui.components.EmptyState
import com.example.app2.ui.components.ProjectDot
import com.example.app2.ui.components.TaskActions
import com.example.app2.ui.components.TaskListBody
import com.example.app2.ui.icons.TodoIcons
import com.example.app2.ui.theme.TodoTheme

/**
 * 收件箱 / 即将到来 / 某个项目 —— 共用同一个列表页，只是数据源不同。
 */
@Composable
fun TaskViewScreen(
    state: TodoUiState,
    actions: TaskActions,
    onBack: () -> Unit
) {
    val palette = TodoTheme.palette
    val project = state.projects.firstOrNull { it.id == state.projectId }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 6.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
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
            Spacer(modifier = Modifier.width(6.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (project != null) {
                        ProjectDot(color = Color(project.color), size = 9)
                    }
                    Text(
                        text = state.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = palette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (state.subtitle.isNotBlank()) {
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = state.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textTertiary
                    )
                }
            }
        }

        TaskListBody(
            sections = state.sections,
            projects = state.projects,
            actions = actions,
            emptyState = {
                EmptyState(
                    icon = when (state.view) {
                        TodoView.INBOX -> TodoIcons.Inbox
                        TodoView.UPCOMING -> TodoIcons.Upcoming
                        else -> TodoIcons.DoneCircle
                    },
                    title = "这里还是空的",
                    message = when (state.view) {
                        TodoView.INBOX -> "没有归属项目的任务都会落在收件箱。"
                        TodoView.UPCOMING -> "接下来几天没有安排，先松口气。"
                        TodoView.PROJECT -> "这个项目下还没有待办。"
                        else -> "暂时没有任务。"
                    }
                )
            }
        )
    }
}
