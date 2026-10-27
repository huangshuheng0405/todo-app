package com.example.app2.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 日期工具。全部基于 [Calendar]，避开 java.time 在 minSdk 24 上的脱糖问题。
 * 约定：dueDate 统一存“当天 00:00”的时间戳。
 */
object Dates {

    private val WEEKDAYS = arrayOf("周日", "周一", "周二", "周三", "周四", "周五", "周六")

    private const val DAY_MS = 24L * 60 * 60 * 1000

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

    fun tomorrow(): Long = plusDays(today(), 1)

    fun plusDays(dayStart: Long, days: Int): Long {
        val c = Calendar.getInstance()
        c.timeInMillis = dayStart
        c.add(Calendar.DAY_OF_YEAR, days)
        return startOfDay(c.timeInMillis)
    }

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

    fun isTomorrow(dayStart: Long?, today: Long = today()): Boolean =
        dayStart != null && daysBetween(today, dayStart) == 1

    private fun weekdayOf(dayStart: Long): String {
        val c = Calendar.getInstance()
        c.timeInMillis = dayStart
        return WEEKDAYS[c.get(Calendar.DAY_OF_WEEK) - 1]
    }

    private fun monthDay(dayStart: Long): String =
        SimpleDateFormat("M月d日", Locale.CHINA).format(Date(dayStart))

    /** 列表里用的简短标签：今天 / 明天 / 昨天 / 周四 / 10月30日 */
    fun shortLabel(dayStart: Long, today: Long = today()): String {
        val diff = daysBetween(today, dayStart)
        return when {
            diff == 0 -> "今天"
            diff == 1 -> "明天"
            diff == -1 -> "昨天"
            diff in 2..6 -> weekdayOf(dayStart)
            else -> monthDay(dayStart)
        }
    }

    /** 面板里用的完整标签：10月27日 周二 */
    fun fullLabel(dayStart: Long): String = "${monthDay(dayStart)} ${weekdayOf(dayStart)}"

    /** 页面副标题：10月27日 · 周二 */
    fun headerLabel(dayStart: Long = today()): String =
        "${monthDay(dayStart)} · ${weekdayOf(dayStart)}"

    fun now(): Long = System.currentTimeMillis()

    fun formatTime(millis: Long): String =
        SimpleDateFormat("HH:mm", Locale.CHINA).format(Date(millis))

    fun formatDateTime(millis: Long): String =
        SimpleDateFormat("M月d日 HH:mm", Locale.CHINA).format(Date(millis))

    /** 一周的问候语，Todoist 首页开头就是这句 */
    fun greeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..8 -> "早上好"
            in 9..11 -> "上午好"
            in 12..13 -> "中午好"
            in 14..17 -> "下午好"
            in 18..22 -> "晚上好"
            else -> "夜深了"
        }
    }

    @Suppress("unused")
    fun dayMs(): Long = DAY_MS
}
