package com.example.app2.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.app2.ui.SectionTone
import com.example.app2.ui.icons.TodoIcons
import com.example.app2.ui.theme.TodoTheme

/** 页面顶部：小字副标题 + 大标题 + 右侧操作 */
@Composable
fun PageHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val palette = TodoTheme.palette
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 12.dp, top = 14.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textTertiary
                )
                Spacer(modifier = Modifier.height(2.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.displaySmall,
                color = palette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        actions()
    }
}

/** 分区标题：逾期 / 今天 / 10月28日 周三 */
@Composable
fun SectionHeader(
    title: String,
    count: Int? = null,
    tone: SectionTone = SectionTone.NORMAL,
    modifier: Modifier = Modifier
) {
    val palette = TodoTheme.palette
    val color = when (tone) {
        SectionTone.OVERDUE -> palette.priority1
        SectionTone.TODAY -> palette.textPrimary
        SectionTone.NORMAL -> palette.textSecondary
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
        Spacer(modifier = Modifier.width(8.dp))
        if (count != null) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = palette.textTertiary
            )
        }
    }
}

/** 空状态 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    val palette = TodoTheme.palette
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = palette.textTertiary.copy(alpha = 0.5f),
            modifier = Modifier.size(44.dp)
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = palette.textSecondary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = palette.textTertiary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

/** Todoist 的红色圆形新增按钮 */
@Composable
fun AddTaskButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Int = 46
) {
    val palette = TodoTheme.palette
    Box(
        modifier = modifier
            .size(size.dp)
            .shadow(elevation = 8.dp, shape = CircleShape, ambientColor = palette.brand, spotColor = palette.brand)
            .clip(CircleShape)
            .background(palette.brand)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = TodoIcons.Add,
            contentDescription = "新建任务",
            tint = Color.White,
            modifier = Modifier.size((size * 0.45f).dp)
        )
    }
}

/** 浏览 / 设置页里那种「图标 + 文字 + 右侧数字/箭头」的行 */
@Composable
fun NavRow(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    iconTint: Color? = null,
    badge: String? = null,
    showChevron: Boolean = false,
    labelColor: Color? = null,
    onClick: () -> Unit
) {
    val palette = TodoTheme.palette
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint ?: palette.textSecondary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = labelColor ?: palette.textPrimary,
            modifier = Modifier.weight(1f)
        )
        if (badge != null && badge != "0") {
            Text(
                text = badge,
                style = MaterialTheme.typography.bodySmall,
                color = palette.textTertiary
            )
        }
        if (showChevron) {
            Icon(
                imageVector = TodoIcons.ChevronRight,
                contentDescription = null,
                tint = palette.textTertiary.copy(alpha = 0.7f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

/** 项目色点 */
@Composable
fun ProjectDot(color: Color, size: Int = 7, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(color)
    )
}
