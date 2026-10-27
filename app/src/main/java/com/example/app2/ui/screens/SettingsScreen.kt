package com.example.app2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.app2.ui.TodoUiState
import com.example.app2.ui.components.NavRow
import com.example.app2.ui.components.PageHeader
import com.example.app2.ui.components.SectionHeader
import com.example.app2.ui.icons.TodoIcons
import com.example.app2.ui.theme.TodoTheme

@Composable
fun SettingsScreen(
    state: TodoUiState,
    themeMode: Int,
    onThemeChange: (Int) -> Unit,
    onClearCompleted: () -> Unit,
    accountEmail: String,
    onSignOut: () -> Unit
) {
    val palette = TodoTheme.palette

    Column(modifier = Modifier.fillMaxSize()) {
        PageHeader(title = "设置", subtitle = "账号、外观与数据")

        LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
            item(key = "account-header") { SectionHeader(title = "账号") }
            item(key = "account") { AccountRow(email = accountEmail) }
            item(key = "sign-out") {
                NavRow(
                    icon = TodoIcons.Close,
                    label = "退出登录",
                    iconTint = palette.priority1,
                    labelColor = palette.priority1,
                    onClick = onSignOut
                )
            }

            item(key = "theme-header") { SectionHeader(title = "外观") }
            item(key = "theme-system") {
                ThemeOptionRow(
                    label = "跟随系统",
                    selected = themeMode == 0,
                    onClick = { onThemeChange(0) }
                )
            }
            item(key = "theme-light") {
                ThemeOptionRow(
                    label = "浅色",
                    selected = themeMode == 1,
                    onClick = { onThemeChange(1) }
                )
            }
            item(key = "theme-dark") {
                ThemeOptionRow(
                    label = "深色",
                    selected = themeMode == 2,
                    onClick = { onThemeChange(2) }
                )
            }

            item(key = "data-header") { SectionHeader(title = "数据") }
            item(key = "clear-completed") {
                NavRow(
                    icon = TodoIcons.Trash,
                    label = "清空已完成的任务",
                    iconTint = palette.priority1,
                    labelColor = palette.priority1,
                    badge = "${state.completed.size} 项",
                    onClick = onClearCompleted
                )
            }

            item(key = "about-header") { SectionHeader(title = "关于") }
            item(key = "version") {
                NavRow(icon = TodoIcons.Hash, label = "版本", badge = "1.0.0", onClick = {})
            }
            item(key = "build") {
                NavRow(icon = TodoIcons.List, label = "Todoist 风格 · Jetpack Compose", onClick = {})
            }
        }
    }
}

@Composable
private fun AccountRow(email: String) {
    val palette = TodoTheme.palette
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(palette.brand),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = email.firstOrNull()?.uppercase() ?: "?",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = email,
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textPrimary
            )
            Text(
                text = "已登录 · 数据仅在本机",
                style = MaterialTheme.typography.bodySmall,
                color = palette.textTertiary
            )
        }
    }
}

@Composable
private fun ThemeOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val palette = TodoTheme.palette
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = palette.textPrimary,
            modifier = Modifier.weight(1f)
        )
        if (selected) {
            Icon(
                imageVector = TodoIcons.Check,
                contentDescription = "已选中",
                tint = palette.brand,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
