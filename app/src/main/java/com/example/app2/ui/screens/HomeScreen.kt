package com.example.app2.ui.screens

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
import androidx.compose.ui.unit.dp
import com.example.app2.ui.TodoUiState
import com.example.app2.ui.components.EmptyState
import com.example.app2.ui.components.PageHeader
import com.example.app2.ui.components.TaskActions
import com.example.app2.ui.components.TaskListBody
import com.example.app2.ui.icons.TodoIcons
import com.example.app2.ui.theme.TodoTheme
import com.example.app2.util.Dates

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
            title = "今天",
            subtitle = "${Dates.greeting()} · ${Dates.headerLabel()}",
            actions = {
                Text(
                    text = if (count == 0) "已清空" else "$count 项",
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.textTertiary,
                    modifier = Modifier.padding(end = 2.dp)
                )
                IconButton(onClick = onOpenSearch, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = TodoIcons.Search,
                        contentDescription = "搜索",
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
                    title = "今天没有待办了",
                    message = "点下面的 + 添加一项，或者心安理得地歇一会儿。"
                )
            }
        )
    }
}
