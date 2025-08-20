package com.yagubogu.data.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MemberNicknameRequest(
    @SerialName("nickname")
    val nickname: String,
)
