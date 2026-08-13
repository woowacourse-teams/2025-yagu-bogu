package com.yagubogu.ui.livetalk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.yagubogu.data.dto.response.stadium.StadiumWeatherResponse
import com.yagubogu.data.repository.game.GameRepository
import com.yagubogu.data.repository.stadium.StadiumRepository
import com.yagubogu.ui.livetalk.model.LivetalkStadiumItem
import com.yagubogu.ui.livetalk.model.WeatherUiModel
import com.yagubogu.ui.mapper.toLivetalkUiModel
import com.yagubogu.ui.mapper.toUiModel
import com.yagubogu.ui.util.mapList
import com.yagubogu.ui.util.now
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlin.time.Clock

class LivetalkViewModel(
    private val gameRepository: GameRepository,
    private val stadiumRepository: StadiumRepository,
    private val clock: Clock,
) : ViewModel() {
    private val logger = Logger.withTag("LivetalkViewModel")

    private val _stadiumItems = MutableStateFlow<List<LivetalkStadiumItem>?>(null)
    val stadiumItems: StateFlow<List<LivetalkStadiumItem>?> = _stadiumItems.asStateFlow()

    private val _isWeatherLoaded = MutableStateFlow(false)
    val isWeatherLoaded: StateFlow<Boolean> = _isWeatherLoaded.asStateFlow()

    fun fetchGames(date: LocalDate = LocalDate.now(clock)) {
        viewModelScope.launch {
            val previousWeather: Map<Long, WeatherUiModel?> =
                _stadiumItems.value?.associate { it.stadiumId to it.weatherUiModel } ?: emptyMap()

            val gamesResult: Result<List<LivetalkStadiumItem>> =
                gameRepository.getGames(date).mapList { it.toLivetalkUiModel() }
            gamesResult
                .onSuccess { livetalkStadiumItems: List<LivetalkStadiumItem> ->
                    val itemsWithPreviousWeather =
                        livetalkStadiumItems.map { item ->
                            item.copy(weatherUiModel = previousWeather[item.stadiumId])
                        }
                    _stadiumItems.value = sortStadiumsByVerification(itemsWithPreviousWeather)

                    fetchWeather()
                }.onFailure { exception: Throwable ->
                    logger.w(exception) { "API 호출 실패" }
                }
        }
    }

    private suspend fun fetchWeather() {
        val ids = _stadiumItems.value?.map { it.stadiumId } ?: return
        stadiumRepository
            .getStadiumWeather(ids)
            .onSuccess { stadiumWeatherResponse: StadiumWeatherResponse ->
                val weatherUiModels: Map<Long, WeatherUiModel> = stadiumWeatherResponse.toUiModel()
                if (weatherUiModels.isNotEmpty()) {
                    _isWeatherLoaded.value = true
                    _stadiumItems.value =
                        _stadiumItems.value?.map { livetalkStadiumItem ->
                            livetalkStadiumItem.copy(weatherUiModel = weatherUiModels[livetalkStadiumItem.stadiumId])
                        }
                }
            }.onFailure {
                logger.w(it) { "날씨 API 호출 실패" }
            }
    }

    private fun sortStadiumsByVerification(livetalkStadiumItems: List<LivetalkStadiumItem>): List<LivetalkStadiumItem> {
        val (verifiedItems, unverifiedItems) =
            livetalkStadiumItems.partition { liveTalkStadiumItem: LivetalkStadiumItem ->
                liveTalkStadiumItem.isVerified
            }
        return verifiedItems + unverifiedItems
    }
}
