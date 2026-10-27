package com.example.app2.ui.screens

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
import androidx.compose.ui.unit.dp
import com.example.app2.ui.TodoUiState
import com.example.app2.ui.TodoView
import com.example.app2.ui.components.NavRow
import com.example.app2.ui.components.PageHeader
import com.example.app2.ui.components.SectionHeader
import com.example.app2.ui.icons.TodoIcons
import com.example.app2.ui.theme.TodoTheme

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
        PageHeader(title = "浏览", subtitle = "所有视图与项目")

        LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
            item(key = "inbox") {
                NavRow(
                    icon = TodoIcons.Inbox,
                    label = "收件箱",
                    badge = state.inboxCount.toString(),
                    onClick = { onOpenView(TodoView.INBOX, null) }
                )
            }
            item(key = "today") {
                NavRow(
                    icon = TodoIcons.Today,
                    label = "今天",
                    badge = state.todayCount.toString(),
                    onClick = { onOpenView(TodoView.TODAY, null) }
                )
            }
            item(key = "upcoming") {
                NavRow(
                    icon = TodoIcons.Upcoming,
                    label = "即将到来",
                    badge = state.upcomingCount.toString(),
                    onClick = { onOpenView(TodoView.UPCOMING, null) }
                )
            }

            item(key = "projects-header") {
                Spacer(modifier = Modifier.height(10.dp))
                SectionHeader(title = "项目", count = state.projects.size)
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
                    label = "新建项目",
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
            title = { Text("新建项目") },
            text = {
                OutlinedTextField(
                    value = projectName,
                    onValueChange = { projectName = it },
                    placeholder = { Text("例如：装修、读书计划") },
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
                ) { Text("创建") }
            },
            dismissButton = {
                TextButton(onClick = { dialogVisible = false }) { Text("取消") }
            }
        )
    }
}
