package com.yagubogu.data.dto.response.checkin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
// TODO: swagger 확인 불가능에 따라 클래스명 임시 정의
data class StadiumCountsResponse(
    @SerialName("stadiums")
    val stadiums: List<StadiumDto>,
)
