package com.yagubogu.data.dto.response.token

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TokenResponse(
    @SerialName("accessToken")
    val accessToken: String, // access 토큰
    @SerialName("refreshToken")
    val refreshToken: String, // refresh 토큰
)
