package com.yagubogu.ui.livetalk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.yagubogu.data.dto.response.stadium.StadiumWeatherResponse
import com.yagubogu.data.repository.game.GameRepository
import com.yagubogu.data.repository.stadium.StadiumRepository
import com.yagubogu.ui.livetalk.model.LivetalkStadiumUiModel
import com.yagubogu.ui.livetalk.model.LivetalkUiState
import com.yagubogu.ui.livetalk.model.WeatherUiModel
import com.yagubogu.ui.mapper.toLivetalkUiModel
import com.yagubogu.ui.mapper.toUiModel
import com.yagubogu.ui.util.mapList
import com.yagubogu.ui.util.now
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlin.time.Clock

class LivetalkViewModel(
    private val gameRepository: GameRepository,
    private val stadiumRepository: StadiumRepository,
    private val clock: Clock,
) : ViewModel() {
    private val logger = Logger.withTag("LivetalkViewModel")

    private val _uiState = MutableStateFlow(LivetalkUiState(isLoading = true))
    val uiState: StateFlow<LivetalkUiState> = _uiState.asStateFlow()

    fun fetchGames(date: LocalDate = LocalDate.now(clock)) {
        viewModelScope.launch {
            gameRepository
                .getGames(date)
                .mapList { it.toLivetalkUiModel() }
                .onSuccess { stadiums: List<LivetalkStadiumUiModel> ->
                    val previousWeather: Map<Long, WeatherUiModel?> =
                        uiState.value.stadiums.associate { it.stadiumId to it.weatherUiModel }
                    val stadiumsWithPreviousWeather: List<LivetalkStadiumUiModel> =
                        stadiums.map { stadium: LivetalkStadiumUiModel ->
                            stadium.copy(weatherUiModel = previousWeather[stadium.stadiumId])
                        }
                    _uiState.update { current: LivetalkUiState ->
                        current.copy(
                            isLoading = false,
                            stadiums =
                                stadiumsWithPreviousWeather
                                    .sortedByVerification()
                                    .toImmutableList(),
                        )
                    }

                    fetchWeather()
                }.onFailure { exception: Throwable ->
                    logger.w(exception) { "API 호출 실패" }
                    _uiState.update { current: LivetalkUiState ->
                        current.copy(isLoading = false)
                    }
                }
        }
    }

    private suspend fun fetchWeather() {
        val ids: List<Long> = uiState.value.stadiums.map { it.stadiumId }
        if (ids.isEmpty()) return

        stadiumRepository
            .getStadiumWeather(ids)
            .onSuccess { stadiumWeatherResponse: StadiumWeatherResponse ->
                val weatherUiModels: Map<Long, WeatherUiModel> = stadiumWeatherResponse.toUiModel()
                if (weatherUiModels.isEmpty()) return@onSuccess

                _uiState.update { current: LivetalkUiState ->
                    current.copy(
                        stadiums =
                            current.stadiums
                                .map { stadium: LivetalkStadiumUiModel ->
                                    stadium.copy(weatherUiModel = weatherUiModels[stadium.stadiumId])
                                }.toImmutableList(),
                        isWeatherLoaded = true,
                    )
                }
            }.onFailure {
                logger.w(it) { "날씨 API 호출 실패" }
            }
    }

    private fun List<LivetalkStadiumUiModel>.sortedByVerification(): List<LivetalkStadiumUiModel> {
        val (verifiedItems, unverifiedItems) =
            partition { stadium: LivetalkStadiumUiModel ->
                stadium.isVerified
            }
        return verifiedItems + unverifiedItems
    }
}
