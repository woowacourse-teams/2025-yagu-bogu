package com.yagubogu.domain.model

import kotlinx.datetime.LocalDateTime

enum class OpeningDate(
    val dateTime: LocalDateTime,
) {
    YEAR_2026(dateTime = LocalDateTime(2026, 3, 28, 14, 0)),
    ;

    companion object {
        fun fromYear(year: Int): OpeningDate? = entries.find { it.dateTime.year == year }
    }
}
