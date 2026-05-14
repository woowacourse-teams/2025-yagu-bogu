package com.yagubogu.ui.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.char

// yyyy.MM.dd 포맷터
val yyyyMMddFormatter: DateTimeFormat<LocalDate> =
    LocalDate
        .Format {
            year()
            char('.')
            monthNumber()
            char('.')
            day()
        }

// yyyy.MM.dd (요일) 포맷터 (예: 2025.08.14 (목))
val yyyyMMddDayOfWeekFormatter: DateTimeFormat<LocalDate> =
    LocalDate
        .Format {
            year()
            char('.')
            monthNumber()
            char('.')
            day()
            chars(" (")
            dayOfWeek(DayOfWeekNames("월", "화", "수", "목", "금", "토", "일"))
            char(')')
        }

// hh:mm 포맷터
val hhmmFormatter: DateTimeFormat<LocalTime> =
    LocalTime
        .Format {
            hour()
            char(':')
            minute()
        }

// MM.dd hh:mm 포맷터
val MMddhhmmFormatter: DateTimeFormat<LocalDateTime> =
    LocalDateTime.Format {
        monthNumber()
        char('.')
        day()
        char(' ')
        hour()
        char(':')
        minute()
    }
