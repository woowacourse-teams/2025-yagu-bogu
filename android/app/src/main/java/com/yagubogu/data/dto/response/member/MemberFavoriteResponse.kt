package com.yagubogu.data.dto.response.member

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MemberFavoriteResponse(
    @SerialName("favorite")
    val favorite: String? = null, // 팀 이름
)
