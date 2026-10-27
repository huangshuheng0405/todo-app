package com.huangshuheng.todo.ui.screens

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.huangshuheng.todo.R
import com.huangshuheng.todo.ui.TodoUiState
import com.huangshuheng.todo.ui.TodoView
import com.huangshuheng.todo.ui.components.EmptyState
import com.huangshuheng.todo.ui.components.ProjectDot
import com.huangshuheng.todo.ui.components.TaskActions
import com.huangshuheng.todo.ui.components.TaskListBody
import com.huangshuheng.todo.ui.format.fullDateLabel
import com.huangshuheng.todo.ui.icons.TodoIcons
import com.huangshuheng.todo.ui.theme.TodoTheme
import com.huangshuheng.todo.util.Dates

/**
 * 收件箱 / 即将到来 / 某个项目 —— 共用同一个列表页，只是数据源不同。
 * 标题与副标题在这里按 view 拼装（文案不进 ViewModel）。
 */
@Composable
fun TaskViewScreen(
    state: TodoUiState,
    actions: TaskActions,
    onBack: () -> Unit
) {
    val palette = TodoTheme.palette
    val project = state.projects.firstOrNull { it.id == state.projectId }

    val title = if (state.view == TodoView.PROJECT) {
        project?.name ?: stringResource(R.string.view_project_fallback)
    } else {
        stringResource(
            when (state.view) {
                TodoView.INBOX -> R.string.view_inbox
                TodoView.UPCOMING -> R.string.view_upcoming
                else -> R.string.view_today
            }
        )
    }

    val subtitle = when (state.view) {
        TodoView.PROJECT -> stringResource(
            R.string.view_project_subtitle,
            state.sections.sumOf { it.tasks.size }
        )
        TodoView.INBOX -> stringResource(R.string.view_inbox_subtitle)
        TodoView.UPCOMING -> stringResource(R.string.view_upcoming_subtitle)
        else -> fullDateLabel(Dates.today())
    }

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
                    contentDescription = stringResource(R.string.action_back),
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
                        text = title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = palette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (subtitle.isNotBlank()) {
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = subtitle,
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
                    title = stringResource(R.string.empty_generic_title),
                    message = stringResource(
                        when (state.view) {
                            TodoView.INBOX -> R.string.empty_inbox_message
                            TodoView.UPCOMING -> R.string.empty_upcoming_message
                            TodoView.PROJECT -> R.string.empty_project_message
                            else -> R.string.empty_default_message
                        }
                    )
                )
            }
        )
    }
}
