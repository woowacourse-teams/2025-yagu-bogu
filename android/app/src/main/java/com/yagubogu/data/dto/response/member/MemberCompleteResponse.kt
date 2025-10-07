package com.yagubogu.data.dto.response.member

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MemberCompleteResponse(
    @SerialName("url")
    val url: String                // 멤버 프로필에 저장된 정적 이미지 URL
)