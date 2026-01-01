package com.yagubogu.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.data.dto.response.location.CoordinateDto
import com.yagubogu.data.repository.checkin.CheckInRepository
import com.yagubogu.data.repository.location.LocationRepository
import com.yagubogu.data.repository.member.MemberRepository
import com.yagubogu.data.repository.stadium.StadiumRepository
import com.yagubogu.data.repository.stats.StatsRepository
import com.yagubogu.data.repository.stream.StreamRepository
import com.yagubogu.data.util.ApiException
import com.yagubogu.domain.model.Coordinate
import com.yagubogu.domain.model.Distance
import com.yagubogu.presentation.mapper.toDomain
import com.yagubogu.presentation.mapper.toUiModel
import com.yagubogu.presentation.util.mapList
import com.yagubogu.ui.common.model.MemberProfile
import com.yagubogu.ui.home.model.CheckInSseEvent
import com.yagubogu.ui.home.model.CheckInUiEvent
import com.yagubogu.ui.home.model.HomeDialogEvent
import com.yagubogu.ui.home.model.MemberStatsUiModel
import com.yagubogu.ui.home.model.StadiumFanRateItem
import com.yagubogu.ui.home.model.StadiumStatsUiModel
import com.yagubogu.ui.home.model.StadiumWithGame
import com.yagubogu.ui.home.model.StadiumsWithGames
import com.yagubogu.ui.home.model.VictoryFairyRanking
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val memberRepository: MemberRepository,
    private val checkInRepository: CheckInRepository,
    private val statsRepository: StatsRepository,
    private val locationRepository: LocationRepository,
    private val stadiumRepository: StadiumRepository,
    private val streamRepository: StreamRepository,
    private val clock: Clock,
) : ViewModel() {
    private val _checkInUiEvent =
        MutableSharedFlow<CheckInUiEvent>(
            replay = 0,
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )
    val checkInUiEvent: SharedFlow<CheckInUiEvent> = _checkInUiEvent.asSharedFlow()

    private val _memberStatsUiModel = MutableStateFlow(MemberStatsUiModel())
    val memberStatsUiModel: StateFlow<MemberStatsUiModel> = _memberStatsUiModel.asStateFlow()

    private val _isStadiumStatsExpanded = MutableStateFlow(false)
    val isStadiumStatsExpanded: StateFlow<Boolean> = _isStadiumStatsExpanded.asStateFlow()

    private val _victoryFairyRanking = MutableStateFlow(VictoryFairyRanking())
    val victoryFairyRanking: StateFlow<VictoryFairyRanking> = _victoryFairyRanking.asStateFlow()

    private val _isCheckInLoading = MutableStateFlow(false)
    val isCheckInLoading: StateFlow<Boolean> = _isCheckInLoading.asStateFlow()

    private val _dialogEvent = MutableSharedFlow<HomeDialogEvent>()
    val dialogEvent: SharedFlow<HomeDialogEvent> = _dialogEvent.asSharedFlow()

    private var stadiums: StadiumsWithGames? = null

    private val cachedStadiumFanRateItems = LinkedHashMap<Long, StadiumFanRateItem>()
    private val _stadiumStatsUiModel = MutableStateFlow(StadiumStatsUiModel())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val sseFlow: Flow<StadiumStatsUiModel> =
        _stadiumStatsUiModel
            .map { it.stadiumFanRates.isNotEmpty() } // 데이터가 있는지 여부만 관찰
            .distinctUntilChanged() // 상태가 바뀔 때만 실행 (중복 연결 방지)
            .flatMapLatest { isNotEmpty: Boolean ->
                if (isNotEmpty) { // 데이터가 있으면 SSE 연결 시작
                    streamRepository
                        .connect()
                        .map { it.toUiModel() }
                        .mapNotNull { event: CheckInSseEvent ->
                            if (event is CheckInSseEvent.CheckInCreated) {
                                mergeCachedItems(event.items)
                                StadiumStatsUiModel(stadiumFanRates = cachedStadiumFanRateItems.values.toList())
                            } else {
                                null
                            }
                        }
                } else { // 데이터가 없으면 아무것도 안 함
                    emptyFlow()
                }
            }

    val stadiumStatsUiModel: StateFlow<StadiumStatsUiModel> =
        merge(_stadiumStatsUiModel, sseFlow)
            .stateIn(
                scope = viewModelScope,
                // UI가 보고 있을 때만 연결하고, 화면 꺼지면 5초 뒤 연결 끊음
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = StadiumStatsUiModel(),
            )

    fun fetchAll() {
        fetchMemberStats()
        fetchStadiumStats()
        fetchVictoryFairyRanking()
    }

    fun fetchStadiums(date: LocalDate = LocalDate.now()) {
        viewModelScope.launch {
            stadiumRepository
                .getStadiumsWithGames(date)
                .map { it.toUiModel() }
                .onSuccess { stadiumsWithGames: StadiumsWithGames ->
                    if (stadiumsWithGames.isEmpty()) {
                        _checkInUiEvent.emit(CheckInUiEvent.NoGame)
                    } else {
                        stadiums = stadiumsWithGames
                        fetchCheckInStatus(date)
                    }
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "API 호출 실패")
                    _checkInUiEvent.emit(CheckInUiEvent.NetworkFailed)
                }
        }
    }

    fun fetchCurrentLocationThenCheckIn() {
        _isCheckInLoading.value = true
        locationRepository.getCurrentCoordinate(
            onSuccess = { coordinateDto: CoordinateDto ->
                val currentCoordinate: Coordinate = coordinateDto.toDomain()
                checkIfWithinThresholdThenCheckIn(currentCoordinate)
                _isCheckInLoading.value = false
            },
            onFailure = { exception: Exception ->
                Timber.w(exception, "위치 불러오기 실패")
                _checkInUiEvent.tryEmit(CheckInUiEvent.LocationFetchFailed)
                _isCheckInLoading.value = false
            },
        )
    }

    fun checkIn(
        stadium: StadiumWithGame,
        gameId: Long,
    ) {
        viewModelScope.launch {
            checkInRepository
                .addCheckIn(gameId)
                .onSuccess {
                    _checkInUiEvent.emit(CheckInUiEvent.Success(stadium))

                    val currentMemberStatsUiModel: MemberStatsUiModel = memberStatsUiModel.value
                    _memberStatsUiModel.value =
                        currentMemberStatsUiModel.copy(attendanceCount = currentMemberStatsUiModel.attendanceCount + 1)
                    _isCheckInLoading.value = false
                }.onFailure { exception: Throwable ->
                    when (exception) {
                        is ApiException.Conflict -> _checkInUiEvent.emit(CheckInUiEvent.AlreadyCheckedIn)
                        else -> _checkInUiEvent.emit(CheckInUiEvent.NetworkFailed)
                    }
                    _isCheckInLoading.value = false
                    Timber.w(exception, "API 호출 실패")
                }
        }
    }

    fun hideCheckInDialog() {
        viewModelScope.launch {
            _dialogEvent.emit(HomeDialogEvent.HideDialog)
        }
    }

    fun toggleStadiumStats() {
        _isStadiumStatsExpanded.value = !isStadiumStatsExpanded.value
    }

    fun refreshStadiumStats() {
        _stadiumStatsUiModel.value = stadiumStatsUiModel.value.copy(refreshTime = LocalTime.now())
    }

    fun fetchMemberProfile(memberId: Long) {
        viewModelScope.launch {
            val memberProfileResult: Result<MemberProfile> =
                memberRepository.getMemberProfile(memberId).map { it.toUiModel() }
            memberProfileResult
                .onSuccess { memberProfile: MemberProfile ->
                    _dialogEvent.emit(HomeDialogEvent.ProfileDialog(memberProfile))
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "사용자 프로필 조회 API 호출 실패")
                }
        }
    }

    private fun fetchMemberStats(year: Int = LocalDate.now(clock).year) {
        viewModelScope.launch {
            val myTeamDeferred: Deferred<Result<String?>> =
                async { memberRepository.getFavoriteTeam() }
            val attendanceCountDeferred: Deferred<Result<Int>> =
                async { checkInRepository.getCheckInCounts(year) }
            val winRateDeferred: Deferred<Result<Double>> =
                async { statsRepository.getStatsWinRate(year) }

            val myTeamResult: Result<String?> = myTeamDeferred.await()
            val attendanceCountResult: Result<Int> = attendanceCountDeferred.await()
            val winRateResult: Result<Double> = winRateDeferred.await()

            if (myTeamResult.isSuccess && attendanceCountResult.isSuccess && winRateResult.isSuccess) {
                val myTeam: String? = myTeamResult.getOrThrow()
                val attendanceCount: Int = attendanceCountResult.getOrThrow()
                val winRate: Double = winRateResult.getOrThrow()

                val memberStatsUiModel =
                    MemberStatsUiModel(
                        myTeam = myTeam,
                        attendanceCount = attendanceCount,
                        winRate = winRate.roundToInt(),
                    )
                _memberStatsUiModel.value = memberStatsUiModel
            } else {
                val errors: List<String> =
                    listOf(myTeamResult, attendanceCountResult, winRateResult)
                        .filter { it.isFailure }
                        .mapNotNull { it.exceptionOrNull()?.message }
                Timber.w("API 호출 실패: ${errors.joinToString()}")
            }
        }
    }

    private fun fetchStadiumStats(date: LocalDate = LocalDate.now(clock)) {
        viewModelScope.launch {
            val stadiumFanRatesResult: Result<List<StadiumFanRateItem>> =
                checkInRepository.getStadiumFanRates(date).mapList { it.toUiModel() }
            stadiumFanRatesResult
                .onSuccess { stadiumFanRates: List<StadiumFanRateItem> ->
                    cachedStadiumFanRateItems.clear()
                    stadiumFanRates.forEach { stadiumFanRateItem: StadiumFanRateItem ->
                        cachedStadiumFanRateItems[stadiumFanRateItem.gameId] = stadiumFanRateItem
                    }
                    _stadiumStatsUiModel.value =
                        StadiumStatsUiModel(stadiumFanRates = stadiumFanRates)
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "API 호출 실패")
                }
        }
    }

    private fun fetchVictoryFairyRanking(year: Int = LocalDate.now(clock).year) {
        viewModelScope.launch {
            val victoryFairyRankingResult: Result<VictoryFairyRanking> =
                statsRepository.getVictoryFairyRankings(year, null).map { it.toUiModel() }
            victoryFairyRankingResult
                .onSuccess { ranking: VictoryFairyRanking ->
                    _victoryFairyRanking.value = ranking
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "API 호출 실패")
                }
        }
    }

    private fun fetchCheckInStatus(date: LocalDate) {
        viewModelScope.launch {
            checkInRepository
                .getCheckInStatus(date)
                .onSuccess { hasCheckedIn: Boolean ->
                    if (hasCheckedIn) {
                        _dialogEvent.emit(HomeDialogEvent.AdditionalCheckInDialog)
                    } else {
                        fetchCurrentLocationThenCheckIn()
                    }
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "API 호출 실패")
                }
        }
    }

    private fun checkIfWithinThresholdThenCheckIn(currentCoordinate: Coordinate) {
        val (nearestStadium: StadiumWithGame, nearestDistance: Distance) =
            stadiums?.findNearestTo(
                currentCoordinate,
            ) { coordinate: Coordinate, targetCoordinate: Coordinate ->
                locationRepository
                    .getDistanceInMeters(
                        coordinate.latitude.value,
                        coordinate.longitude.value,
                        targetCoordinate.latitude.value,
                        targetCoordinate.longitude.value,
                    ).toDomain()
            }
                ?: return

        if (!nearestDistance.isWithin(Distance(THRESHOLD_IN_METERS))) {
            _checkInUiEvent.tryEmit(CheckInUiEvent.OutOfRange)
            return
        }

        checkDoubleHeaderThenCheckIn(nearestStadium)
    }

    private fun checkDoubleHeaderThenCheckIn(stadium: StadiumWithGame) {
        viewModelScope.launch {
            if (stadium.isDoubleHeader()) {
                _dialogEvent.emit(HomeDialogEvent.DoubleHeaderDialog(stadium))
                return@launch
            }
            _dialogEvent.emit(HomeDialogEvent.CheckInDialog(stadium))
        }
    }

    private fun mergeCachedItems(newItems: List<StadiumFanRateItem>) {
        val validKeys: Set<Long> = newItems.map { it.gameId }.toSet()
        cachedStadiumFanRateItems.keys.retainAll(validKeys)
        newItems.forEach { item: StadiumFanRateItem ->
            cachedStadiumFanRateItems[item.gameId] = item
        }
    }

    companion object {
        private const val THRESHOLD_IN_METERS = 2200.0 // TODO: 300.0 으로 변경
    }
}
