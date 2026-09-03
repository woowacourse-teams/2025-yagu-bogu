package com.yagubogu.ui.livetalk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.yagubogu.data.dto.response.game.GameWithCheckInDto
import com.yagubogu.data.dto.response.game.LiveGamesResponse.LiveGameDto
import com.yagubogu.data.dto.response.stadium.StadiumWeatherResponse
import com.yagubogu.data.repository.game.GameRepository
import com.yagubogu.data.repository.stadium.StadiumRepository
import com.yagubogu.ui.attendance.model.GameState
import com.yagubogu.ui.livetalk.model.LivetalkStadiumUiModel
import com.yagubogu.ui.livetalk.model.LivetalkUiState
import com.yagubogu.ui.livetalk.model.WeatherUiModel
import com.yagubogu.ui.mapper.GameUiMapper
import com.yagubogu.ui.mapper.toUiModel
import com.yagubogu.ui.util.now
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.time.Clock

class LivetalkViewModel(
    private val gameRepository: GameRepository,
    private val stadiumRepository: StadiumRepository,
    private val clock: Clock,
) : ViewModel() {
    private val logger = Logger.withTag("LivetalkViewModel")

    private val games = MutableStateFlow<List<GameWithCheckInDto>?>(null)

    private val selectedDate = MutableStateFlow(LocalDate.now(clock))

    @OptIn(ExperimentalCoroutinesApi::class)
    private val liveGames: StateFlow<List<LiveGameDto>?> =
        selectedDate
            .flatMapLatest { date: LocalDate -> pollLiveGames(date) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MILLIS),
                initialValue = null,
            )

    private val weathers = MutableStateFlow<Map<Long, WeatherUiModel>>(emptyMap())

    val uiState: StateFlow<LivetalkUiState> =
        combine(games, liveGames, weathers) {
            games: List<GameWithCheckInDto>?,
            liveGames: List<LiveGameDto>?,
            weathers: Map<Long, WeatherUiModel>,
            ->
            if (games == null || liveGames == null) {
                LivetalkUiState(isLoading = true)
            } else {
                LivetalkUiState(
                    isLoading = false,
                    stadiums =
                        GameUiMapper
                            .mapToLivetalkUiModels(
                                games = games,
                                liveGames = liveGames,
                                weathers = weathers,
                            ).sortedByVerification()
                            .toImmutableList(),
                    isWeatherLoaded = weathers.isNotEmpty(),
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MILLIS),
            initialValue = LivetalkUiState(isLoading = true),
        )

    fun fetchGames(date: LocalDate = LocalDate.now(clock)) {
        selectedDate.value = date

        viewModelScope.launch {
            gameRepository
                .getGames(date)
                .onSuccess { result: List<GameWithCheckInDto> ->
                    games.value = result
                    fetchWeathers(result.map { it.stadium.id })
                }.onFailure { exception: Throwable ->
                    logger.w(exception) { "경기 목록 API 호출 실패" }
                }
        }
    }

    private fun fetchWeathers(stadiumIds: List<Long>) {
        if (stadiumIds.isEmpty()) return

        viewModelScope.launch {
            stadiumRepository
                .getStadiumWeather(stadiumIds)
                .onSuccess { response: StadiumWeatherResponse -> weathers.value = response.toUiModel() }
                .onFailure { exception: Throwable ->
                    logger.w(exception) { "날씨 API 호출 실패" }
                }
        }
    }

    private fun pollLiveGames(date: LocalDate): Flow<List<LiveGameDto>> =
        flow {
            while (true) {
                val result: List<LiveGameDto>? =
                    gameRepository
                        .getLiveGames(date)
                        .onSuccess { liveGames: List<LiveGameDto> -> emit(liveGames) }
                        .onFailure { exception: Throwable ->
                            logger.w(exception) { "실시간 경기 API 호출 실패" }
                        }.getOrNull()

                if (result == null) {
                    delay(ACTIVE_INTERVAL_MILLIS)
                    continue
                }

                val nextDelay: Long = nextPollingDelayMillis(result) ?: return@flow
                delay(nextDelay)
            }
        }

    private fun nextPollingDelayMillis(liveGames: List<LiveGameDto>): Long? {
        val gameStates: List<GameState> = liveGames.map { GameState.from(it.gameState) }
        if (gameStates.any { it == GameState.LIVE || it == GameState.UNKNOWN }) {
            return ACTIVE_INTERVAL_MILLIS
        }

        val earliestStartAt: LocalTime =
            liveGames
                .filter { GameState.from(it.gameState) == GameState.SCHEDULED }
                .minOfOrNull { it.startAt }
                ?: return null

        val secondsUntilStart: Int = earliestStartAt.toSecondOfDay() - LocalTime.now(clock).toSecondOfDay()
        return (secondsUntilStart * MILLIS_PER_SECOND).coerceAtLeast(ACTIVE_INTERVAL_MILLIS)
    }

    private fun List<LivetalkStadiumUiModel>.sortedByVerification(): List<LivetalkStadiumUiModel> {
        val (verifiedItems, unverifiedItems) =
            partition { stadium: LivetalkStadiumUiModel -> stadium.isVerified }
        return verifiedItems + unverifiedItems
    }

    companion object {
        private const val ACTIVE_INTERVAL_MILLIS = 15_000L

        private const val MILLIS_PER_SECOND = 1_000L
        private const val SUBSCRIPTION_TIMEOUT_MILLIS = 5_000L
    }
}
