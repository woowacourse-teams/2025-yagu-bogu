package com.yagubogu.ui.setting.component.model

import java.time.LocalDate

data class MemberInfoItem(
    val nickName: String = "",
    val createdAt: LocalDate = LocalDate.now(),
    val memberPeriod: Int = 1,
    val favoriteTeam: String = "",
    val profileImageUrl: String = "",
)
