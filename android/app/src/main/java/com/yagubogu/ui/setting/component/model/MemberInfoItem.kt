package com.yagubogu.ui.setting.component.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class MemberInfoItem(
    val nickName: String = "",
    val createdAt: LocalDate = LocalDate.now(),
    val favoriteTeam: String = "",
    val profileImageUrl: String = "",
) {
    val memberPeriod: Int
        get() = ChronoUnit.DAYS.between(createdAt, LocalDate.now()).toInt() + 1
}
