package com.yagubogu.data.dto.response.stats

import com.yagubogu.domain.model.Team
import com.yagubogu.ui.stats.detail.model.VsTeamStatItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpponentWinRateTeamDto(
    @SerialName("teamId")
    val teamId: Long, // 팀 id
    @SerialName("name")
    val name: String, // 팀 이름
    @SerialName("shortName")
    val shortName: String, // 축약된 팀 이름
    @SerialName("teamCode")
    val teamCode: String, // 팀 코드
    @SerialName("wins")
    val wins: Int, // 승리 횟수
    @SerialName("losses")
    val losses: Int, // 패배 횟수
    @SerialName("draws")
    val draws: Int, // 무승부 횟수
    @SerialName("winRate")
    val winRate: Double, // 승률(%)
) {
    fun toPresentation(rank: Int): VsTeamStatItem =
        VsTeamStatItem(
            rank = rank,
            team = Team.getByCode(teamCode),
            teamName = shortName,
            winCounts = wins,
            drawCounts = draws,
            loseCounts = losses,
            winningPercentage = winRate,
        )
}
