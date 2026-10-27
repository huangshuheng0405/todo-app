package com.huangshuheng.todo.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.huangshuheng.todo.R
import com.huangshuheng.todo.ui.TodoUiState
import com.huangshuheng.todo.ui.TodoView
import com.huangshuheng.todo.ui.components.NavRow
import com.huangshuheng.todo.ui.components.PageHeader
import com.huangshuheng.todo.ui.components.SectionHeader
import com.huangshuheng.todo.ui.icons.TodoIcons
import com.huangshuheng.todo.ui.theme.TodoTheme

/** 浏览：所有视图入口 + 项目列表 */
@Composable
fun BrowseScreen(
    state: TodoUiState,
    onOpenView: (TodoView, Long?) -> Unit,
    onAddProject: (String) -> Unit
) {
    val palette = TodoTheme.palette
    var dialogVisible by remember { mutableStateOf(false) }
    var projectName by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        PageHeader(
            title = stringResource(R.string.tab_browse),
            subtitle = stringResource(R.string.browse_subtitle)
        )

        LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
            item(key = "inbox") {
                NavRow(
                    icon = TodoIcons.Inbox,
                    label = stringResource(R.string.view_inbox),
                    badge = state.inboxCount.toString(),
                    onClick = { onOpenView(TodoView.INBOX, null) }
                )
            }
            item(key = "today") {
                NavRow(
                    icon = TodoIcons.Today,
                    label = stringResource(R.string.view_today),
                    badge = state.todayCount.toString(),
                    onClick = { onOpenView(TodoView.TODAY, null) }
                )
            }
            item(key = "upcoming") {
                NavRow(
                    icon = TodoIcons.Upcoming,
                    label = stringResource(R.string.view_upcoming),
                    badge = state.upcomingCount.toString(),
                    onClick = { onOpenView(TodoView.UPCOMING, null) }
                )
            }

            item(key = "projects-header") {
                Spacer(modifier = Modifier.height(10.dp))
                SectionHeader(
                    title = stringResource(R.string.browse_projects),
                    count = state.projects.size
                )
            }

            items(items = state.projects, key = { "project-${it.id}" }) { project ->
                NavRow(
                    icon = TodoIcons.Hash,
                    label = project.name,
                    iconTint = Color(project.color),
                    badge = (state.countsByProject[project.id] ?: 0).toString(),
                    showChevron = true,
                    onClick = { onOpenView(TodoView.PROJECT, project.id) }
                )
            }

            item(key = "add-project") {
                NavRow(
                    icon = TodoIcons.Add,
                    label = stringResource(R.string.browse_new_project),
                    iconTint = palette.brand,
                    labelColor = palette.brand,
                    onClick = { dialogVisible = true }
                )
            }
        }
    }

    if (dialogVisible) {
        AlertDialog(
            onDismissRequest = { dialogVisible = false },
            title = { Text(stringResource(R.string.browse_new_project)) },
            text = {
                OutlinedTextField(
                    value = projectName,
                    onValueChange = { projectName = it },
                    placeholder = { Text(stringResource(R.string.browse_new_project_hint)) },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onAddProject(projectName)
                        projectName = ""
                        dialogVisible = false
                    }
                ) { Text(stringResource(R.string.action_create)) }
            },
            dismissButton = {
                TextButton(onClick = { dialogVisible = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}
