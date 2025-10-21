package com.yagubogu.ui.pastcheckin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.R
import com.yagubogu.domain.repository.GameRepository
import com.yagubogu.presentation.livetalk.stadium.LivetalkStadiumItem
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber
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
                    R.string.past_check_in_not_selected_game,
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
                        R.string.past_check_in_existing_game,
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
            _uiState.value =
                _uiState.value.copy(loadingMessageRes = R.string.past_check_in_loading_games)

            runCatching {
                gameRepository.getGames(date)
            }.onSuccess { games ->
                _uiState.value =
                    _uiState.value.copy(
                        gameList = games.getOrDefault(emptyList()),
                        loadingMessageRes = null,
                    )
            }.onFailure { exception ->
                _uiState.value =
                    _uiState.value.copy(
                        gameList = emptyList(),
                        loadingMessageRes = null,
                    )
                _uiEvent.trySend(
                    PastCheckInUiEvent.ShowToast(
                        R.string.past_check_in_failed_load_game_list,
                    ),
                )
                Timber.w("과거 직관 등록 중 게임 목록 fetch 실패: $exception")
            }
        }
    }

    private fun registerPastGame(
        game: LivetalkStadiumItem,
        date: LocalDate,
    ) {
        viewModelScope.launch {
            _uiState.value =
                _uiState.value.copy(loadingMessageRes = R.string.past_check_in_adding_message)

            runCatching {
                // TODO: 실제 과거 직관 등록 API 호출
                kotlinx.coroutines.delay(1000)
                // TODO: 테스트용 딜레이. 실제 API 호출 시 삭제할 것.
            }.onSuccess {
                _uiState.value = _uiState.value.copy(loadingMessageRes = null)
                fetchGameList(uiState.value.selectedDate ?: LocalDate.now())
                _uiEvent.trySend(
                    PastCheckInUiEvent.ShowToastWithArgs(
                        R.string.past_check_in_saved_alert,
                        listOf(
                            game.homeTeam.shortname,
                            game.awayTeam.shortname,
                        ),
                    ),
                )
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(loadingMessageRes = null)
                _uiEvent.trySend(
                    PastCheckInUiEvent.ShowToast(
                        R.string.past_check_in_failed_adding_game,
                    ),
                )
                Timber.w("과거 직관 등록 실패: $exception")
            }
        }
    }
}
