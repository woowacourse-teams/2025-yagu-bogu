package com.yagubogu.domain.util

import kotlinx.datetime.DateTimeUnit
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

fun LocalDateTime.formatToAmPm(
    amText: String,
    pmText: String,
): String {
    val marker: String = if (hour < 12) amText else pmText
    val hour12: Int = if (hour % 12 == 0) 12 else hour % 12
    val minuteStr: String = minute.toString().padStart(2, '0')

    return "$marker $hour12:$minuteStr"
}
