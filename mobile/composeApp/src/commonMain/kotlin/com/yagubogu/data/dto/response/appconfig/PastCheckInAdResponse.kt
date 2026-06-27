package com.yagubogu.data.dto.response.appconfig

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PastCheckInAdResponse(
    @SerialName("is_enabled")
    val isEnabled: Boolean = true, // 광고 활성화 여부
    @SerialName("start_count")
    val startCount: Int = 2, // 광고가 처음 노출되는 과거 직관 추가 횟수
    @SerialName("interval")
    val interval: Int = 3, // 이후 반복 노출 간격
)
