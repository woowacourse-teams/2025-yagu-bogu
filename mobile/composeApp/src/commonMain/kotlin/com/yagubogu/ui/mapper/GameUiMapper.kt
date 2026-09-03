package com.yagubogu.ui.mapper

import com.yagubogu.data.dto.response.game.GameWithCheckInDto
import com.yagubogu.data.dto.response.game.LiveGamesResponse
import com.yagubogu.data.dto.response.game.TeamByGameDto
import com.yagubogu.domain.model.Team
import com.yagubogu.ui.attendance.model.GameState
import com.yagubogu.ui.attendance.model.PastGameUiModel
import com.yagubogu.ui.livetalk.model.BallCountUiModel
import com.yagubogu.ui.livetalk.model.BasesUiModel
import com.yagubogu.ui.livetalk.model.LiveGameStateUiModel
import com.yagubogu.ui.livetalk.model.LivetalkStadiumUiModel
import com.yagubogu.ui.livetalk.model.LivetalkTeamUiModel
import com.yagubogu.ui.livetalk.model.ScoreUiModel
import com.yagubogu.ui.livetalk.model.WeatherUiModel
import kotlinx.datetime.LocalDate

fun GameWithCheckInDto.toAttendanceUiModel(date: LocalDate): PastGameUiModel =
    PastGameUiModel(
        gameId = gameId,
        date = date,
        startAt = startAt,
        stadiumName = stadium.name,
        awayTeam = awayTeam.toDomain(),
        awayTeamName = awayTeam.name,
        homeTeam = homeTeam.toDomain(),
        homeTeamName = homeTeam.name,
    )

fun TeamByGameDto.toDomain(): Team = Team.getByCode(code)

object GameUiMapper {
    fun mapToLivetalkUiModels(
        games: List<GameWithCheckInDto>,
        liveGames: List<LiveGamesResponse.LiveGameDto>,
        weathers: Map<Long, WeatherUiModel>,
    ): List<LivetalkStadiumUiModel> {
        val liveGameByGameId: Map<Long, LiveGamesResponse.LiveGameDto> =
            liveGames.associateBy { it.gameId }

        return games.mapNotNull { game: GameWithCheckInDto ->
            val liveGame: LiveGamesResponse.LiveGameDto =
                liveGameByGameId[game.gameId] ?: return@mapNotNull null
            mapToLivetalkUiModel(
                game = game,
                liveGame = liveGame,
                weather = weathers[game.stadium.id],
            )
        }
    }

    private fun mapToLivetalkUiModel(
        game: GameWithCheckInDto,
        liveGame: LiveGamesResponse.LiveGameDto,
        weather: WeatherUiModel?,
    ): LivetalkStadiumUiModel =
        LivetalkStadiumUiModel(
            gameId = game.gameId,
            stadiumId = game.stadium.id,
            stadiumName = game.stadium.name,
            userCount = game.totalCheckIns,
            isVerified = game.isMyCheckIn,
            liveGameState = liveGame.toUiModel(),
            weatherUiModel = weather,
        )

    private fun LiveGamesResponse.LiveGameDto.toUiModel(): LiveGameStateUiModel {
        val gameState = GameState.from(gameState)

        return when (gameState) {
            GameState.SCHEDULED -> this.toScheduledUiModel()
            GameState.LIVE -> this.toLiveUiModel()
            GameState.COMPLETED -> this.toCompletedUiModel()
            GameState.CANCELED -> this.toCanceledUiModel()
            GameState.UNKNOWN -> this.toScheduledUiModel()
        }
    }

    private fun LiveGamesResponse.LiveGameDto.toScheduledUiModel(): LiveGameStateUiModel =
        LiveGameStateUiModel.Scheduled(
            awayTeam = awayTeam.toUiModel(),
            homeTeam = homeTeam.toUiModel(),
            startAt = startAt,
        )

    private fun LiveGamesResponse.LiveGameDto.toLiveUiModel(): LiveGameStateUiModel =
        when (liveState == null) {
            true -> {
                this.toScheduledUiModel()
            }

            false -> {
                LiveGameStateUiModel.Live(
                    awayTeam = awayTeam.toUiModel(),
                    homeTeam = homeTeam.toUiModel(),
                    score =
                        ScoreUiModel(
                            awayScore = awayTeam.score,
                            homeScore = homeTeam.score,
                        ),
                    inning = liveState.inning,
                    inningHalf = liveState.inningHalf,
                    bases = liveState.bases.toUiModel(),
                    ballCount = liveState.count.toUiModel(),
                )
            }
        }

    private fun LiveGamesResponse.LiveGameDto.toCompletedUiModel(): LiveGameStateUiModel =
        LiveGameStateUiModel.Completed(
            awayTeam = awayTeam.toUiModel(),
            homeTeam = homeTeam.toUiModel(),
            score =
                ScoreUiModel(
                    awayScore = awayTeam.score,
                    homeScore = homeTeam.score,
                ),
        )

    private fun LiveGamesResponse.LiveGameDto.toCanceledUiModel(): LiveGameStateUiModel =
        LiveGameStateUiModel.Canceled(
            awayTeam = awayTeam.toUiModel(),
            homeTeam = homeTeam.toUiModel(),
        )

    private fun LiveGamesResponse.LiveTeamDto.toUiModel(): LivetalkTeamUiModel =
        LivetalkTeamUiModel(
            team = Team.getByCode(code),
            currentPlayerName = currentPlayer,
            currentPlayerRole = currentPlayerRole,
        )

    private fun LiveGamesResponse.BasesDto.toUiModel(): BasesUiModel =
        BasesUiModel(
            isFirstBaseOccupied = firstBaseOccupied,
            isSecondBaseOccupied = secondBaseOccupied,
            isThirdBaseOccupied = thirdBaseOccupied,
        )

    private fun LiveGamesResponse.BallCountDto.toUiModel(): BallCountUiModel =
        BallCountUiModel(
            ballCount = balls,
            strikeCount = strikes,
            outCount = outs,
        )
}
