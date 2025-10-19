package com.yagubogu.ui.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
