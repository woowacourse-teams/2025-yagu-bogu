package com.yagubogu.data.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MemberDto(
    @SerialName("id")
    val id: Int, // 사용자 id
    @SerialName("nickname")
    val nickname: String, // 사용자 닉네임
    @SerialName("profileImageUrl")
    val profileImageUrl: String, // 사용자 프로필 이미지 url
)
