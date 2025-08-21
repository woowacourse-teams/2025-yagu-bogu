package com.yagubogu.data.dto.request.member

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MemberFavoriteRequest(
    @SerialName("teamCode")
    val teamCode: String,
)
