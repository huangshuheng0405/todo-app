package com.example.app2.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.app2.data.PRIORITY_NONE
import com.example.app2.data.Project
import com.example.app2.data.Task
import com.example.app2.ui.TaskSection
import com.example.app2.ui.icons.TodoIcons
import com.example.app2.ui.theme.TodoTheme
import com.example.app2.util.Dates

/** 列表需要的全部回调，打包成一个对象，避免参数列表爆炸 */
class TaskActions(
    val onToggle: (Task) -> Unit,
    val onOpen: (Task) -> Unit,
    val onSetPriority: (Task, Int) -> Unit,
    val onSetToday: (Task) -> Unit,
    val onClearDate: (Task) -> Unit,
    val onDelete: (Task) -> Unit
)

@Composable
fun TaskListBody(
    sections: List<TaskSection>,
    projects: List<Project>,
    actions: TaskActions,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(bottom = 24.dp),
    header: (@Composable () -> Unit)? = null,
    emptyState: @Composable () -> Unit = {}
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        state = listState,
        contentPadding = contentPadding
    ) {
        if (header != null) {
            item(key = "header") { header() }
        }

        if (sections.isEmpty()) {
            item(key = "empty") { emptyState() }
        }

        sections.forEach { section ->
            item(key = "section-${section.key}") {
                SectionHeader(
                    title = section.title,
                    count = section.tasks.size,
                    tone = section.tone
                )
            }
            items(items = section.tasks, key = { "${section.key}-${it.id}" }) { task ->
                SwipeToDelete(
                    onDelete = { actions.onDelete(task) },
                    modifier = Modifier.animateItem()
                ) {
                    TaskRowWithMenu(
                        task = task,
                        project = projects.firstOrNull { it.id == task.projectId },
                        actions = actions
                    )
                }
            }
        }
    }
}

/** 任务行 + 长按菜单（改优先级 / 挪到今天 / 删除） */
@Composable
fun TaskRowWithMenu(
    task: Task,
    project: Project?,
    actions: TaskActions,
    modifier: Modifier = Modifier
) {
    var menuOpen by remember { mutableStateOf(false) }
    val palette = TodoTheme.palette

    Box {
        TaskRow(
            task = task,
            project = project,
            onToggle = { actions.onToggle(task) },
            onClick = { actions.onOpen(task) },
            onLongClick = { menuOpen = true },
            modifier = modifier
        )

        DropdownMenu(
            expanded = menuOpen,
            onDismissRequest = { menuOpen = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Text(
                text = "优先级",
                style = MaterialTheme.typography.labelSmall,
                color = palette.textTertiary,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )
            (1..4).forEach { p ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = if (p == PRIORITY_NONE) "P4 · 无优先级" else "P$p",
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.textPrimary
                        )
                    },
                    leadingIcon = {
                        if (p == PRIORITY_NONE) {
                            Icon(
                                imageVector = TodoIcons.Flag,
                                contentDescription = null,
                                tint = palette.textTertiary,
                                modifier = Modifier.padding(0.dp)
                            )
                        } else {
                            PriorityFlag(priority = p)
                        }
                    },
                    trailingIcon = {
                        if (task.priority == p) {
                            Icon(
                                imageVector = TodoIcons.Check,
                                contentDescription = null,
                                tint = palette.brand
                            )
                        }
                    },
                    onClick = {
                        actions.onSetPriority(task, p)
                        menuOpen = false
                    }
                )
            }
            HorizontalDivider(color = palette.divider)
            DropdownMenuItem(
                text = { Text("挪到今天", style = MaterialTheme.typography.bodyMedium) },
                leadingIcon = {
                    Icon(TodoIcons.Today, contentDescription = null, tint = palette.textSecondary)
                },
                enabled = !Dates.isToday(task.dueDate),
                onClick = {
                    actions.onSetToday(task)
                    menuOpen = false
                }
            )
            if (task.dueDate != null) {
                DropdownMenuItem(
                    text = { Text("清除日期", style = MaterialTheme.typography.bodyMedium) },
                    leadingIcon = {
                        Icon(TodoIcons.Close, contentDescription = null, tint = palette.textSecondary)
                    },
                    onClick = {
                        actions.onClearDate(task)
                        menuOpen = false
                    }
                )
            }
            HorizontalDivider(color = palette.divider)
            DropdownMenuItem(
                text = {
                    Text(
                        text = "删除任务",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.priority1
                    )
                },
                leadingIcon = {
                    Icon(TodoIcons.Trash, contentDescription = null, tint = palette.priority1)
                },
                onClick = {
                    actions.onDelete(task)
                    menuOpen = false
                }
            )
        }
    }
}
