package com.yagubogu.data.dto.response.game

import com.yagubogu.domain.model.InningHalf
import com.yagubogu.domain.model.PlayerRole
import kotlinx.datetime.LocalTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LiveGamesResponse(
    @SerialName("games")
    val games: List<LiveGameDto>,
) {
    @Serializable
    data class LiveGameDto(
        @SerialName("gameId")
        val gameId: Long, // 경기 id
        @SerialName("homeTeam")
        val homeTeam: LiveTeamDto,
        @SerialName("awayTeam")
        val awayTeam: LiveTeamDto,
        @SerialName("gameState")
        val gameState: String, // 경기 상태 ("SCHEDULED", "LIVE", "COMPLETED", "CANCELED")
        @SerialName("startAt")
        val startAt: LocalTime, // 경기 시작 시간
        @SerialName("liveState")
        val liveState: LiveStateDto?,
    )

    @Serializable
    data class LiveTeamDto(
        @SerialName("code")
        val code: String, // 팀 코드
        @SerialName("currentPlayer")
        val currentPlayer: String?, // 현재 선수 (타자 또는 투수)
        @SerialName("currentPlayerRole")
        val currentPlayerRole: PlayerRole?, // "BATTER" 또는 "PITCHER"
        @SerialName("score")
        val score: Int?, // 팀 점수
    )

    @Serializable
    data class LiveStateDto(
        @SerialName("inning")
        val inning: Int, // 현재 이닝
        @SerialName("inningHalf")
        val inningHalf: InningHalf, // "TOP"(초) 또는 "BOTTOM"(말)
        @SerialName("bases")
        val bases: BasesDto,
        @SerialName("count")
        val count: BallCountDto,
    )

    @Serializable
    data class BasesDto(
        @SerialName("firstBaseOccupied")
        val firstBaseOccupied: Boolean, // 1루 주자 여부
        @SerialName("secondBaseOccupied")
        val secondBaseOccupied: Boolean, // 2루 주자 여부
        @SerialName("thirdBaseOccupied")
        val thirdBaseOccupied: Boolean, // 3루 주자 여부
    )

    @Serializable
    data class BallCountDto(
        @SerialName("balls")
        val balls: Int, // 볼 카운트
        @SerialName("strikes")
        val strikes: Int, // 스트라이크 카운트
        @SerialName("outs")
        val outs: Int, // 아웃 카운트
    )
}
