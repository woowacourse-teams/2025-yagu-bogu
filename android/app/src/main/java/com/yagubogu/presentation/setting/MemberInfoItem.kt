package com.yagubogu.presentation.setting

import java.time.LocalDate

data class MemberInfoItem(
    val nickName: String,
    val createdAt: LocalDate,
    val memberPeriod: Int,
    val favoriteTeam: String,
    val profileImageUrl: String,
)
