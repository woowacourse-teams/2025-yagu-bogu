package com.yagubogu.data.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RefreshResponse(
    @SerialName("accessToken")
    val accessToken: String, // access 토큰
    @SerialName("refreshToken")
    val refreshToken: String, // refresh 토큰
)
