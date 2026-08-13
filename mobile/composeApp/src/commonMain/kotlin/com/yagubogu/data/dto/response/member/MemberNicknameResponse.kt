package com.yagubogu.data.dto.response.member

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MemberNicknameResponse(
    @SerialName("nickname")
    val nickname: String, // 유저 닉네임
)
