package com.example.app2.ui

import com.example.app2.data.Task
import com.example.app2.util.Dates

/**
 * 任务分组与排序的纯逻辑。
 * 刻意不依赖 Android / ViewModel，也不自己读系统时间（today 由外部传入），
 * 这样可以在 JVM 单元测试里稳定验证。
 */
object TaskGrouping {

    /** 先按优先级（P1 在前），再按手动排序值，最后按创建时间倒序 */
    fun sort(tasks: List<Task>): List<Task> =
        tasks.sortedWith(compareBy({ it.priority }, { it.sortOrder }, { -it.createdAt }))

    fun sections(
        view: TodoView,
        projectId: Long?,
        allTasks: List<Task>,
        today: Long = Dates.today()
    ): List<TaskSection> {
        val active = allTasks.filter { !it.isCompleted }

        return when (view) {
            TodoView.HOME, TodoView.TODAY -> {
                val overdue = active.filter { Dates.isOverdue(it.dueDate, today) }
                val dueToday = active.filter { Dates.isToday(it.dueDate, today) }
                buildList {
                    if (overdue.isNotEmpty()) {
                        add(TaskSection("overdue", "逾期", sort(overdue), SectionTone.OVERDUE))
                    }
                    if (dueToday.isNotEmpty()) {
                        add(TaskSection("today", "今天", sort(dueToday), SectionTone.TODAY))
                    }
                }
            }

            TodoView.INBOX -> {
                val inbox = active.filter { it.projectId == null }
                if (inbox.isEmpty()) emptyList()
                else listOf(TaskSection("inbox", "收件箱", sort(inbox)))
            }

            TodoView.UPCOMING -> {
                val dated = active
                    .filter { it.dueDate != null && !Dates.isOverdue(it.dueDate, today) }
                    .groupBy { it.dueDate!! }
                    .toSortedMap()
                buildList {
                    dated.forEach { (day, list) ->
                        add(
                            TaskSection(
                                key = "day-$day",
                                title = Dates.fullLabel(day),
                                tasks = sort(list),
                                tone = if (Dates.isToday(day, today)) SectionTone.TODAY
                                else SectionTone.NORMAL
                            )
                        )
                    }
                    val undated = active.filter { it.dueDate == null }
                    if (undated.isNotEmpty()) {
                        add(TaskSection("undated", "没有日期", sort(undated)))
                    }
                }
            }

            TodoView.PROJECT -> {
                val inProject = active.filter { it.projectId == projectId }
                val overdue = inProject.filter { Dates.isOverdue(it.dueDate, today) }
                val dueToday = inProject.filter { Dates.isToday(it.dueDate, today) }
                val later = inProject.filter {
                    it.dueDate != null &&
                        !Dates.isOverdue(it.dueDate, today) &&
                        !Dates.isToday(it.dueDate, today)
                }
                val undated = inProject.filter { it.dueDate == null }
                buildList {
                    if (overdue.isNotEmpty()) {
                        add(TaskSection("p-overdue", "逾期", sort(overdue), SectionTone.OVERDUE))
                    }
                    if (dueToday.isNotEmpty()) {
                        add(TaskSection("p-today", "今天", sort(dueToday), SectionTone.TODAY))
                    }
                    if (later.isNotEmpty()) {
                        add(TaskSection("p-later", "之后", sort(later)))
                    }
                    if (undated.isNotEmpty()) {
                        add(TaskSection("p-undated", "没有日期", sort(undated)))
                    }
                }
            }
        }
    }
}
