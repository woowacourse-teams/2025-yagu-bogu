package com.yagubogu.presentation.livetalk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.data.repository.game.GameRepository
import com.yagubogu.presentation.livetalk.stadium.LivetalkStadiumItem
import com.yagubogu.presentation.mapper.toUiModel
import com.yagubogu.presentation.util.mapList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class LivetalkViewModel @Inject constructor(
    private val gameRepository: GameRepository,
) : ViewModel() {
    private val _stadiumItems = MutableStateFlow<List<LivetalkStadiumItem>>(emptyList())
    val stadiumItems: StateFlow<List<LivetalkStadiumItem>> = _stadiumItems.asStateFlow()

    private val _scrollToTopEvent =
        MutableSharedFlow<Unit>(
            replay = 0,
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )
    val scrollToTopEvent: SharedFlow<Unit> = _scrollToTopEvent.asSharedFlow()

    init {
        fetchGames()
    }

    fun scrollToTop() {
        viewModelScope.launch {
            _scrollToTopEvent.emit(Unit)
        }
    }

    fun fetchGames(date: LocalDate = LocalDate.now()) {
        viewModelScope.launch {
            val gamesResult: Result<List<LivetalkStadiumItem>> =
                gameRepository.getGames(date).mapList { it.toUiModel() }
            gamesResult
                .onSuccess { livetalkStadiumItems: List<LivetalkStadiumItem> ->
                    _stadiumItems.value = sortStadiumsByVerification(livetalkStadiumItems)
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "API 호출 실패")
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
