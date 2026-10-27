package com.huangshuheng.todo.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.huangshuheng.todo.R
import com.huangshuheng.todo.ui.TodoUiState
import com.huangshuheng.todo.ui.components.EmptyState
import com.huangshuheng.todo.ui.components.HairLine
import com.huangshuheng.todo.ui.components.TaskActions
import com.huangshuheng.todo.ui.components.TaskRowWithMenu
import com.huangshuheng.todo.ui.icons.TodoIcons
import com.huangshuheng.todo.ui.theme.TodoTheme

/** 搜索：标题与描述都参与匹配，结果点开还是同一个编辑面板 */
@Composable
fun SearchScreen(
    state: TodoUiState,
    actions: TaskActions,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit
) {
    val palette = TodoTheme.palette
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        runCatching { focusRequester.requestFocus() }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 6.dp, end = 12.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = TodoIcons.ChevronLeft,
                    contentDescription = stringResource(R.string.action_back),
                    tint = palette.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
            BasicTextField(
                value = state.searchQuery,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = palette.textPrimary),
                cursorBrush = SolidColor(palette.brand),
                decorationBox = { inner ->
                    Box {
                        if (state.searchQuery.isEmpty()) {
                            Text(
                                text = stringResource(R.string.search_hint),
                                style = MaterialTheme.typography.bodyLarge,
                                color = palette.textTertiary
                            )
                        }
                        inner()
                    }
                }
            )
            if (state.searchQuery.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = TodoIcons.Close,
                        contentDescription = stringResource(R.string.cd_clear_input),
                        tint = palette.textTertiary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }

        HairLine()

        when {
            state.searchQuery.isBlank() -> EmptyState(
                icon = TodoIcons.Search,
                title = stringResource(R.string.search_idle_title),
                message = stringResource(R.string.search_idle_message)
            )

            state.searchResults.isEmpty() -> EmptyState(
                icon = TodoIcons.Search,
                title = stringResource(R.string.search_empty_title),
                message = stringResource(R.string.search_empty_message)
            )

            else -> {
                Text(
                    text = stringResource(R.string.search_result_count, state.searchResults.size),
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.textTertiary,
                    modifier = Modifier.padding(start = 20.dp, top = 12.dp, bottom = 4.dp)
                )
                LazyColumn {
                    items(items = state.searchResults, key = { it.id }) { task ->
                        TaskRowWithMenu(
                            task = task,
                            project = state.projects.firstOrNull { it.id == task.projectId },
                            actions = actions,
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }
}
