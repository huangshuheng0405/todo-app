package com.example.app2.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app2.auth.Session
import com.example.app2.ui.components.AddTaskButton
import com.example.app2.ui.components.HairLine
import com.example.app2.ui.components.TaskActions
import com.example.app2.ui.editor.TaskEditorSheet
import com.example.app2.ui.icons.TodoIcons
import com.example.app2.ui.screens.BrowseScreen
import com.example.app2.ui.screens.CompletedScreen
import com.example.app2.ui.screens.HomeScreen
import com.example.app2.ui.screens.SearchScreen
import com.example.app2.ui.screens.SettingsScreen
import com.example.app2.ui.screens.TaskDetailScreen
import com.example.app2.ui.screens.TaskViewScreen
import com.example.app2.ui.theme.TodoTheme
import com.example.app2.util.Dates
import kotlinx.coroutines.launch

enum class BottomTab(val label: String, val icon: ImageVector) {
    HOME("首页", TodoIcons.List),
    BROWSE("浏览", TodoIcons.Grid),
    COMPLETED("已完成", TodoIcons.DoneCircle),
    SETTINGS("设置", TodoIcons.Settings)
}

/**
 * 登录后的主界面。数据只包含当前 [session] 对应账号的内容，
 * 换账号时 ViewModel 的 key 变化，等于换了一个干净的状态。
 */
@Composable
fun TodoistRoot(
    session: Session,
    themeMode: Int,
    onThemeChange: (Int) -> Unit,
    onSignOut: () -> Unit,
    vm: TodoViewModel = viewModel(
        key = "todo-${session.userId}",
        factory = TodoViewModel.factory(session.userId)
    )
) {
    val state by vm.ui.collectAsStateWithLifecycle()

    var tab by rememberSaveable { mutableStateOf(BottomTab.HOME) }
    /** 收件箱 / 即将到来 / 某个项目 的列表页 */
    var listOpen by rememberSaveable { mutableStateOf(false) }
    var searchOpen by rememberSaveable { mutableStateOf(false) }
    /** 正在查看详情的任务 id */
    var openTaskId by rememberSaveable { mutableStateOf<Long?>(null) }
    var addingNew by remember { mutableStateOf(false) }

    val openTask = openTaskId?.let { id -> state.allTasks.firstOrNull { it.id == id } }

    // 任务被删掉 / 视图切换后自动收起详情
    LaunchedEffect(openTaskId, openTask) {
        if (openTaskId != null && openTask == null) openTaskId = null
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val actions = remember(vm, snackbarHostState) {
        TaskActions(
            onToggle = { task ->
                vm.toggleCompleted(task)
                // 勾选完成时给一次反悔机会；取消完成则不打扰
                if (!task.isCompleted) {
                    scope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = "已完成「${task.title}」",
                            actionLabel = "撤销",
                            withDismissAction = true,
                            duration = SnackbarDuration.Short
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            vm.toggleCompleted(task)
                        }
                    }
                }
            },
            onOpen = { task -> openTaskId = task.id },
            onSetPriority = { task, priority -> vm.setPriority(task, priority) },
            onSetToday = { task -> vm.setDueDate(task, Dates.today()) },
            onClearDate = { task -> vm.setDueDate(task, null) },
            onDelete = { task ->
                vm.deleteTask(task)
                scope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = "已删除「${task.title}」",
                        actionLabel = "撤销",
                        withDismissAction = true,
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        vm.restoreTask(task)
                    }
                }
            }
        )
    }

    BackHandler(enabled = searchOpen) {
        searchOpen = false
        vm.setSearchQuery("")
    }
    // 详情页自己处理返回（需要先提交再关闭），这里只在没有详情时管列表页
    BackHandler(enabled = !searchOpen && openTaskId == null && listOpen) {
        listOpen = false
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            // 任务详情页隐藏底部导航，进入更沉浸的单任务视图
            if (openTask == null) {
                TodoBottomBar(
                    current = tab,
                    onSelect = {
                        tab = it
                        listOpen = false
                        searchOpen = false
                        vm.setSearchQuery("")
                    },
                    onAdd = { addingNew = true }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val task = openTask
            when {
                searchOpen -> SearchScreen(
                    state = state,
                    actions = actions,
                    onQueryChange = { vm.setSearchQuery(it) },
                    onBack = {
                        searchOpen = false
                        vm.setSearchQuery("")
                    }
                )

                task != null -> TaskDetailScreen(
                    task = task,
                    projects = state.projects,
                    onClose = { openTaskId = null },
                    onSave = { vm.upsert(it) },
                    onToggleComplete = {
                        actions.onToggle(task)
                        // 完成后收起详情，回到列表（Snackbar 里还能撤销）
                        if (!task.isCompleted) openTaskId = null
                    },
                    onDelete = {
                        actions.onDelete(task)
                        openTaskId = null
                    }
                )

                listOpen -> TaskViewScreen(
                    state = state,
                    actions = actions,
                    onBack = { listOpen = false }
                )

                tab == BottomTab.HOME -> HomeScreen(
                    state = state,
                    actions = actions,
                    onOpenSearch = { searchOpen = true }
                )

                tab == BottomTab.BROWSE -> BrowseScreen(
                    state = state,
                    onOpenView = { targetView, projectId ->
                        vm.select(targetView, projectId)
                        listOpen = true
                    },
                    onAddProject = { name -> vm.addProject(name) }
                )

                tab == BottomTab.COMPLETED -> CompletedScreen(
                    state = state,
                    onToggle = { taskItem -> actions.onToggle(taskItem) },
                    onOpen = { taskItem -> openTaskId = taskItem.id },
                    onClear = { vm.clearCompleted() }
                )

                else -> SettingsScreen(
                    state = state,
                    themeMode = themeMode,
                    onThemeChange = onThemeChange,
                    onClearCompleted = { vm.clearCompleted() },
                    accountEmail = session.email,
                    onSignOut = onSignOut
                )
            }
        }
    }

    if (addingNew) {
        // 新建任务的默认属性跟着“当前看到的页面”走
        val defaultProject = if (listOpen && state.view == TodoView.PROJECT) {
            state.projectId
        } else null
        val defaultDate = when {
            !listOpen && tab == BottomTab.HOME -> Dates.today()
            listOpen && (state.view == TodoView.TODAY || state.view == TodoView.HOME) -> Dates.today()
            else -> null
        }

        TaskEditorSheet(
            initial = null,
            projects = state.projects,
            defaultProjectId = defaultProject,
            defaultDate = defaultDate,
            onDismiss = { addingNew = false },
            onSave = { task -> vm.upsert(task) }
        )
    }
}

@Composable
private fun TodoBottomBar(
    current: BottomTab,
    onSelect: (BottomTab) -> Unit,
    onAdd: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .navigationBarsPadding()
    ) {
        HairLine()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomTabItem(
                tab = BottomTab.HOME,
                selected = current == BottomTab.HOME,
                modifier = Modifier.weight(1f)
            ) { onSelect(BottomTab.HOME) }

            BottomTabItem(
                tab = BottomTab.BROWSE,
                selected = current == BottomTab.BROWSE,
                modifier = Modifier.weight(1f)
            ) { onSelect(BottomTab.BROWSE) }

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                AddTaskButton(onClick = onAdd, size = 44)
            }

            BottomTabItem(
                tab = BottomTab.COMPLETED,
                selected = current == BottomTab.COMPLETED,
                modifier = Modifier.weight(1f)
            ) { onSelect(BottomTab.COMPLETED) }

            BottomTabItem(
                tab = BottomTab.SETTINGS,
                selected = current == BottomTab.SETTINGS,
                modifier = Modifier.weight(1f)
            ) { onSelect(BottomTab.SETTINGS) }
        }
    }
}

@Composable
private fun RowScope.BottomTabItem(
    tab: BottomTab,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val palette = TodoTheme.palette
    val tint = if (selected) palette.brand else palette.textTertiary
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = tab.label,
            tint = tint,
            modifier = Modifier.size(21.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = tab.label,
            color = tint,
            fontSize = 10.sp
        )
    }
}
