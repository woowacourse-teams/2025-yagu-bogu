package com.yagubogu.data.dto.response.checkin

import com.yagubogu.presentation.home.ranking.VictoryFairyItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VictoryFairyRankingDto(
    @SerialName("ranking")
    val ranking: Int, // 승리요정 랭킹
    @SerialName("nickname")
    val nickname: String, // 회원 닉네임
    @SerialName("profileImageUrl")
    val profileImageUrl: String, // 회원 프로필 이미지 url
    @SerialName("teamShortName")
    val teamShortName: String, // 팀 이름
    @SerialName("winPercent")
    val winPercent: Double, // 승률(%)
    @SerialName("victoryFairyScore")
    val victoryFairyScore: Double, // 승리요정 점수
) {
    fun toPresentation(): VictoryFairyItem =
        VictoryFairyItem(
            rank = ranking,
            nickname = nickname,
            profileImageUrl = profileImageUrl,
            teamName = teamShortName,
            winRate = winPercent,
            score = victoryFairyScore,
        )
}
