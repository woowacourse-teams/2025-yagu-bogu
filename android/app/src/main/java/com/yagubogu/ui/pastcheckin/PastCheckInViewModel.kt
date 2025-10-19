package com.yagubogu.ui.pastcheckin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.domain.repository.GameRepository
import com.yagubogu.presentation.livetalk.stadium.LivetalkStadiumItem
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class PastCheckInViewModel(
    private val gameRepository: GameRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PastCheckInUiState())
    val uiState: StateFlow<PastCheckInUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<PastCheckInUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun confirmRegistration() {
        val selectedGame = _uiState.value.selectedGame
        val selectedDate = _uiState.value.selectedDate

        dismissDialog()

        if (selectedGame != null && selectedDate != null) {
            registerPastGame(selectedGame, selectedDate)
        } else {
            _uiEvent.trySend(
                PastCheckInUiEvent.ShowToast(
                    "선택된 게임 정보가 없어\n직관 정보를 등록할 수 없습니다.",
                ),
            )
        }
    }

    fun dismissDialog() {
        _uiState.value =
            _uiState.value.copy(
                showConfirmDialog = false,
            )
    }

    fun onDateSelected(date: LocalDate?) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
        date?.let { fetchGameList(it) }
    }

    fun onGameSelected(game: LivetalkStadiumItem) {
        when (game.isVerified) {
            true -> {
                _uiEvent.trySend(
                    PastCheckInUiEvent.ShowToast(
                        "이미 직관 등록된 경기입니다!",
                    ),
                )
            }

            false ->
                _uiState.value =
                    _uiState.value.copy(selectedGame = game, showConfirmDialog = true)
        }
    }

    private fun fetchGameList(date: LocalDate) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = "경기 목록을 불러오는 중...")

            runCatching {
                gameRepository.getGames(date)
            }.onSuccess { games ->
                _uiState.value =
                    _uiState.value.copy(
                        gameList = games.getOrDefault(emptyList()),
                        isLoading = null,
                        errorMessage = null,
                    )
            }.onFailure { exception ->
                _uiState.value =
                    _uiState.value.copy(
                        gameList = emptyList(),
                        isLoading = null,
                        errorMessage = exception.message ?: "알 수 없는 오류가 발생했습니다",
                    )
            }
        }
    }

    private fun registerPastGame(
        game: LivetalkStadiumItem,
        date: LocalDate,
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = "과거 직관을 등록하는 중...")

            runCatching {
                // TODO: 실제 과거 직관 등록 API 호출
                kotlinx.coroutines.delay(1000)
                // TODO: 테스트용 딜레이. 실제 API 호출 시 삭제할 것.
            }.onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = null)
                fetchGameList(uiState.value.selectedDate ?: LocalDate.now())
                _uiEvent.trySend(
                    PastCheckInUiEvent.ShowToast("${game.homeTeam.name} vs ${game.awayTeam.name} 직관 기록이 등록되었습니다!"),
                )
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(isLoading = null)
                _uiEvent.trySend(
                    PastCheckInUiEvent.ShowToast(
                        exception.message ?: "등록에 실패했습니다. 다시 시도해주세요.",
                    ),
                )
            }
        }
    }
}
