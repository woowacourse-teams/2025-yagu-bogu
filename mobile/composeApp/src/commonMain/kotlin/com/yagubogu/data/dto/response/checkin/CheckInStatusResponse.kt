package com.yagubogu.data.dto.response.checkin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CheckInStatusResponse(
    @SerialName("isCheckIn")
    val isCheckIn: Boolean, // 인증 여부 확인
)
