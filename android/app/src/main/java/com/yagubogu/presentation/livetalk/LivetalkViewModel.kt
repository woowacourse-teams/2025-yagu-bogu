package com.yagubogu.presentation.livetalk

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.domain.repository.GamesRepository
import com.yagubogu.presentation.livetalk.stadium.LivetalkStadiumItem
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate

class LivetalkViewModel(
    private val gamesRepository: GamesRepository,
) : ViewModel() {
    private val _livetalkStadiumItems = MutableLiveData<List<LivetalkStadiumItem>>()
    val livetalkStadiumItems: LiveData<List<LivetalkStadiumItem>> get() = _livetalkStadiumItems

    init {
        fetchGames()
    }

    fun fetchGames() {
        viewModelScope.launch {
            val gamesResult: Result<List<LivetalkStadiumItem>> =
                gamesRepository.getGames(TOKEN, DATE)
            gamesResult
                .onSuccess { livetalkStadiumItems: List<LivetalkStadiumItem> ->
                    _livetalkStadiumItems.value = livetalkStadiumItems
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "API 호출 실패")
                }
        }
    }

    companion object {
        private val DATE = LocalDate.of(2025, 7, 25) // TODO: LocalDate.now()로 변경
        private const val TOKEN = "액세스 토큰"
    }
}
