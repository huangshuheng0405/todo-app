package com.huangshuheng.todo.ui.screens

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.huangshuheng.todo.R
import com.huangshuheng.todo.ui.TodoUiState
import com.huangshuheng.todo.ui.components.NavRow
import com.huangshuheng.todo.ui.components.PageHeader
import com.huangshuheng.todo.ui.components.SectionHeader
import com.huangshuheng.todo.ui.icons.TodoIcons
import com.huangshuheng.todo.ui.theme.TodoTheme

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
        PageHeader(
            title = stringResource(R.string.tab_settings),
            subtitle = stringResource(R.string.settings_subtitle)
        )

        LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
            item(key = "account-header") {
                SectionHeader(title = stringResource(R.string.settings_account))
            }
            item(key = "account") { AccountRow(email = accountEmail) }
            item(key = "sign-out") {
                NavRow(
                    icon = TodoIcons.Close,
                    label = stringResource(R.string.settings_sign_out),
                    iconTint = palette.priority1,
                    labelColor = palette.priority1,
                    onClick = onSignOut
                )
            }

            item(key = "theme-header") {
                SectionHeader(title = stringResource(R.string.settings_appearance))
            }
            item(key = "theme-system") {
                ThemeOptionRow(
                    label = stringResource(R.string.settings_theme_system),
                    selected = themeMode == 0,
                    onClick = { onThemeChange(0) }
                )
            }
            item(key = "theme-light") {
                ThemeOptionRow(
                    label = stringResource(R.string.settings_theme_light),
                    selected = themeMode == 1,
                    onClick = { onThemeChange(1) }
                )
            }
            item(key = "theme-dark") {
                ThemeOptionRow(
                    label = stringResource(R.string.settings_theme_dark),
                    selected = themeMode == 2,
                    onClick = { onThemeChange(2) }
                )
            }

            item(key = "data-header") {
                SectionHeader(title = stringResource(R.string.settings_data))
            }
            item(key = "clear-completed") {
                NavRow(
                    icon = TodoIcons.Trash,
                    label = stringResource(R.string.settings_clear_completed),
                    iconTint = palette.priority1,
                    labelColor = palette.priority1,
                    badge = stringResource(R.string.settings_count_items, state.completed.size),
                    onClick = onClearCompleted
                )
            }

            item(key = "about-header") {
                SectionHeader(title = stringResource(R.string.settings_about))
            }
            item(key = "version") {
                NavRow(
                    icon = TodoIcons.Hash,
                    label = stringResource(R.string.settings_version),
                    badge = "1.0.0",
                    onClick = {}
                )
            }
            item(key = "build") {
                NavRow(
                    icon = TodoIcons.List,
                    label = stringResource(R.string.settings_built_with),
                    onClick = {}
                )
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
                text = stringResource(R.string.settings_signed_in_hint),
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
                contentDescription = stringResource(R.string.cd_selected),
                tint = palette.brand,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
