package com.huangshuheng.todo.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huangshuheng.todo.R
import com.huangshuheng.todo.data.Project
import com.huangshuheng.todo.data.Task
import com.huangshuheng.todo.ui.components.HairLine
import com.huangshuheng.todo.ui.components.TaskCheckbox
import com.huangshuheng.todo.ui.components.priorityLabelRes
import com.huangshuheng.todo.ui.editor.DueDatePanel
import com.huangshuheng.todo.ui.editor.PriorityPanel
import com.huangshuheng.todo.ui.editor.ProjectPanel
import com.huangshuheng.todo.ui.editor.PropertyPicker
import com.huangshuheng.todo.ui.format.dateTimeLabel
import com.huangshuheng.todo.ui.format.fullDateLabel
import com.huangshuheng.todo.ui.icons.TodoIcons
import com.huangshuheng.todo.ui.theme.TodoTheme
import com.huangshuheng.todo.ui.theme.priorityColor
import com.huangshuheng.todo.util.Dates
import kotlinx.coroutines.delay

/**
 * 任务详情页（Todoist 点进任务后的那一层）：
 * 标题、描述可直接改，日期 / 优先级 / 项目 点开就地展开选择器，改动即时落库。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    task: Task,
    projects: List<Project>,
    onClose: () -> Unit,
    onSave: (Task) -> Unit,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit
) {
    val palette = TodoTheme.palette
    val ringColor = if (task.hasPriority) priorityColor(task.priority) else palette.checkboxRing

    var title by remember(task.id) { mutableStateOf(task.title) }
    var note by remember(task.id) { mutableStateOf(task.note) }
    var dueDate by remember(task.id) { mutableStateOf(task.dueDate) }
    var priority by remember(task.id) { mutableIntStateOf(task.priority) }
    var projectId by remember(task.id) { mutableStateOf(task.projectId) }
    var picker by remember { mutableStateOf(PropertyPicker.NONE) }
    var showCalendar by remember { mutableStateOf(false) }

    val currentProject = projects.firstOrNull { it.id == projectId }

    fun draft(): Task = task.copy(
        // 标题被清空时保留原标题，不产出空任务
        title = title.trim().ifEmpty { task.title },
        note = note.trim(),
        dueDate = dueDate,
        priority = priority,
        projectId = projectId
    )

    fun commit() {
        val d = draft()
        if (d != task) onSave(d)
    }

    fun closeWithCommit() {
        commit()
        onClose()
    }

    // 文字改动做 450ms 防抖，避免每敲一个字就写一次库
    LaunchedEffect(title, note) {
        delay(450)
        commit()
    }

    BackHandler { closeWithCommit() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        // 顶栏：关闭 + 完成
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 6.dp, end = 14.dp, top = 6.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { closeWithCommit() }, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = TodoIcons.Close,
                    contentDescription = stringResource(R.string.action_close),
                    tint = palette.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            if (task.isCompleted) {
                Text(
                    text = stringResource(R.string.detail_status_done),
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.brand
                )
            } else {
                TextButton(
                    onClick = {
                        commit()
                        onToggleComplete()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.action_done),
                        style = MaterialTheme.typography.labelMedium,
                        color = palette.brand
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
        ) {
            // 标题 + 复选框
            Row(verticalAlignment = Alignment.Top) {
                TaskCheckbox(
                    checked = task.isCompleted,
                    ringColor = ringColor,
                    onToggle = {
                        commit()
                        onToggleComplete()
                    },
                    modifier = Modifier.padding(top = 1.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                BasicTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (task.isCompleted) palette.textTertiary else palette.textPrimary,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough
                        else TextDecoration.None
                    ),
                    cursorBrush = SolidColor(palette.brand),
                    decorationBox = { inner ->
                        Box {
                            if (title.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.editor_task_name),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = palette.textTertiary
                                )
                            }
                            inner()
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 描述
            BasicTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = palette.textSecondary,
                    lineHeight = 22.sp
                ),
                cursorBrush = SolidColor(palette.brand),
                decorationBox = { inner ->
                    Box {
                        if (note.isEmpty()) {
                            Text(
                                text = stringResource(R.string.editor_add_description),
                                style = MaterialTheme.typography.bodyMedium,
                                color = palette.textTertiary
                            )
                        }
                        inner()
                    }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))
            HairLine()
            Spacer(modifier = Modifier.height(2.dp))

            // 属性行
            DetailPropertyRow(
                icon = TodoIcons.Today,
                label = stringResource(R.string.editor_date),
                value = dueDate?.let {
                    fullDateLabel(it) + (task.dueTime?.let { t -> " $t" } ?: "")
                } ?: stringResource(R.string.date_none),
                iconTint = if (dueDate != null) palette.brand else null,
                valueColor = when {
                    dueDate == null -> palette.textTertiary
                    Dates.isOverdue(dueDate) || Dates.isToday(dueDate) -> palette.brand
                    else -> null
                },
                onClick = {
                    picker = if (picker == PropertyPicker.DATE) PropertyPicker.NONE
                    else PropertyPicker.DATE
                }
            )
            if (picker == PropertyPicker.DATE) {
                DueDatePanel(
                    dueDate = dueDate,
                    onPick = {
                        dueDate = it
                        picker = PropertyPicker.NONE
                        commit()
                    },
                    onCustom = { showCalendar = true }
                )
            }

            DetailPropertyRow(
                icon = TodoIcons.Flag,
                label = stringResource(R.string.editor_priority),
                value = stringResource(priorityLabelRes(priority)),
                iconTint = if (priority in 1..3) priorityColor(priority) else null,
                valueColor = if (priority in 1..3) priorityColor(priority) else palette.textTertiary,
                onClick = {
                    picker = if (picker == PropertyPicker.PRIORITY) PropertyPicker.NONE
                    else PropertyPicker.PRIORITY
                }
            )
            if (picker == PropertyPicker.PRIORITY) {
                PriorityPanel(
                    priority = priority,
                    onPick = {
                        priority = it
                        picker = PropertyPicker.NONE
                        commit()
                    }
                )
            }

            DetailPropertyRow(
                icon = TodoIcons.Hash,
                label = stringResource(R.string.editor_project),
                value = currentProject?.name ?: stringResource(R.string.view_inbox),
                iconTint = currentProject?.let { Color(it.color) },
                valueColor = if (currentProject == null) palette.textTertiary else null,
                onClick = {
                    picker = if (picker == PropertyPicker.PROJECT) PropertyPicker.NONE
                    else PropertyPicker.PROJECT
                }
            )
            if (picker == PropertyPicker.PROJECT) {
                ProjectPanel(
                    projects = projects,
                    selectedId = projectId,
                    onPick = {
                        projectId = it
                        picker = PropertyPicker.NONE
                        commit()
                    }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            HairLine()
            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = stringResource(
                    R.string.task_created_at,
                    dateTimeLabel(task.createdAt)
                ),
                style = MaterialTheme.typography.bodySmall,
                color = palette.textTertiary
            )
            if (task.completedAt != null) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = stringResource(
                        R.string.task_completed_at,
                        dateTimeLabel(task.completedAt)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textTertiary
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        commit()
                        onDelete()
                    }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = TodoIcons.Trash,
                    contentDescription = null,
                    tint = palette.priority1,
                    modifier = Modifier.size(17.dp)
                )
                Text(
                    text = stringResource(R.string.menu_delete_task),
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.priority1
                )
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }

    if (showCalendar) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dueDate ?: Dates.today()
        )
        DatePickerDialog(
            onDismissRequest = { showCalendar = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            // DatePicker 返回 UTC 零点，加 12 小时再归一到本地当天
                            dueDate = Dates.startOfDay(it + 12L * 60 * 60 * 1000)
                        }
                        showCalendar = false
                        picker = PropertyPicker.NONE
                        commit()
                    }
                ) { Text(stringResource(R.string.action_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showCalendar = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun DetailPropertyRow(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit,
    iconTint: Color? = null,
    valueColor: Color? = null
) {
    val palette = TodoTheme.palette
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint ?: palette.textSecondary,
            modifier = Modifier.size(17.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = palette.textSecondary
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor ?: palette.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
