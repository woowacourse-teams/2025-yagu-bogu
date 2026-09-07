package com.yagubogu.ui.livetalk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.yagubogu.data.repository.game.GameRepository
import com.yagubogu.data.repository.stadium.StadiumRepository
import com.yagubogu.ui.livetalk.model.GameCheckInUiModel
import com.yagubogu.ui.livetalk.model.LiveGameStateUiModel
import com.yagubogu.ui.livetalk.model.LivetalkStadiumUiModel
import com.yagubogu.ui.livetalk.model.LivetalkUiState
import com.yagubogu.ui.livetalk.model.WeatherUiModel
import com.yagubogu.ui.mapper.GameUiMapper
import com.yagubogu.ui.mapper.GameUiMapper.toUiModel
import com.yagubogu.ui.mapper.toUiModel
import com.yagubogu.ui.util.mapList
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

    private val games = MutableStateFlow<List<GameCheckInUiModel>?>(null)

    private val selectedDate = MutableStateFlow(LocalDate.now(clock))

    @OptIn(ExperimentalCoroutinesApi::class)
    private val liveGames: StateFlow<List<LiveGameStateUiModel>?> =
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
            games: List<GameCheckInUiModel>?,
            liveGames: List<LiveGameStateUiModel>?,
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
                .mapList { it.toUiModel() }
                .onSuccess { result: List<GameCheckInUiModel> ->
                    games.value = result
                    fetchWeathers(result.map { it.stadiumId })
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
                .map { it.toUiModel() }
                .onSuccess { result: Map<Long, WeatherUiModel> -> weathers.value = result }
                .onFailure { exception: Throwable ->
                    logger.w(exception) { "날씨 API 호출 실패" }
                }
        }
    }

    private fun pollLiveGames(date: LocalDate): Flow<List<LiveGameStateUiModel>> =
        flow {
            while (true) {
                val result: List<LiveGameStateUiModel>? =
                    gameRepository
                        .getLiveGames(date)
                        .mapList { it.toUiModel() }
                        .onSuccess { liveGames: List<LiveGameStateUiModel> ->
                            emit(liveGames)
                        }.onFailure { exception: Throwable ->
                            logger.w(exception) { "실시간 경기 API 호출 실패" }
                        }.getOrNull()

                if (result == null) {
                    delay(POLLING_INTERVAL_MILLIS)
                    continue
                }

                val nextDelay: Long = nextPollingDelayMillis(result) ?: return@flow
                delay(nextDelay)
            }
        }

    private fun nextPollingDelayMillis(liveGames: List<LiveGameStateUiModel>): Long? {
        val hasOngoingGame: Boolean =
            liveGames.any { it is LiveGameStateUiModel.Live || it is LiveGameStateUiModel.Unknown }
        if (hasOngoingGame) return POLLING_INTERVAL_MILLIS

        val earliestStartAt: LocalTime =
            liveGames
                .filterIsInstance<LiveGameStateUiModel.Scheduled>()
                .minOfOrNull { it.startAt }
                ?: return null

        val secondsUntilStart: Int = earliestStartAt.toSecondOfDay() - LocalTime.now(clock).toSecondOfDay()
        return (secondsUntilStart * MILLIS_PER_SECOND).coerceAtLeast(POLLING_INTERVAL_MILLIS)
    }

    private fun List<LivetalkStadiumUiModel>.sortedByVerification(): List<LivetalkStadiumUiModel> {
        val (verifiedItems, unverifiedItems) =
            partition { stadium: LivetalkStadiumUiModel -> stadium.isVerified }
        return verifiedItems + unverifiedItems
    }

    companion object {
        private const val MILLIS_PER_SECOND = 1_000L
        private const val POLLING_INTERVAL_MILLIS = 15_000L
        private const val SUBSCRIPTION_TIMEOUT_MILLIS = 5_000L
    }
}
