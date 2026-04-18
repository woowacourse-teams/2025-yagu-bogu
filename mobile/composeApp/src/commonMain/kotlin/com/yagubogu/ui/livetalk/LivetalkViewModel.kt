package com.yagubogu.ui.livetalk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.yagubogu.data.repository.game.GameRepository
import com.yagubogu.domain.util.now
import com.yagubogu.ui.livetalk.model.LivetalkStadiumItem
import com.yagubogu.ui.mapper.toLivetalkUiModel
import com.yagubogu.ui.util.mapList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlin.time.Clock

class LivetalkViewModel(
    private val gameRepository: GameRepository,
    private val clock: Clock,
) : ViewModel() {
    private val logger = Logger.withTag("LivetalkViewModel")

    private val _stadiumItems = MutableStateFlow<List<LivetalkStadiumItem>?>(null)
    val stadiumItems: StateFlow<List<LivetalkStadiumItem>?> = _stadiumItems.asStateFlow()

    fun fetchGames(date: LocalDate = LocalDate.now(clock)) {
        viewModelScope.launch {
            val gamesResult: Result<List<LivetalkStadiumItem>> =
                gameRepository.getGames(date).mapList { it.toLivetalkUiModel() }
            gamesResult
                .onSuccess { livetalkStadiumItems: List<LivetalkStadiumItem> ->
                    _stadiumItems.value = sortStadiumsByVerification(livetalkStadiumItems)
                }.onFailure { exception: Throwable ->
                    logger.w(exception) { "API 호출 실패" }
                }
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
