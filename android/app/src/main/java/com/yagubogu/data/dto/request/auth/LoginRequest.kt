package com.yagubogu.data.dto.request.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    @SerialName("idToken")
    val idToken: String, // Google SDK에서 발급 받은 ID 토큰
)
