package com.yagubogu.data.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MemberFavoriteResponse(
    @SerialName("favorite")
    val favorite: String, // 팀 이름
)
