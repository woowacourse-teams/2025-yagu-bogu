package com.yagubogu.data.dto.response.checkin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CheckInMemoResponse(
    @SerialName("memo")
    val memo: String?, // 메모 내용 (없으면 null)
)
