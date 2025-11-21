package com.yagubogu.data.dto.response.member

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MemberProfileResponse(
    @SerialName("nickname")
    val nickname: String, // 회원 닉네임
    @SerialName("enterDate")
    val enterDate: LocalDate, // 회원 가입일 (YYYY-MM-DD)
    @SerialName("profileImageUrl")
    val profileImageUrl: String, // 프로필 이미지 주소
    @SerialName("favoriteTeam")
    val favoriteTeam: String, // 응원하는 팀
    @SerialName("representativeBadge")
    val representativeBadge: ProfileRepresentativeBadgeDto?, // 대표 배지 정보
    @SerialName("victoryFairy")
    val victoryFairyProfile: VictoryFairyProfileDto, // 승리 요정 정보
    @SerialName("checkIn")
    val checkIn: MemberCheckInDto, // 직관 인증 정보
)
