package com.yagubogu.presentation.livetalk

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.domain.repository.GameRepository
import com.yagubogu.presentation.livetalk.stadium.LivetalkStadiumItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class LivetalkViewModel @Inject constructor(
    private val gameRepository: GameRepository,
) : ViewModel() {
    private val _livetalkStadiumItems = MutableLiveData<List<LivetalkStadiumItem>>()
    val livetalkStadiumItems: LiveData<List<LivetalkStadiumItem>> get() = _livetalkStadiumItems

    init {
        fetchGames()
    }

    fun fetchGames(date: LocalDate = LocalDate.of(2025, 10, 24)) {
        viewModelScope.launch {
            val gamesResult: Result<List<LivetalkStadiumItem>> = gameRepository.getGames(date)
            gamesResult
                .onSuccess { livetalkStadiumItems: List<LivetalkStadiumItem> ->
                    _livetalkStadiumItems.value = sortStadiumsByVerification(livetalkStadiumItems)
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
