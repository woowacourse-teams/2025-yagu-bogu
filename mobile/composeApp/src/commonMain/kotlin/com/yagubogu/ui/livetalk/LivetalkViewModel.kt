package com.yagubogu.ui.livetalk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.yagubogu.data.dto.response.stadium.StadiumWeatherResponse
import com.yagubogu.data.repository.game.GameRepository
import com.yagubogu.data.repository.stadium.StadiumRepository
import com.yagubogu.ui.livetalk.model.LivetalkStadiumItem
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
                .onSuccess { livetalkStadiumItems: List<LivetalkStadiumItem> ->
                    val previousWeather: Map<Long, WeatherUiModel?> =
                        uiState.value.stadiumItems.associate { it.stadiumId to it.weatherUiModel }
                    val itemsWithPreviousWeather =
                        livetalkStadiumItems.map { item ->
                            item.copy(weatherUiModel = previousWeather[item.stadiumId])
                        }
                    _uiState.update { current: LivetalkUiState ->
                        current.copy(
                            isLoading = false,
                            stadiumItems =
                                itemsWithPreviousWeather
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
        val ids: List<Long> = uiState.value.stadiumItems.map { it.stadiumId }
        if (ids.isEmpty()) return

        stadiumRepository
            .getStadiumWeather(ids)
            .onSuccess { stadiumWeatherResponse: StadiumWeatherResponse ->
                val weatherUiModels: Map<Long, WeatherUiModel> = stadiumWeatherResponse.toUiModel()
                if (weatherUiModels.isEmpty()) return@onSuccess

                _uiState.update { current: LivetalkUiState ->
                    current.copy(
                        stadiumItems =
                            current.stadiumItems
                                .map { livetalkStadiumItem: LivetalkStadiumItem ->
                                    livetalkStadiumItem.copy(weatherUiModel = weatherUiModels[livetalkStadiumItem.stadiumId])
                                }.toImmutableList(),
                        isWeatherLoaded = true,
                    )
                }
            }.onFailure {
                logger.w(it) { "날씨 API 호출 실패" }
            }
    }

    private fun List<LivetalkStadiumItem>.sortedByVerification(): List<LivetalkStadiumItem> {
        val (verifiedItems, unverifiedItems) =
            partition { liveTalkStadiumItem: LivetalkStadiumItem ->
                liveTalkStadiumItem.isVerified
            }
        return verifiedItems + unverifiedItems
    }
}
