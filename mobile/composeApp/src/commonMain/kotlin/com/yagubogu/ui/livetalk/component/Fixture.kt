package com.yagubogu.ui.livetalk.component

import com.yagubogu.domain.model.Team
import com.yagubogu.ui.livetalk.model.BallCountUiModel
import com.yagubogu.ui.livetalk.model.BasesUiModel
import com.yagubogu.ui.livetalk.model.Condition
import com.yagubogu.ui.livetalk.model.InningHalf
import com.yagubogu.ui.livetalk.model.LiveGameStateUiModel
import com.yagubogu.ui.livetalk.model.LivetalkStadiumUiModel
import com.yagubogu.ui.livetalk.model.PlayerRole
import com.yagubogu.ui.livetalk.model.PlayerUiModel
import com.yagubogu.ui.livetalk.model.ScoreUiModel
import com.yagubogu.ui.livetalk.model.WeatherUiModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalTime

private val PLAYER_BATTER =
    PlayerUiModel(
        name = "김타자",
        role = PlayerRole.BATTER,
    )

private val PLAYER_PITCHER =
    PlayerUiModel(
        name = "김투수투수",
        role = PlayerRole.PITCHER,
    )

val LIVE_GAME_STATE_SCHEDULED =
    LiveGameStateUiModel.Scheduled(
        awayTeam = Team.HT,
        homeTeam = Team.SS,
        awayPlayer = PLAYER_BATTER,
        homePlayer = PLAYER_PITCHER,
        startAt = LocalTime(18, 30),
    )

val LIVE_GAME_STATE_LIVE =
    LiveGameStateUiModel.Live(
        awayTeam = Team.HT,
        homeTeam = Team.SS,
        awayPlayer = PLAYER_BATTER,
        homePlayer = PLAYER_PITCHER,
        score = ScoreUiModel(awayScore = 3, homeScore = 5),
        inning = 7,
        inningHalf = InningHalf.TOP,
        bases =
            BasesUiModel(
                isFirstBaseOccupied = true,
                isSecondBaseOccupied = false,
                isThirdBaseOccupied = true,
            ),
        ballCount =
            BallCountUiModel(
                ballCount = 2,
                strikeCount = 1,
                outCount = 1,
            ),
    )

val LIVE_GAME_STATE_LIVE_FULL =
    LIVE_GAME_STATE_LIVE.copy(
        score = ScoreUiModel(awayScore = 12, homeScore = 0),
        inning = 9,
        inningHalf = InningHalf.TOP,
        bases =
            BasesUiModel(
                isFirstBaseOccupied = true,
                isSecondBaseOccupied = true,
                isThirdBaseOccupied = true,
            ),
        ballCount =
            BallCountUiModel(
                ballCount = 3,
                strikeCount = 2,
                outCount = 2,
            ),
    )

val LIVE_GAME_STATE_COMPLETED =
    LiveGameStateUiModel.Completed(
        awayTeam = Team.HT,
        homeTeam = Team.SS,
        score = ScoreUiModel(awayScore = 7, homeScore = 4),
    )

val LIVE_GAME_STATE_CANCELED =
    LiveGameStateUiModel.Canceled(
        awayTeam = Team.HT,
        homeTeam = Team.SS,
    )

val LIVE_GAME_STATE_UNKNOWN =
    LiveGameStateUiModel.Unknown(
        awayTeam = Team.HT,
        homeTeam = Team.SS,
        startAt = LocalTime(18, 30),
    )

val LIVETALK_STADIUM_VERIFIED =
    LivetalkStadiumUiModel(
        gameId = 0L,
        stadiumId = 9L,
        stadiumName = "대전 한화생명 볼파크",
        userCount = 100,
        isVerified = true,
        liveGameState = LIVE_GAME_STATE_LIVE,
        weatherUiModel = WeatherUiModel(9L, Condition.Clear, "12.3°C"),
    )

val LIVETALK_STADIUM_UNVERIFIED =
    LivetalkStadiumUiModel(
        gameId = 1L,
        stadiumId = 8L,
        stadiumName = "창원 NC 파크",
        userCount = 10,
        isVerified = false,
        liveGameState = LIVE_GAME_STATE_LIVE,
        weatherUiModel = WeatherUiModel(8L, Condition.Clear, "12.3°C"),
    )

val LIVETALK_STADIUMS =
    persistentListOf(
        LIVETALK_STADIUM_VERIFIED,
        LIVETALK_STADIUM_UNVERIFIED,
        LIVETALK_STADIUM_UNVERIFIED.copy(gameId = 2L),
        LIVETALK_STADIUM_UNVERIFIED.copy(gameId = 3L),
    )
