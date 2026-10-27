package com.huangshuheng.todo.ui.components

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.huangshuheng.todo.R
import com.huangshuheng.todo.data.Project
import com.huangshuheng.todo.data.Task
import com.huangshuheng.todo.ui.SectionKind
import com.huangshuheng.todo.ui.TaskSection
import com.huangshuheng.todo.ui.format.fullDateLabel
import com.huangshuheng.todo.ui.icons.TodoIcons
import com.huangshuheng.todo.ui.theme.TodoTheme
import com.huangshuheng.todo.util.Dates

/** 列表需要的全部回调，打包成一个对象，避免参数列表爆炸 */
class TaskActions(
    val onToggle: (Task) -> Unit,
    val onOpen: (Task) -> Unit,
    val onSetPriority: (Task, Int) -> Unit,
    val onSetToday: (Task) -> Unit,
    val onClearDate: (Task) -> Unit,
    val onDelete: (Task) -> Unit
)

/** 分区标题：DATE 类型渲染具体日期，其余查资源 */
@Composable
fun sectionTitle(section: TaskSection): String = when (section.kind) {
    SectionKind.OVERDUE -> stringResource(R.string.section_overdue)
    SectionKind.TODAY -> stringResource(R.string.section_today)
    SectionKind.INBOX -> stringResource(R.string.view_inbox)
    SectionKind.AFTER -> stringResource(R.string.section_after)
    SectionKind.UNDATED -> stringResource(R.string.section_undated)
    SectionKind.DATE -> fullDateLabel(section.date ?: Dates.today())
}

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
                    title = sectionTitle(section),
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
                text = stringResource(R.string.menu_priority),
                style = MaterialTheme.typography.labelSmall,
                color = palette.textTertiary,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )
            (1..4).forEach { p ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(priorityLabelRes(p)),
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.textPrimary
                        )
                    },
                    leadingIcon = {
                        if (p == 4) {
                            Icon(
                                imageVector = TodoIcons.Flag,
                                contentDescription = null,
                                tint = palette.textTertiary
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
                text = {
                    Text(
                        text = stringResource(R.string.menu_move_today),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
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
                    text = {
                        Text(
                            text = stringResource(R.string.menu_clear_date),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
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
                        text = stringResource(R.string.menu_delete_task),
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

/** 优先级文案对应的资源 id */
internal fun priorityLabelRes(priority: Int): Int = when (priority) {
    1 -> R.string.priority_p1
    2 -> R.string.priority_p2
    3 -> R.string.priority_p3
    else -> R.string.priority_p4
}
