package com.huangshuheng.todo.util

import com.huangshuheng.todo.util.Dates.RelativeDay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class DatesTest {

    /** 构造一个指定年月日的“当天零点”，时间部分故意给个非零值，用来验证归一化 */
    private fun day(year: Int, month: Int, dayOfMonth: Int): Long {
        val c = Calendar.getInstance()
        c.clear()
        c.set(year, month - 1, dayOfMonth, 13, 45, 30)
        c.set(Calendar.MILLISECOND, 123)
        return Dates.startOfDay(c.timeInMillis)
    }

    @Test
    fun startOfDayNormalizesToMidnight() {
        val c = Calendar.getInstance().apply { timeInMillis = day(2026, 10, 27) }
        assertEquals(0, c.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, c.get(Calendar.MINUTE))
        assertEquals(0, c.get(Calendar.SECOND))
        assertEquals(0, c.get(Calendar.MILLISECOND))
    }

    @Test
    fun daysBetweenSameDayIsZero() {
        assertEquals(0, Dates.daysBetween(day(2026, 10, 27), day(2026, 10, 27)))
    }

    @Test
    fun daysBetweenAcrossMonthBoundary() {
        assertEquals(1, Dates.daysBetween(day(2026, 1, 31), day(2026, 2, 1)))
        assertEquals(-1, Dates.daysBetween(day(2026, 2, 1), day(2026, 1, 31)))
    }

    @Test
    fun daysBetweenAcrossYearBoundary() {
        assertEquals(1, Dates.daysBetween(day(2025, 12, 31), day(2026, 1, 1)))
        assertEquals(365, Dates.daysBetween(day(2025, 1, 1), day(2026, 1, 1)))
    }

    @Test
    fun daysBetweenHandlesLeapYear() {
        // 2028 是闰年
        assertEquals(1, Dates.daysBetween(day(2028, 2, 28), day(2028, 2, 29)))
        assertEquals(2, Dates.daysBetween(day(2028, 2, 28), day(2028, 3, 1)))
    }

    @Test
    fun plusDaysCrossesMonthAndYear() {
        val nov1 = Dates.plusDays(day(2026, 10, 31), 1)
        val c = Calendar.getInstance().apply { timeInMillis = nov1 }
        assertEquals(Calendar.NOVEMBER, c.get(Calendar.MONTH))
        assertEquals(1, c.get(Calendar.DAY_OF_MONTH))

        val nextYear = Calendar.getInstance().apply {
            timeInMillis = Dates.plusDays(day(2026, 12, 31), 1)
        }
        assertEquals(2027, nextYear.get(Calendar.YEAR))
        assertEquals(Calendar.JANUARY, nextYear.get(Calendar.MONTH))
    }

    @Test
    fun relativeDayClassifiesOffsets() {
        val today = day(2026, 10, 27)
        assertEquals(RelativeDay.TODAY, Dates.relativeDay(today, today))
        assertEquals(RelativeDay.TOMORROW, Dates.relativeDay(Dates.plusDays(today, 1), today))
        assertEquals(RelativeDay.YESTERDAY, Dates.relativeDay(Dates.plusDays(today, -1), today))
        assertEquals(RelativeDay.WITHIN_WEEK, Dates.relativeDay(Dates.plusDays(today, 3), today))
        assertEquals(RelativeDay.WITHIN_WEEK, Dates.relativeDay(Dates.plusDays(today, 6), today))
        assertEquals(RelativeDay.LATER, Dates.relativeDay(Dates.plusDays(today, 7), today))
        assertEquals(RelativeDay.EARLIER, Dates.relativeDay(Dates.plusDays(today, -2), today))
    }

    @Test
    fun isTodayAndIsOverdueRespectGivenToday() {
        val today = day(2026, 10, 27)
        assertTrue(Dates.isToday(today, today))
        assertFalse(Dates.isToday(Dates.plusDays(today, 1), today))
        assertTrue(Dates.isOverdue(Dates.plusDays(today, -1), today))
        assertFalse(Dates.isOverdue(today, today))
        assertFalse(Dates.isOverdue(null, today))
    }
}
