package com.huangshuheng.todo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.huangshuheng.todo.TodoApp
import com.huangshuheng.todo.data.Project
import com.huangshuheng.todo.data.Task
import com.huangshuheng.todo.data.TodoRepository
import com.huangshuheng.todo.util.Dates
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TodoView { HOME, INBOX, TODAY, UPCOMING, PROJECT }

/** 分区标题的语气，用来决定是否标红 */
enum class SectionTone { NORMAL, OVERDUE, TODAY }

/** 分区类型：标题文案由 UI 层按这个类型去查 strings.xml */
enum class SectionKind { OVERDUE, TODAY, INBOX, AFTER, UNDATED, DATE }

data class TaskSection(
    val key: String,
    val kind: SectionKind,
    val tasks: List<Task>,
    /** kind == DATE 时有效（即将到来的按天分组） */
    val date: Long? = null,
    val tone: SectionTone = SectionTone.NORMAL
)

data class TodoUiState(
    val loaded: Boolean = false,
    val view: TodoView = TodoView.HOME,
    val projectId: Long? = null,
    val sections: List<TaskSection> = emptyList(),
    /** 首页固定展示「逾期 + 今天」，不随当前选中视图变化 */
    val homeSections: List<TaskSection> = emptyList(),
    val projects: List<Project> = emptyList(),
    val completed: List<Task> = emptyList(),
    val countsByProject: Map<Long, Int> = emptyMap(),
    val inboxCount: Int = 0,
    val todayCount: Int = 0,
    val upcomingCount: Int = 0,
    val searchQuery: String = "",
    val searchResults: List<Task> = emptyList(),
    /** 全量任务，供详情页按 id 反查（无论当前在哪个视图） */
    val allTasks: List<Task> = emptyList()
) {
    val visibleTaskCount: Int get() = sections.sumOf { it.tasks.size }

    fun projectOf(task: Task): Project? = projects.firstOrNull { it.id == task.projectId }
}

private data class Selection(val view: TodoView, val projectId: Long?)

/**
 * 某个登录用户的任务视图。
 * [ownerId] 在构造时注入，所有读写都带上它，因此换账号不会看到别人的数据。
 * 每个账号用独立的 ViewModel 实例（见 [factory] 的 key）。
 *
 * 这里只产出数据，不产出文案——标题、副标题都由 UI 层根据 view / 项目名拼装，
 * 避免中文写死在 ViewModel 里。
 */
class TodoViewModel(
    private val repo: TodoRepository,
    private val ownerId: String
) : ViewModel() {

    private val selection = MutableStateFlow(Selection(TodoView.HOME, null))
    private val searchQuery = MutableStateFlow("")

    init {
        // v1 遗留的无主数据（ownerId 为 null）在首次登录时认领给当前账号
        viewModelScope.launch { repo.claimLegacyData(ownerId) }
    }

    val ui: StateFlow<TodoUiState> = combine(
        repo.observeTasks(ownerId),
        repo.observeProjects(ownerId),
        selection,
        searchQuery
    ) { tasks, projects, sel, query ->
        buildState(tasks, projects, sel, query)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TodoUiState()
    )

    private fun buildState(
        tasks: List<Task>,
        projects: List<Project>,
        selection: Selection,
        query: String
    ): TodoUiState {
        val active = tasks.filter { !it.isCompleted }
        val today = Dates.today()
        val completed = tasks.filter { it.isCompleted }
            .sortedByDescending { it.completedAt ?: 0L }

        return TodoUiState(
            loaded = true,
            view = selection.view,
            projectId = selection.projectId,
            sections = TaskGrouping.sections(selection.view, selection.projectId, tasks, today),
            homeSections = TaskGrouping.sections(TodoView.HOME, null, tasks, today),
            projects = projects,
            completed = completed,
            countsByProject = active.filter { it.projectId != null }
                .groupingBy { it.projectId!! }
                .eachCount(),
            inboxCount = active.count { it.projectId == null },
            todayCount = active.count {
                Dates.isToday(it.dueDate, today) || Dates.isOverdue(it.dueDate, today)
            },
            upcomingCount = active.count {
                it.dueDate != null && !Dates.isOverdue(it.dueDate, today)
            },
            searchQuery = query,
            allTasks = tasks,
            searchResults = if (query.isBlank()) {
                emptyList()
            } else {
                val keyword = query.trim()
                TaskGrouping.sort(
                    active.filter {
                        it.title.contains(keyword, ignoreCase = true) ||
                            it.note.contains(keyword, ignoreCase = true)
                    }
                )
            }
        )
    }

    fun select(view: TodoView, projectId: Long? = null) {
        selection.value = Selection(view, projectId)
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    /** id == 0 → 新建，否则更新。写入前统一打上当前账号的 ownerId */
    fun upsert(task: Task) {
        val clean = task.copy(title = task.title.trim(), ownerId = ownerId)
        if (clean.title.isEmpty()) return
        viewModelScope.launch {
            if (clean.id == 0L) {
                repo.addTask(clean.copy(sortOrder = (System.currentTimeMillis() / 1000).toInt()))
            } else {
                repo.updateTask(clean)
            }
        }
    }

    fun toggleCompleted(task: Task) {
        viewModelScope.launch { repo.setCompleted(task.id, ownerId, !task.isCompleted) }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch { repo.deleteTask(task.id, ownerId) }
    }

    /** 撤销删除：以新 id 重新插入，保留原来的标题、日期、优先级与项目 */
    fun restoreTask(task: Task) {
        viewModelScope.launch {
            repo.addTask(
                task.copy(
                    id = 0L,
                    ownerId = ownerId,
                    sortOrder = (System.currentTimeMillis() / 1000).toInt()
                )
            )
        }
    }

    fun setPriority(task: Task, priority: Int) {
        viewModelScope.launch { repo.updateTask(task.copy(priority = priority, ownerId = ownerId)) }
    }

    fun setDueDate(task: Task, dueDate: Long?) {
        viewModelScope.launch { repo.updateTask(task.copy(dueDate = dueDate, ownerId = ownerId)) }
    }

    fun clearCompleted() {
        viewModelScope.launch { repo.deleteCompleted(ownerId) }
    }

    fun addProject(name: String) {
        val clean = name.trim()
        if (clean.isEmpty()) return
        viewModelScope.launch {
            val index = ui.value.projects.size
            repo.addProject(clean, PROJECT_COLORS[index % PROJECT_COLORS.size], ownerId)
        }
    }

    companion object {
        private val PROJECT_COLORS = intArrayOf(
            0xFF4073FF.toInt(),
            0xFF7ECC49.toInt(),
            0xFFFF9933.toInt(),
            0xFFAF38EB.toInt(),
            0xFF14AAF5.toInt(),
            0xFFEB8909.toInt()
        )

        /** 每个账号一个 ViewModel 实例，key 里带上 ownerId，换账号即换实例 */
        fun factory(ownerId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as TodoApp
                TodoViewModel(app.repository, ownerId)
            }
        }
    }
}
