package com.yagubogu.data.dto.request.token

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TokenRequest(
    @SerialName("refreshToken")
    val refreshToken: String,
)
