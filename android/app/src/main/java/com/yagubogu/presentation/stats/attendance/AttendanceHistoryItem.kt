package com.yagubogu.presentation.stats.attendance

import java.time.LocalDate

data class AttendanceHistoryItem(
    val awayTeam: TeamItem,
    val homeTeam: TeamItem,
    val date: LocalDate,
    val stadiumName: String,
)
