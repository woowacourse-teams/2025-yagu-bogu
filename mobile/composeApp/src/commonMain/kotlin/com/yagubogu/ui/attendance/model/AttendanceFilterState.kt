package com.yagubogu.ui.attendance.model

import kotlinx.datetime.YearMonth

data class AttendanceFilterState(
    val yearMonth: YearMonth,
    val isWinOnly: Boolean = false,
    val isYearly: Boolean = false,
)
