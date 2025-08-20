package com.yagubogu.presentation.setting

import com.yagubogu.domain.model.Team
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class MemberInfoItem(
    val nickName: String,
    val createdAt: LocalDate,
    val favoriteTeam: Team,
    val profileImageUrl: String,
) {
    val memberPeriod: Int
        get() = ChronoUnit.DAYS.between(createdAt, LocalDate.now()).toInt() + 1
}
