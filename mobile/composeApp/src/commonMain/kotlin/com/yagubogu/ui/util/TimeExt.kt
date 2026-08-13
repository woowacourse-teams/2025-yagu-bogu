package com.yagubogu.ui.util

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import kotlinx.datetime.yearMonth
import org.jetbrains.compose.resources.StringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.day_friday
import yagubogu.composeapp.generated.resources.day_monday
import yagubogu.composeapp.generated.resources.day_saturday
import yagubogu.composeapp.generated.resources.day_sunday
import yagubogu.composeapp.generated.resources.day_thursday
import yagubogu.composeapp.generated.resources.day_tuesday
import yagubogu.composeapp.generated.resources.day_wednesday
import kotlin.time.Clock
import kotlin.time.Instant

private val KST: TimeZone = TimeZone.of("Asia/Seoul")

fun LocalDate.Companion.now(
    clock: Clock = Clock.System,
    timeZone: TimeZone = KST,
): LocalDate = clock.todayIn(timeZone)

fun LocalTime.Companion.now(
    clock: Clock = Clock.System,
    timeZone: TimeZone = KST,
): LocalTime = clock.now().toLocalDateTime(timeZone).time

fun LocalDateTime.Companion.now(
    clock: Clock = Clock.System,
    timeZone: TimeZone = KST,
): LocalDateTime = clock.now().toLocalDateTime(timeZone)

fun YearMonth.Companion.now(
    clock: Clock = Clock.System,
    timeZone: TimeZone = KST,
): YearMonth = clock.todayIn(timeZone).yearMonth

fun LocalDateTime.toInstantKST(): Instant = this.toInstant(KST)

fun LocalDate.toInstantKST(): Instant = this.atStartOfDayIn(KST)

fun LocalDate.minusDays(value: Int): LocalDate = minus(value, DateTimeUnit.DAY)

fun YearMonth.plusMonths(value: Int): YearMonth = plus(value, DateTimeUnit.MONTH)

fun YearMonth.minusMonths(value: Int): YearMonth = minus(value, DateTimeUnit.MONTH)

fun YearMonth.minusYears(value: Int): YearMonth = minus(value, DateTimeUnit.YEAR)

fun DayOfWeek.getDisplayNameResId(): StringResource =
    when (this) {
        DayOfWeek.MONDAY -> Res.string.day_monday
        DayOfWeek.TUESDAY -> Res.string.day_tuesday
        DayOfWeek.WEDNESDAY -> Res.string.day_wednesday
        DayOfWeek.THURSDAY -> Res.string.day_thursday
        DayOfWeek.FRIDAY -> Res.string.day_friday
        DayOfWeek.SATURDAY -> Res.string.day_saturday
        DayOfWeek.SUNDAY -> Res.string.day_sunday
    }

fun LocalDateTime.formatToAmPm(
    amText: String,
    pmText: String,
): String {
    val marker: String = if (hour < 12) amText else pmText
    val hour12: Int = if (hour % 12 == 0) 12 else hour % 12
    val minuteStr: String = minute.toString().padStart(2, '0')

    return "$marker $hour12:$minuteStr"
}
