package com.example.app2.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app2.data.PRIORITY_NONE
import com.example.app2.data.Project
import com.example.app2.data.Task
import com.example.app2.ui.components.HairLine
import com.example.app2.ui.icons.TodoIcons
import com.example.app2.ui.theme.TodoTheme
import com.example.app2.ui.theme.priorityColor
import com.example.app2.util.Dates
import kotlinx.coroutines.delay

/**
 * Todoist 式的底部快速添加面板：标题 + 描述 + 三个属性 chip，
 * chip 点开在下方展开对应的选择器，最后用红色圆钮提交。
 *
 * 编辑已有任务不走这里，而是整页的 TaskDetailScreen。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditorSheet(
    initial: Task?,
    projects: List<Project>,
    defaultProjectId: Long?,
    defaultDate: Long?,
    onDismiss: () -> Unit,
    onSave: (Task) -> Unit
) {
    val palette = TodoTheme.palette
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title by remember { mutableStateOf(initial?.title.orEmpty()) }
    var note by remember { mutableStateOf(initial?.note.orEmpty()) }
    var dueDate by remember { mutableStateOf(initial?.dueDate ?: defaultDate) }
    var dueTime by remember { mutableStateOf(initial?.dueTime) }
    var priority by remember { mutableIntStateOf(initial?.priority ?: PRIORITY_NONE) }
    var projectId by remember { mutableStateOf(initial?.projectId ?: defaultProjectId) }
    var picker by remember { mutableStateOf(PropertyPicker.NONE) }
    var showCalendar by remember { mutableStateOf(false) }
    val titleFocus = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        delay(300)
        runCatching { titleFocus.requestFocus() }
    }

    val currentProject = projects.firstOrNull { it.id == projectId }

    fun submit() {
        val clean = title.trim()
        if (clean.isEmpty()) return
        val draft = (initial ?: Task(title = clean)).copy(
            title = clean,
            note = note.trim(),
            dueDate = dueDate,
            dueTime = dueTime,
            priority = priority,
            projectId = projectId
        )
        onSave(draft)
        onDismiss()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = palette.textPrimary,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 32.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(palette.outline)
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .imePadding()
                .navigationBarsPadding()
        ) {
            BasicTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(titleFocus),
                textStyle = MaterialTheme.typography.titleMedium.copy(
                    color = palette.textPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                cursorBrush = SolidColor(palette.brand),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                decorationBox = { inner ->
                    Box {
                        if (title.isEmpty()) {
                            Text(
                                text = "任务名称",
                                style = MaterialTheme.typography.titleMedium,
                                fontSize = 17.sp,
                                color = palette.textTertiary
                            )
                        }
                        inner()
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))
            HairLine()
            Spacer(modifier = Modifier.height(10.dp))

            BasicTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = palette.textSecondary),
                cursorBrush = SolidColor(palette.brand),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                decorationBox = { inner ->
                    Box {
                        if (note.isEmpty()) {
                            Text(
                                text = "描述",
                                style = MaterialTheme.typography.bodyMedium,
                                color = palette.textTertiary
                            )
                        }
                        inner()
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EditorChip(
                    icon = TodoIcons.Today,
                    label = dueDate?.let { Dates.shortLabel(it) } ?: "日期",
                    active = dueDate != null,
                    onClick = {
                        picker = if (picker == PropertyPicker.DATE) PropertyPicker.NONE else PropertyPicker.DATE
                    }
                )
                EditorChip(
                    icon = TodoIcons.Flag,
                    label = if (priority in 1..3) "P$priority" else "优先级",
                    active = priority in 1..3,
                    tint = if (priority in 1..3) priorityColor(priority) else null,
                    onClick = {
                        picker = if (picker == PropertyPicker.PRIORITY) PropertyPicker.NONE
                        else PropertyPicker.PRIORITY
                    }
                )
                EditorChip(
                    icon = TodoIcons.Hash,
                    label = currentProject?.name ?: "收件箱",
                    active = currentProject != null,
                    tint = currentProject?.let { Color(it.color) },
                    onClick = {
                        picker = if (picker == PropertyPicker.PROJECT) PropertyPicker.NONE
                        else PropertyPicker.PROJECT
                    }
                )
            }

            when (picker) {
                PropertyPicker.DATE -> DueDatePanel(
                    dueDate = dueDate,
                    onPick = { dueDate = it; picker = PropertyPicker.NONE },
                    onCustom = { showCalendar = true }
                )

                PropertyPicker.PRIORITY -> PriorityPanel(
                    priority = priority,
                    onPick = { priority = it; picker = PropertyPicker.NONE }
                )

                PropertyPicker.PROJECT -> ProjectPanel(
                    projects = projects,
                    selectedId = projectId,
                    onPick = { projectId = it; picker = PropertyPicker.NONE }
                )

                PropertyPicker.NONE -> Spacer(modifier = Modifier.height(6.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (title.isNotBlank()) palette.brand
                            else palette.brand.copy(alpha = 0.35f)
                        )
                        .clickable(enabled = title.isNotBlank()) { submit() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = TodoIcons.Send,
                        contentDescription = "保存",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
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
                    }
                ) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showCalendar = false }) { Text("取消") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun EditorChip(
    icon: ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit,
    tint: Color? = null
) {
    val palette = TodoTheme.palette
    val accent = tint ?: palette.brand
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (active) accent.copy(alpha = 0.09f) else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (active) accent.copy(alpha = 0.45f) else palette.outline,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (active) accent else palette.textSecondary,
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (active) accent else palette.textSecondary
        )
    }
}
