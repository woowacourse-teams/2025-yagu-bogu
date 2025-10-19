package com.yagubogu.ui.pastcheckin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.domain.repository.GameRepository
import com.yagubogu.presentation.livetalk.stadium.LivetalkStadiumItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class PastCheckInViewModel(
    private val gameRepository: GameRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PastCheckInUiState())
    val uiState: StateFlow<PastCheckInUiState> = _uiState.asStateFlow()

    fun onDateSelected(date: LocalDate?) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
        date?.let { fetchGameList(it) }
    }

    fun onGameSelected(game: LivetalkStadiumItem) {
        _uiState.value = _uiState.value.copy(selectedGame = game)
    }

    private fun fetchGameList(date: LocalDate) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            runCatching {
                gameRepository.getGames(date)
            }.onSuccess { games ->
                _uiState.value =
                    _uiState.value.copy(
                        gameList = games.getOrDefault(emptyList()),
                        isLoading = false,
                        errorMessage = null,
                    )
            }.onFailure { exception ->
                _uiState.value =
                    _uiState.value.copy(
                        gameList = emptyList(),
                        isLoading = false,
                        errorMessage = exception.message ?: "알 수 없는 오류가 발생했습니다",
                    )
            }
        }
    }
}
