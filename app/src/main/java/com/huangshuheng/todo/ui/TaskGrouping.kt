package com.huangshuheng.todo.ui

import com.huangshuheng.todo.data.Task
import com.huangshuheng.todo.util.Dates

/**
 * 任务分组与排序的纯逻辑。
 * 刻意不依赖 Android / ViewModel，也不自己读系统时间（today 由外部传入），
 * 这样可以在 JVM 单元测试里稳定验证。
 *
 * 分区标题只给“类型”（[SectionKind]），具体文字由 UI 层查资源，
 * 保证领域逻辑里不出现任何文案。
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
                        add(
                            TaskSection(
                                key = "overdue",
                                kind = SectionKind.OVERDUE,
                                tasks = sort(overdue),
                                tone = SectionTone.OVERDUE
                            )
                        )
                    }
                    if (dueToday.isNotEmpty()) {
                        add(
                            TaskSection(
                                key = "today",
                                kind = SectionKind.TODAY,
                                tasks = sort(dueToday),
                                tone = SectionTone.TODAY
                            )
                        )
                    }
                }
            }

            TodoView.INBOX -> {
                val inbox = active.filter { it.projectId == null }
                if (inbox.isEmpty()) emptyList()
                else listOf(
                    TaskSection(
                        key = "inbox",
                        kind = SectionKind.INBOX,
                        tasks = sort(inbox)
                    )
                )
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
                                kind = SectionKind.DATE,
                                tasks = sort(list),
                                date = day,
                                tone = if (Dates.isToday(day, today)) SectionTone.TODAY
                                else SectionTone.NORMAL
                            )
                        )
                    }
                    val undated = active.filter { it.dueDate == null }
                    if (undated.isNotEmpty()) {
                        add(
                            TaskSection(
                                key = "undated",
                                kind = SectionKind.UNDATED,
                                tasks = sort(undated)
                            )
                        )
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
                        add(
                            TaskSection(
                                key = "p-overdue",
                                kind = SectionKind.OVERDUE,
                                tasks = sort(overdue),
                                tone = SectionTone.OVERDUE
                            )
                        )
                    }
                    if (dueToday.isNotEmpty()) {
                        add(
                            TaskSection(
                                key = "p-today",
                                kind = SectionKind.TODAY,
                                tasks = sort(dueToday),
                                tone = SectionTone.TODAY
                            )
                        )
                    }
                    if (later.isNotEmpty()) {
                        add(
                            TaskSection(
                                key = "p-later",
                                kind = SectionKind.AFTER,
                                tasks = sort(later)
                            )
                        )
                    }
                    if (undated.isNotEmpty()) {
                        add(
                            TaskSection(
                                key = "p-undated",
                                kind = SectionKind.UNDATED,
                                tasks = sort(undated)
                            )
                        )
                    }
                }
            }
        }
    }
}
