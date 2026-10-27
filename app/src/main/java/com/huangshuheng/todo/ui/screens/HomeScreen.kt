package com.huangshuheng.todo.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.huangshuheng.todo.R
import com.huangshuheng.todo.ui.TodoUiState
import com.huangshuheng.todo.ui.components.EmptyState
import com.huangshuheng.todo.ui.components.PageHeader
import com.huangshuheng.todo.ui.components.TaskActions
import com.huangshuheng.todo.ui.components.TaskListBody
import com.huangshuheng.todo.ui.format.greetingLabel
import com.huangshuheng.todo.ui.format.todayHeaderLabel
import com.huangshuheng.todo.ui.icons.TodoIcons
import com.huangshuheng.todo.ui.theme.TodoTheme

/** 首页 = 逾期 + 今天，Todoist 打开 App 的第一屏 */
@Composable
fun HomeScreen(
    state: TodoUiState,
    actions: TaskActions,
    onOpenSearch: () -> Unit
) {
    val palette = TodoTheme.palette
    val count = state.homeSections.sumOf { it.tasks.size }

    Column(modifier = Modifier.fillMaxSize()) {
        PageHeader(
            title = stringResource(R.string.view_today),
            subtitle = stringResource(
                R.string.home_subtitle,
                greetingLabel(),
                todayHeaderLabel()
            ),
            actions = {
                Text(
                    text = if (count == 0) {
                        stringResource(R.string.home_all_done)
                    } else {
                        stringResource(R.string.home_task_count, count)
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.textTertiary,
                    modifier = Modifier.padding(end = 2.dp)
                )
                IconButton(onClick = onOpenSearch, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = TodoIcons.Search,
                        contentDescription = stringResource(R.string.action_search),
                        tint = palette.textSecondary,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        )

        TaskListBody(
            sections = state.homeSections,
            projects = state.projects,
            actions = actions,
            emptyState = {
                EmptyState(
                    icon = TodoIcons.DoneCircle,
                    title = stringResource(R.string.home_empty_title),
                    message = stringResource(R.string.home_empty_message)
                )
            }
        )
    }
}
