package com.yagubogu.data.dto.response.appconfig

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PastCheckInAdResponse(
    @SerialName("is_enabled")
    val isEnabled: Boolean = true,
    @SerialName("start_count")
    val startCount: Int = 2,
    @SerialName("interval")
    val interval: Int = 3,
)
