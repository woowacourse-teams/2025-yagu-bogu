package com.yagubogu.data.dto.response.member

import com.yagubogu.ui.dialog.model.MemberProfile
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
    val representativeBadge: RepresentativeBadgeDto, // 대표 배지 정보
    @SerialName("victoryFairy")
    val victoryFairy: VictoryFairyDto, // 승리 요정 정보
    @SerialName("checkIn")
    val checkIn: CheckInDto, // 직관 인증 정보
) {
    fun toPresentation(): MemberProfile =
        MemberProfile(
            nickname = nickname,
            enterDate = enterDate,
            profileImageUrl = profileImageUrl,
            favoriteTeam = favoriteTeam,
            representativeBadgeName = representativeBadge.name,
            representativeBadgeImageUrl = representativeBadge.badgeImageUrl,
            victoryFairyRanking = victoryFairy.ranking,
            victoryFairyScore = victoryFairy.score,
            victoryFairyRankingWithinTeam = victoryFairy.rankWithinTeam,
            checkInCounts = checkIn.counts,
            checkInWinRate = checkIn.winRate,
            winCounts = checkIn.winCounts,
            drawCounts = checkIn.drawCounts,
            loseCounts = checkIn.loseCounts,
            recentCheckInDate = checkIn.recentCheckInDate,
        )
}
