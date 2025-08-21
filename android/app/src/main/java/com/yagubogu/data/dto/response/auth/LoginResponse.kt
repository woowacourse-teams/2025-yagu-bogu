package com.yagubogu.data.dto.response.auth

import com.yagubogu.data.dto.response.MemberDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    @SerialName("accessToken")
    val accessToken: String, // access 토큰
    @SerialName("refreshToken")
    val refreshToken: String, // refresh 토큰
    @SerialName("isNew")
    val isNew: Boolean, // 회원가입 여부 (true: 회원가입, false: 로그인)
    @SerialName("member")
    val member: MemberDto, // 사용자 정보
)
