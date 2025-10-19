package com.yagubogu.ui.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * LocalDate를 UTC 기준 epoch millis로 변환 (DatePicker용)
 */
fun LocalDate.toEpochMillisForDatePicker(): Long =
    this
        .atStartOfDay(ZoneOffset.UTC)
        .toInstant()
        .toEpochMilli()

/**
 * DatePicker의 epoch millis를 LocalDate로 변환 (UTC 기준)
 */
fun Long.toLocalDateFromDatePicker(): LocalDate =
    Instant
        .ofEpochMilli(this)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()

/**
 * LocalDate를 epoch millis로 변환
 */
fun LocalDate.toEpochMillis(): Long =
    this
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()

/**
 * epoch millis를 LocalDate로 변환
 */
fun Long.toLocalDate(): LocalDate =
    Instant
        .ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()

/**
 * LocalDate 포맷팅
 */
fun formatLocalDate(date: LocalDate): String = date.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))
