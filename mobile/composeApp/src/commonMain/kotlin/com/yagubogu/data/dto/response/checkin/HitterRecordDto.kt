package com.yagubogu.data.dto.response.checkin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HitterRecordDto(
    @SerialName("battingOrder")
    val battingOrder: Int, // 타순 (1~9)
    @SerialName("position")
    val position: String, // 수비 포지션
    @SerialName("playerName")
    val playerName: String, // 선수 이름
    @SerialName("atBats")
    val atBats: Int, // 타수
    @SerialName("hits")
    val hits: Int, // 안타 수
    @SerialName("rbi")
    val rbi: Int, // 타점
    @SerialName("runs")
    val runs: Int, // 득점
)
