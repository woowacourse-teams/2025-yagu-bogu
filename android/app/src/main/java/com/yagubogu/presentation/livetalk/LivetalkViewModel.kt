package com.yagubogu.presentation.livetalk

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.domain.repository.GameRepository
import com.yagubogu.presentation.livetalk.stadium.LivetalkStadiumItem
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate

class LivetalkViewModel(
    private val gameRepository: GameRepository,
) : ViewModel() {
    private val _livetalkStadiumItems = MutableLiveData<List<LivetalkStadiumItem>>()
    val livetalkStadiumItems: LiveData<List<LivetalkStadiumItem>> get() = _livetalkStadiumItems

    init {
        fetchGames()
    }

    fun fetchGames() {
        viewModelScope.launch {
            val gamesResult: Result<List<LivetalkStadiumItem>> =
                gameRepository.getGames(DATE)
            gamesResult
                .onSuccess { livetalkStadiumItems: List<LivetalkStadiumItem> ->
                    _livetalkStadiumItems.value = livetalkStadiumItems
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "API 호출 실패")
                }
        }
    }

    companion object {
        private val DATE = LocalDate.now()
    }
}
