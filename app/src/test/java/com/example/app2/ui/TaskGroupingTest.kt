package com.example.app2.ui

import com.example.app2.data.Task
import com.example.app2.util.Dates
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class TaskGroupingTest {

    private val today: Long = run {
        val c = Calendar.getInstance()
        c.clear()
        c.set(2026, Calendar.OCTOBER, 27, 0, 0, 0)
        Dates.startOfDay(c.timeInMillis)
    }

    private fun day(offset: Int): Long = Dates.plusDays(today, offset)

    private fun task(
        id: Long,
        title: String = "任务 $id",
        due: Long? = null,
        priority: Int = 4,
        projectId: Long? = null,
        completed: Boolean = false,
        sortOrder: Int = 0
    ) = Task(
        id = id,
        title = title,
        dueDate = due,
        priority = priority,
        projectId = projectId,
        isCompleted = completed,
        createdAt = 0L,
        sortOrder = sortOrder
    )

    @Test
    fun homeGroupsOverdueAndToday() {
        val tasks = listOf(
            task(1, due = day(-1)),
            task(2, due = day(0)),
            task(3, due = day(1)),
            task(4, due = null)
        )

        val sections = TaskGrouping.sections(TodoView.HOME, null, tasks, today)

        assertEquals(2, sections.size)
        assertEquals("逾期", sections[0].title)
        assertEquals(SectionTone.OVERDUE, sections[0].tone)
        assertEquals(listOf(1L), sections[0].tasks.map { it.id })
        assertEquals("今天", sections[1].title)
        assertEquals(listOf(2L), sections[1].tasks.map { it.id })
    }

    @Test
    fun homeHasNoSectionsWhenNothingIsDueToday() {
        val tasks = listOf(task(1, due = day(3)), task(2, due = null))
        assertTrue(TaskGrouping.sections(TodoView.HOME, null, tasks, today).isEmpty())
    }

    @Test
    fun completedTasksAreExcludedFromActiveViews() {
        val tasks = listOf(
            task(1, due = day(0), completed = true),
            task(2, due = day(0))
        )
        val sections = TaskGrouping.sections(TodoView.HOME, null, tasks, today)
        assertEquals(listOf(2L), sections.first().tasks.map { it.id })
    }

    @Test
    fun inboxOnlyContainsTasksWithoutProject() {
        val tasks = listOf(
            task(1, projectId = null),
            task(2, projectId = 99L),
            task(3, projectId = null, completed = true)
        )
        val sections = TaskGrouping.sections(TodoView.INBOX, null, tasks, today)

        assertEquals(1, sections.size)
        assertEquals("收件箱", sections[0].title)
        assertEquals(listOf(1L), sections[0].tasks.map { it.id })
    }

    @Test
    fun upcomingGroupsByDateAscendingAndExcludesOverdue() {
        val tasks = listOf(
            task(1, due = day(3)),
            task(2, due = day(1)),
            task(3, due = day(-2)),
            task(4, due = null)
        )
        val sections = TaskGrouping.sections(TodoView.UPCOMING, null, tasks, today)

        assertEquals(3, sections.size)
        assertEquals(listOf(2L), sections[0].tasks.map { it.id })
        assertEquals(listOf(1L), sections[1].tasks.map { it.id })
        assertEquals("没有日期", sections[2].title)
        assertEquals(SectionTone.NORMAL, sections[0].tone)
    }

    @Test
    fun projectViewFiltersByProjectAndSplitsSections() {
        val tasks = listOf(
            task(1, due = day(-1), projectId = 7L),
            task(2, due = day(0), projectId = 7L),
            task(3, due = day(5), projectId = 7L),
            task(4, due = null, projectId = 7L),
            task(5, due = day(0), projectId = 8L)
        )
        val sections = TaskGrouping.sections(TodoView.PROJECT, 7L, tasks, today)

        assertEquals(listOf("逾期", "今天", "之后", "没有日期"), sections.map { it.title })
        assertEquals(listOf(1L), sections[0].tasks.map { it.id })
        assertEquals(listOf(2L), sections[1].tasks.map { it.id })
        assertEquals(listOf(3L), sections[2].tasks.map { it.id })
        assertEquals(listOf(4L), sections[3].tasks.map { it.id })
    }

    @Test
    fun sortPutsHigherPriorityFirst() {
        val tasks = listOf(
            task(1, priority = 4, sortOrder = 0),
            task(2, priority = 3, sortOrder = 1),
            task(3, priority = 1, sortOrder = 2)
        )
        assertEquals(listOf(3L, 2L, 1L), TaskGrouping.sort(tasks).map { it.id })
    }

    @Test
    fun sortRespectsManualOrderWithinSamePriority() {
        val tasks = listOf(
            task(1, priority = 2, sortOrder = 5),
            task(2, priority = 2, sortOrder = 1),
            task(3, priority = 2, sortOrder = 9)
        )
        assertEquals(listOf(2L, 1L, 3L), TaskGrouping.sort(tasks).map { it.id })
    }
}
