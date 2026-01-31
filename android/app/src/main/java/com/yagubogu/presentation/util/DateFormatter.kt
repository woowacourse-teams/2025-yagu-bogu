package com.yagubogu.presentation.util

import java.time.format.DateTimeFormatter
import java.util.Locale

object DateFormatter {
    val yyyyMMdd: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

    val hhmm: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    // TODO 다국어 지원시 한국어 수정
    val amPmhhmm = DateTimeFormatter.ofPattern("a hh:mm", Locale.KOREAN)
}
