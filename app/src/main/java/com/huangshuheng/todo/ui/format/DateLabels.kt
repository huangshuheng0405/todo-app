package com.huangshuheng.todo.ui.format

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.huangshuheng.todo.R
import com.huangshuheng.todo.util.Dates
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 日期的显示格式。放在 UI 层是因为：
 * 1) 需要 `stringResource` 取本地化文案；
 * 2) 月日/星期的格式要跟随系统语言（中文「10月27日 周二」/ 英文「Oct 27 Tue」）。
 */
private fun format(millis: Long, skeleton: String): String {
    val locale = Locale.getDefault()
    val pattern = DateFormat.getBestDateTimePattern(locale, skeleton)
    return SimpleDateFormat(pattern, locale).format(Date(millis))
}

/** 月日：10月27日 / Oct 27 */
fun monthDayLabel(day: Long): String = format(day, "MMMd")

/** 星期：周二 / Tue */
fun weekdayLabel(day: Long): String =
    SimpleDateFormat("EEE", Locale.getDefault()).format(Date(day))

/** 完整日期：10月27日 周二 */
fun fullDateLabel(day: Long): String = "${monthDayLabel(day)} ${weekdayLabel(day)}"

/** 日期 + 时间：10月27日 14:30 */
fun dateTimeLabel(millis: Long): String = format(millis, "MMMdHm")

/** 列表里的简短标签：今天 / 明天 / 昨天 / 周二 / 10月30日 */
@Composable
fun dayShortLabel(day: Long, today: Long = Dates.today()): String =
    when (Dates.relativeDay(day, today)) {
        Dates.RelativeDay.TODAY -> stringResource(R.string.date_today)
        Dates.RelativeDay.TOMORROW -> stringResource(R.string.date_tomorrow)
        Dates.RelativeDay.YESTERDAY -> stringResource(R.string.date_yesterday)
        Dates.RelativeDay.WITHIN_WEEK -> weekdayLabel(day)
        Dates.RelativeDay.LATER, Dates.RelativeDay.EARLIER -> monthDayLabel(day)
    }

/** 首页副标题里的日期部分：10月27日 周二 */
@Composable
fun todayHeaderLabel(): String = fullDateLabel(Dates.today())

/** 按时间段给个问候语 */
@Composable
fun greetingLabel(): String = when (Dates.hourOfDay()) {
    in 5..8 -> stringResource(R.string.greeting_morning)
    in 9..11 -> stringResource(R.string.greeting_forenoon)
    in 12..13 -> stringResource(R.string.greeting_noon)
    in 14..17 -> stringResource(R.string.greeting_afternoon)
    in 18..22 -> stringResource(R.string.greeting_evening)
    else -> stringResource(R.string.greeting_night)
}
