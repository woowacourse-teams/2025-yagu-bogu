package com.yagubogu.ui.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.yagubogu.data.dto.response.game.GameWithCheckInDto
import com.yagubogu.data.repository.appconfig.AppConfigRepository
import com.yagubogu.data.repository.checkin.CheckInRepository
import com.yagubogu.data.repository.game.GameRepository
import com.yagubogu.ui.attendance.model.AttendanceFilterState
import com.yagubogu.ui.attendance.model.AttendanceHistoryItem
import com.yagubogu.ui.attendance.model.AttendanceHistorySort
import com.yagubogu.ui.attendance.model.PastGameUiModel
import com.yagubogu.ui.attendance.model.PastGameUiState
import com.yagubogu.ui.mapper.toAttendanceUiModel
import com.yagubogu.ui.mapper.toUiModel
import com.yagubogu.ui.util.mapList
import com.yagubogu.ui.util.now
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.number

class AttendanceHistoryViewModel(
    private val checkInRepository: CheckInRepository,
    private val gameRepository: GameRepository,
    appConfigRepository: AppConfigRepository,
) : ViewModel() {
    private val logger = Logger.withTag("AttendanceHistoryViewModel")

    private val _items = MutableStateFlow<List<AttendanceHistoryItem>>(emptyList())
    val items: StateFlow<List<AttendanceHistoryItem>> = _items.asStateFlow()

    private val _gameDates = MutableStateFlow<Set<LocalDate>>(emptySet())
    val gameDates: StateFlow<Set<LocalDate>> = _gameDates.asStateFlow()

    private val _selectedMonth = MutableStateFlow<YearMonth>(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth.asStateFlow()

    private val _selectedDate = MutableStateFlow<LocalDate>(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _filterState =
        MutableStateFlow(AttendanceFilterState(yearMonth = selectedMonth.value))
    val filterState: StateFlow<AttendanceFilterState> = _filterState.asStateFlow()

    private val _sort = MutableStateFlow(AttendanceHistorySort.LATEST)
    val sort: StateFlow<AttendanceHistorySort> = _sort.asStateFlow()

    private val _pastGameUiState = MutableStateFlow<PastGameUiState>(PastGameUiState.Loading)
    val pastGameUiState: StateFlow<PastGameUiState> = _pastGameUiState.asStateFlow()

    private val _pastCheckInUiEvent =
        MutableSharedFlow<Unit>(
            replay = 0,
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )
    val pastCheckInUiEvent: SharedFlow<Unit> = _pastCheckInUiEvent.asSharedFlow()

    private val _showInterstitialAdEvent =
        MutableSharedFlow<Unit>(
            replay = 0,
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )
    val showInterstitialAdEvent: SharedFlow<Unit> = _showInterstitialAdEvent.asSharedFlow()

    private var isPastCheckInAdEnabled = true
    private var pastCheckInCount = 0

    init {
        isPastCheckInAdEnabled = appConfigRepository.isPastCheckInAdEnabled()
    }

    fun fetchAttendanceHistoryItems(
        yearMonth: YearMonth,
        isYearly: Boolean = false,
        isWinOnly: Boolean = false,
        sort: AttendanceHistorySort = AttendanceHistorySort.LATEST,
    ) {
        viewModelScope.launch {
            checkInRepository
                .getCheckInHistories(
                    year = yearMonth.year,
                    month = if (isYearly) null else yearMonth.month.number,
                    sort = sort.name,
                    isWinOnly = isWinOnly,
                ).mapList { it.toUiModel() }
                .onSuccess { attendanceItems: List<AttendanceHistoryItem> ->
                    _items.value = attendanceItems
                }.onFailure { exception: Throwable ->
                    logger.w(exception) { "API 호출 실패" }
                }
        }
    }

    fun fetchGameDates() {
        viewModelScope.launch {
            val yearMonth: YearMonth = selectedMonth.value
            gameRepository
                .getGameDates(yearMonth)
                .onSuccess { dates: List<LocalDate> ->
                    _gameDates.value = dates.toSet()
                }.onFailure { exception: Throwable ->
                    logger.w(exception) { "API 호출 실패" }
                }
        }
    }

    fun fetchPastGames(date: LocalDate) {
        viewModelScope.launch {
            _pastGameUiState.value = PastGameUiState.Loading
            val gamesResult: Result<List<PastGameUiModel>> =
                gameRepository
                    .getGames(date)
                    .map { list: List<GameWithCheckInDto> ->
                        list.filter { !it.isMyCheckIn }
                    }.mapList { it.toAttendanceUiModel(date) }
            gamesResult
                .onSuccess { pastGameUiModels: List<PastGameUiModel> ->
                    _pastGameUiState.value = PastGameUiState.Success(pastGameUiModels)
                }.onFailure { exception: Throwable ->
                    logger.w(exception) { "API 호출 실패" }
                }
        }
    }

    fun addPastCheckIn(gameId: Long) {
        viewModelScope.launch {
            checkInRepository
                .addPastCheckIn(gameId)
                .onSuccess {
                    emitPastCheckInEvent()
                    fetchAttendanceHistoryItems(yearMonth = selectedMonth.value)
                }.onFailure { exception: Throwable ->
                    logger.w(exception) { "API 호출 실패" }
                }
        }
    }

    private suspend fun emitPastCheckInEvent() {
        pastCheckInCount++
        if (isPastCheckInAdEnabled && pastCheckInCount % 3 == 1) {
            _showInterstitialAdEvent.emit(Unit)
        } else {
            _pastCheckInUiEvent.emit(Unit)
        }
    }

    fun updateSelectedMonth(yearMonth: YearMonth) {
        _selectedMonth.value = yearMonth
        _filterState.update { it.copy(yearMonth = yearMonth) }
    }

    fun updateSelectedDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun toggleWinOnlyFilter() {
        _filterState.update { it.copy(isWinOnly = !it.isWinOnly) }
    }

    fun toggleYearlyFilter() {
        _filterState.update { it.copy(isYearly = !it.isYearly) }
    }

    fun updateSort(sort: AttendanceHistorySort) {
        _sort.value = sort
    }

    companion object {
        val START_MONTH: YearMonth = YearMonth(2021, 3)
        val END_MONTH: YearMonth = YearMonth.now()
    }
}
