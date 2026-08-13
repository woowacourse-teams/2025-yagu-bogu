package com.yagubogu.data.dto.response.checkin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CheckInReviewResponse(
    @SerialName("homeHitters")
    val homeHitters: List<HitterRecordDto>, // 홈팀 타자 기록 목록
    @SerialName("awayHitters")
    val awayHitters: List<HitterRecordDto>, // 원정팀 타자 기록 목록
    @SerialName("homePitchers")
    val homePitchers: List<PitcherRecordDto>, // 홈팀 투수 기록 목록
    @SerialName("awayPitchers")
    val awayPitchers: List<PitcherRecordDto>, // 원정팀 투수 기록 목록
)
