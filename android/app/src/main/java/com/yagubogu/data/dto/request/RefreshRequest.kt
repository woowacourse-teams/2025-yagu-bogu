package com.yagubogu.data.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RefreshRequest(
    @SerialName("refreshToken")
    val refreshToken: String,
)
