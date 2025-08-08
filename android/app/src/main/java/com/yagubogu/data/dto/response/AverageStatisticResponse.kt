package com.yagubogu.data.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AverageStatisticResponse(
    @SerialName("averageRun")
    val averageRun: Double?, // 평균 득점
    @SerialName("concededRuns")
    val concededRuns: Double?, // 평균 실점
    @SerialName("averageErrors")
    val averageErrors: Double?, // 평균 실책
    @SerialName("averageHits")
    val averageHits: Double?, // 평균 안타
    @SerialName("concededHits")
    val concededHits: Double?, // 평균 피안타
)
