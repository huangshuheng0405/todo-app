package com.huangshuheng.todo.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Indication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.huangshuheng.todo.R
import com.huangshuheng.todo.data.Project
import com.huangshuheng.todo.data.Task
import com.huangshuheng.todo.ui.format.dateTimeLabel
import com.huangshuheng.todo.ui.format.dayShortLabel
import com.huangshuheng.todo.ui.icons.TodoIcons
import com.huangshuheng.todo.ui.theme.TodoTheme
import com.huangshuheng.todo.ui.theme.priorityColor
import com.huangshuheng.todo.util.Dates

/**
 * Todoist 的圆形复选框：未完成是彩色细环（颜色跟着优先级走），
 * 完成时填充为实心圆并弹出白色对勾。
 */
@Composable
fun TaskCheckbox(
    checked: Boolean,
    ringColor: Color,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    size: Int = 23,
    iconRatio: Float = 0.62f
) {
    val progress by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.55f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "checkbox"
    )
    val interaction = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size((size + 12).dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = interaction,
                indication = null as Indication?
            ) { onToggle() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size.dp)) {
            val maxRadius = this.size.minDimension / 2f
            val strokeWidth = 1.7.dp.toPx()

            if (progress < 1f) {
                drawCircle(
                    color = ringColor,
                    radius = maxRadius - strokeWidth / 2f,
                    alpha = 1f - progress,
                    style = Stroke(width = strokeWidth)
                )
            }
            if (progress > 0f) {
                drawCircle(
                    color = ringColor,
                    radius = maxRadius * (0.55f + 0.45f * progress),
                    alpha = progress
                )
            }
        }
        Icon(
            imageVector = TodoIcons.Check,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .size((size * iconRatio).dp)
                .graphicsLayer {
                    alpha = progress
                    val scale = 0.4f + 0.6f * progress
                    scaleX = scale
                    scaleY = scale
                }
        )
    }
}

/**
 * 单条任务。点击编辑，长按弹出菜单，点圆圈完成。
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskRow(
    task: Task,
    project: Project?,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    showCompletedTime: Boolean = false
) {
    val palette = TodoTheme.palette
    val ringColor = if (task.hasPriority) priorityColor(task.priority) else palette.checkboxRing
    val strike = task.isCompleted

    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(start = 10.dp, end = 16.dp, top = 9.dp, bottom = 9.dp),
        verticalAlignment = Alignment.Top
    ) {
        TaskCheckbox(
            checked = task.isCompleted,
            ringColor = ringColor,
            onToggle = onToggle,
            modifier = Modifier.padding(top = 1.dp)
        )
        Spacer(modifier = Modifier.width(2.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 15.sp,
                color = if (strike) palette.textTertiary else palette.textPrimary,
                textDecoration = if (strike) TextDecoration.LineThrough else TextDecoration.None,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            if (task.note.isNotBlank()) {
                Spacer(modifier = Modifier.size(2.dp))
                Text(
                    text = task.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            val showMeta = task.dueDate != null || project != null || task.hasPriority || showCompletedTime
            if (showMeta) {
                Spacer(modifier = Modifier.size(3.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    if (showCompletedTime && task.completedAt != null) {
                        MetaItem(
                            text = stringResource(
                                R.string.task_completed_at,
                                dateTimeLabel(task.completedAt)
                            ),
                            tint = palette.textTertiary
                        )
                    }
                    task.dueDate?.let { due ->
                        val overdue = Dates.isOverdue(due)
                        val today = Dates.isToday(due)
                        MetaItem(
                            text = dayShortLabel(due) + (task.dueTime?.let { " $it" } ?: ""),
                            tint = when {
                                strike -> palette.textTertiary
                                overdue || today -> palette.brand
                                else -> palette.textTertiary
                            },
                            icon = TodoIcons.Today
                        )
                    }
                    if (!showCompletedTime) {
                        project?.let {
                            MetaItem(
                                text = it.name,
                                tint = palette.textTertiary,
                                dotColor = Color(it.color)
                            )
                        }
                        if (task.hasPriority) {
                            Icon(
                                imageVector = TodoIcons.Flag,
                                contentDescription = null,
                                tint = priorityColor(task.priority),
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/** 一行小号元信息：可选的小圆点 + 可选图标 + 文字 */
@Composable
private fun MetaItem(
    text: String,
    tint: Color,
    dotColor: Color? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (dotColor != null) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = tint,
            fontWeight = FontWeight.Normal
        )
    }
}

/** 优先级小旗子，编辑器里也用 */
@Composable
fun PriorityFlag(priority: Int, modifier: Modifier = Modifier) {
    if (priority in 1..3) {
        Icon(
            imageVector = TodoIcons.Flag,
            contentDescription = stringResource(R.string.cd_task_priority, priority),
            tint = priorityColor(priority),
            modifier = modifier.size(14.dp)
        )
    }
}

/** 分隔线，Todoist 用的是极淡的一条 */
@Composable
fun HairLine(modifier: Modifier = Modifier, startPadding: Int = 0) {
    val palette = TodoTheme.palette
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = startPadding.dp)
    ) {
        drawLine(
            color = palette.divider,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = 1.dp.toPx(),
            cap = StrokeCap.Butt
        )
    }
}
