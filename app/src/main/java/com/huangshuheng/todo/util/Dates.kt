package com.huangshuheng.todo.util

import java.util.Calendar

/**
 * 日期工具。全部基于 [Calendar]，避开 java.time 在 minSdk 24 上的脱糖问题。
 * 约定：dueDate 统一存“当天 00:00”的时间戳。
 *
 * 这里**只做计算不做文案**：需要「今天 / 明天 / 周二」这类文字时，
 * 用 ui/format/DateLabels.kt 里的组合函数，文案走 strings.xml 以便本地化。
 */
object Dates {

    /** 相对今天的档位，具体显示什么文字由 UI 层决定 */
    enum class RelativeDay { TODAY, TOMORROW, YESTERDAY, WITHIN_WEEK, LATER, EARLIER }

    fun startOfDay(timeMillis: Long = System.currentTimeMillis()): Long {
        val c = Calendar.getInstance()
        c.timeInMillis = timeMillis
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    fun today(): Long = startOfDay()

    fun plusDays(dayStart: Long, days: Int): Long {
        val c = Calendar.getInstance()
        c.timeInMillis = dayStart
        c.add(Calendar.DAY_OF_YEAR, days)
        return startOfDay(c.timeInMillis)
    }

    fun hourOfDay(): Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

    /** 儒略日序号，用于安全地做日期差（跨月、跨年都正确）。 */
    private fun dayIndex(timeMillis: Long): Long {
        val c = Calendar.getInstance()
        c.timeInMillis = timeMillis
        val y = c.get(Calendar.YEAR).toLong()
        val m = c.get(Calendar.MONTH).toLong() + 1
        val d = c.get(Calendar.DAY_OF_MONTH).toLong()
        val a = (14 - m) / 12
        val yy = y + 4800 - a
        val mm = m + 12 * a - 3
        return d + (153 * mm + 2) / 5 + 365 * yy + yy / 4 - yy / 100 + yy / 400 - 32045
    }

    /** 两个自然日相差几天：负数=在前，0=同一天。跨月跨年都正确 */
    fun daysBetween(fromDay: Long, toDay: Long): Int =
        (dayIndex(toDay) - dayIndex(fromDay)).toInt()

    /** 目标日期距今天几天：负数=逾期，0=今天，1=明天 */
    fun daysFromToday(dayStart: Long): Int = daysBetween(today(), dayStart)

    fun isToday(dayStart: Long?, today: Long = today()): Boolean =
        dayStart != null && daysBetween(today, dayStart) == 0

    fun isOverdue(dayStart: Long?, today: Long = today()): Boolean =
        dayStart != null && daysBetween(today, dayStart) < 0

    fun relativeDay(dayStart: Long, today: Long = today()): RelativeDay {
        val diff = daysBetween(today, dayStart)
        return when {
            diff == 0 -> RelativeDay.TODAY
            diff == 1 -> RelativeDay.TOMORROW
            diff == -1 -> RelativeDay.YESTERDAY
            diff in 2..6 -> RelativeDay.WITHIN_WEEK
            diff > 6 -> RelativeDay.LATER
            else -> RelativeDay.EARLIER
        }
    }
}
