package com.yagubogu.data.dto.response.member

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MemberInfoResponse(
    @SerialName("nickname")
    val nickname: String, // 닉네임
    @SerialName("createdAt")
    val createdAt: String, // 가입일
    @SerialName("favoriteTeam")
    val favoriteTeam: String, // 나의 팀
    @SerialName("profileImageUrl")
    val profileImageUrl: String, // 프로필 이미지
)
