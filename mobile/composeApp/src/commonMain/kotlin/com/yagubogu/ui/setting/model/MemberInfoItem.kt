package com.yagubogu.ui.setting.model

import com.yagubogu.ui.util.now
import kotlinx.datetime.LocalDate

data class MemberInfoItem(
    val nickName: String = "",
    val createdAt: LocalDate = LocalDate.now(),
    val memberPeriod: Int = 1,
    val favoriteTeam: String = "",
    val profileImageUrl: String = "",
)
