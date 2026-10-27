package com.example.app2.ui.editor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.app2.data.PRIORITY_NONE
import com.example.app2.data.Project
import com.example.app2.ui.components.PriorityFlag
import com.example.app2.ui.components.ProjectDot
import com.example.app2.ui.icons.TodoIcons
import com.example.app2.ui.theme.TodoTheme
import com.example.app2.util.Dates
import java.util.Calendar

internal enum class PropertyPicker { NONE, DATE, PRIORITY, PROJECT }

/** 选择器里的一行：左侧可放图标/色点，右侧打勾 */
@Composable
internal fun PickerRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    leading: @Composable () -> Unit
) {
    val palette = TodoTheme.palette
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(modifier = Modifier.width(18.dp), contentAlignment = Alignment.Center) { leading() }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = palette.textPrimary,
            modifier = Modifier.weight(1f)
        )
        if (selected) {
            Icon(
                imageVector = TodoIcons.Check,
                contentDescription = null,
                tint = palette.brand,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
internal fun DueDatePanel(
    dueDate: Long?,
    onPick: (Long?) -> Unit,
    onCustom: () -> Unit
) {
    val today = Dates.today()
    val nextMonday = run {
        val cal = Calendar.getInstance()
        cal.timeInMillis = today
        val dow = cal.get(Calendar.DAY_OF_WEEK)
        val diff = (Calendar.MONDAY - dow + 7) % 7
        Dates.plusDays(today, if (diff == 0) 7 else diff)
    }
    val palette = TodoTheme.palette

    Column(modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)) {
        PickerRow(
            label = "今天",
            selected = dueDate != null && Dates.daysFromToday(dueDate) == 0,
            onClick = { onPick(today) },
            leading = { Icon(TodoIcons.Today, null, tint = palette.textSecondary, modifier = Modifier.size(16.dp)) }
        )
        PickerRow(
            label = "明天",
            selected = dueDate != null && Dates.daysFromToday(dueDate) == 1,
            onClick = { onPick(Dates.plusDays(today, 1)) },
            leading = { Icon(TodoIcons.Upcoming, null, tint = palette.textSecondary, modifier = Modifier.size(16.dp)) }
        )
        PickerRow(
            label = "下周",
            selected = dueDate != null && Dates.daysFromToday(dueDate) == 7,
            onClick = { onPick(Dates.plusDays(today, 7)) },
            leading = { Icon(TodoIcons.ChevronRight, null, tint = palette.textSecondary, modifier = Modifier.size(16.dp)) }
        )
        PickerRow(
            label = "下周一 · ${Dates.fullLabel(nextMonday)}",
            selected = dueDate == nextMonday,
            onClick = { onPick(nextMonday) },
            leading = { Icon(TodoIcons.List, null, tint = palette.textSecondary, modifier = Modifier.size(16.dp)) }
        )
        PickerRow(
            label = "自选日期…",
            selected = false,
            onClick = onCustom,
            leading = { Icon(TodoIcons.Search, null, tint = palette.textSecondary, modifier = Modifier.size(16.dp)) }
        )
        if (dueDate != null) {
            PickerRow(
                label = "清除日期",
                selected = false,
                onClick = { onPick(null) },
                leading = { Icon(TodoIcons.Close, null, tint = palette.textTertiary, modifier = Modifier.size(16.dp)) }
            )
        }
    }
}

@Composable
internal fun PriorityPanel(priority: Int, onPick: (Int) -> Unit) {
    val palette = TodoTheme.palette
    Column(modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)) {
        (1..4).forEach { p ->
            PickerRow(
                label = when (p) {
                    1 -> "P1 · 最高"
                    2 -> "P2 · 高"
                    3 -> "P3 · 中"
                    else -> "P4 · 无优先级"
                },
                selected = priority == p,
                onClick = { onPick(p) },
                leading = {
                    if (p == PRIORITY_NONE) {
                        Icon(
                            imageVector = TodoIcons.Flag,
                            contentDescription = null,
                            tint = palette.textTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        PriorityFlag(priority = p)
                    }
                }
            )
        }
    }
}

@Composable
internal fun ProjectPanel(
    projects: List<Project>,
    selectedId: Long?,
    onPick: (Long?) -> Unit
) {
    val palette = TodoTheme.palette
    Column(modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)) {
        PickerRow(
            label = "收件箱",
            selected = selectedId == null,
            onClick = { onPick(null) },
            leading = { Icon(TodoIcons.Inbox, null, tint = palette.textSecondary, modifier = Modifier.size(16.dp)) }
        )
        projects.forEach { project ->
            PickerRow(
                label = project.name,
                selected = selectedId == project.id,
                onClick = { onPick(project.id) },
                leading = { ProjectDot(color = androidx.compose.ui.graphics.Color(project.color), size = 8) }
            )
        }
    }
}
