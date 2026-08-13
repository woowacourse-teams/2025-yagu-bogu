package com.yagubogu.data.dto.response.stats

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocationCheckInRankingDto(
    @SerialName("ranking")
    val ranking: Long, // 직관 랭킹
    @SerialName("memberId")
    val memberId: Long, // 회원 ID
    @SerialName("checkInCount")
    val checkInCount: Int, // 직관 인증 횟수
    @SerialName("nickname")
    val nickname: String, // 회원 닉네임
    @SerialName("imageUrl")
    val imageUrl: String, // 회원 프로필 이미지 url
    @SerialName("teamShortName")
    val teamShortName: String, // 팀 이름
)
