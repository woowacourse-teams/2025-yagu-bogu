package com.yagubogu.presentation.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.data.util.ApiException
import com.yagubogu.domain.model.Coordinate
import com.yagubogu.domain.model.Distance
import com.yagubogu.domain.repository.CheckInRepository
import com.yagubogu.domain.repository.LocationRepository
import com.yagubogu.domain.repository.MemberRepository
import com.yagubogu.domain.repository.StadiumRepository
import com.yagubogu.domain.repository.StatsRepository
import com.yagubogu.domain.repository.StreamRepository
import com.yagubogu.presentation.home.model.CheckInSseEvent
import com.yagubogu.presentation.home.model.CheckInUiEvent
import com.yagubogu.presentation.home.model.HomeDialogEvent
import com.yagubogu.presentation.home.model.MemberStatsUiModel
import com.yagubogu.presentation.home.model.StadiumStatsUiModel
import com.yagubogu.presentation.home.model.StadiumWithGame
import com.yagubogu.presentation.home.model.StadiumsWithGames
import com.yagubogu.presentation.home.ranking.VictoryFairyRanking
import com.yagubogu.presentation.home.stadium.StadiumFanRateItem
import com.yagubogu.presentation.util.livedata.MutableSingleLiveData
import com.yagubogu.presentation.util.livedata.SingleLiveData
import com.yagubogu.ui.dialog.model.MemberProfile
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.roundToInt

class HomeViewModel(
    private val memberRepository: MemberRepository,
    private val checkInRepository: CheckInRepository,
    private val statsRepository: StatsRepository,
    private val locationRepository: LocationRepository,
    private val stadiumRepository: StadiumRepository,
    private val streamRepository: StreamRepository,
) : ViewModel() {
    private val _memberStatsUiModel = MutableLiveData<MemberStatsUiModel>()
    val memberStatsUiModel: LiveData<MemberStatsUiModel> get() = _memberStatsUiModel

    private val _checkInUiEvent = MutableSingleLiveData<CheckInUiEvent>()
    val checkInUiEvent: SingleLiveData<CheckInUiEvent> get() = _checkInUiEvent

    private val cachedStadiumFanRateItems = mutableMapOf<Long, StadiumFanRateItem>()
    private val stadiumFanRateItems = MutableLiveData<List<StadiumFanRateItem>>()

    private val _isStadiumStatsExpanded = MutableLiveData(false)
    val isStadiumStatsExpanded: LiveData<Boolean> get() = _isStadiumStatsExpanded

    val isShowMoreVisible: LiveData<Boolean> =
        MediatorLiveData<Boolean>().apply {
            addSource(stadiumFanRateItems) { value = it.size > 1 }
        }

    private val _stadiumStatsUiModel: MutableLiveData<StadiumStatsUiModel> =
        MediatorLiveData<StadiumStatsUiModel>().apply {
            addSource(stadiumFanRateItems) { updateStadiumStats() }
            addSource(isStadiumStatsExpanded) { updateStadiumStats() }
        }
    val stadiumStatsUiModel: LiveData<StadiumStatsUiModel> get() = _stadiumStatsUiModel

    private val _victoryFairyRanking = MutableLiveData<VictoryFairyRanking>()
    val victoryFairyRanking: LiveData<VictoryFairyRanking> get() = _victoryFairyRanking

    private val _isCheckInLoading = MutableLiveData<Boolean>()
    val isCheckInLoading: LiveData<Boolean> get() = _isCheckInLoading

    private val _dialogEvent = MutableSharedFlow<HomeDialogEvent>()
    val dialogEvent: SharedFlow<HomeDialogEvent> = _dialogEvent.asSharedFlow()

    private var stadiums: StadiumsWithGames? = null

    private val _profileImageClickEvent = MutableLiveData<MemberProfile?>()
    val profileImageClickEvent: LiveData<MemberProfile?> = _profileImageClickEvent

    init {
        fetchAll()
    }

    fun fetchAll() {
        fetchMemberStats()
        fetchStadiumStats()
        fetchVictoryFairyRanking()
    }

    fun startStreaming() {
        viewModelScope.launch {
            streamRepository.connect().collect { event: CheckInSseEvent ->
                when (event) {
                    is CheckInSseEvent.CheckInCreated -> {
                        val newItems: List<StadiumFanRateItem> = event.items
                        val validKeys: Set<Long> = newItems.map { it.gameId }.toSet()
                        cachedStadiumFanRateItems.keys.retainAll(validKeys)

                        newItems.forEach { item: StadiumFanRateItem ->
                            cachedStadiumFanRateItems[item.gameId] = item
                        }
                        stadiumFanRateItems.value = newItems
                    }

                    CheckInSseEvent.Connect,
                    CheckInSseEvent.Timeout,
                    CheckInSseEvent.Unknown,
                    -> Unit
                }
            }
        }
    }

    fun stopStreaming() {
        streamRepository.disconnect()
    }

    fun fetchStadiums(date: LocalDate = LocalDate.now()) {
        viewModelScope.launch {
            stadiumRepository
                .getStadiumsWithGames(date)
                .onSuccess { stadiumsWithGames: StadiumsWithGames ->
                    if (stadiumsWithGames.isEmpty()) {
                        _checkInUiEvent.setValue(CheckInUiEvent.NoGame)
                    } else {
                        stadiums = stadiumsWithGames
                        fetchCheckInStatus(date)
                    }
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "API 호출 실패")
                    _checkInUiEvent.setValue(CheckInUiEvent.NetworkFailed)
                }
        }
    }

    fun fetchCurrentLocationThenCheckIn() {
        _isCheckInLoading.value = true
        locationRepository.getCurrentCoordinate(
            onSuccess = { currentCoordinate: Coordinate ->
                checkIfWithinThresholdThenCheckIn(currentCoordinate)
                _isCheckInLoading.value = false
            },
            onFailure = { exception: Exception ->
                Timber.w(exception, "위치 불러오기 실패")
                _checkInUiEvent.setValue(CheckInUiEvent.LocationFetchFailed)
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
                    _checkInUiEvent.setValue(CheckInUiEvent.Success(stadium))
                    _memberStatsUiModel.value =
                        memberStatsUiModel.value?.let { currentMemberStatsUiModel: MemberStatsUiModel ->
                            currentMemberStatsUiModel.copy(attendanceCount = currentMemberStatsUiModel.attendanceCount + 1)
                        }
                    _isCheckInLoading.value = false
                }.onFailure { exception: Throwable ->
                    when (exception) {
                        is ApiException.Conflict -> _checkInUiEvent.setValue(CheckInUiEvent.AlreadyCheckedIn)
                        else -> _checkInUiEvent.setValue(CheckInUiEvent.NetworkFailed)
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

    fun updateStadiumStats() {
        val items: List<StadiumFanRateItem> = cachedStadiumFanRateItems.values.toList()
        val isExpanded: Boolean = isStadiumStatsExpanded.value ?: false

        val newItems = if (!isExpanded) listOfNotNull(items.firstOrNull()) else items
        val newStadiumStats =
            StadiumStatsUiModel(
                stadiumFanRates = newItems,
                refreshTime = LocalTime.now(),
            )
        _stadiumStatsUiModel.value = newStadiumStats
    }

    fun toggleStadiumStats() {
        _isStadiumStatsExpanded.value = isStadiumStatsExpanded.value?.not() ?: true
    }

    fun fetchMemberProfile(memberId: Long) {
        viewModelScope.launch {
            val memberProfileResult: Result<MemberProfile> =
                memberRepository.getMemberProfile(memberId)
            memberProfileResult
                .onSuccess { memberProfile: MemberProfile ->
                    _profileImageClickEvent.value = memberProfile
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "사용자 프로필 조회 API 호출 실패")
                }
        }
    }

    fun clearMemberProfileEvent() {
        _profileImageClickEvent.value = null
    }

    private fun fetchMemberStats(year: Int = LocalDate.now().year) {
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

    private fun fetchStadiumStats(date: LocalDate = LocalDate.now()) {
        viewModelScope.launch {
            val stadiumFanRatesResult: Result<List<StadiumFanRateItem>> =
                checkInRepository.getStadiumFanRates(date)
            stadiumFanRatesResult
                .onSuccess { stadiumFanRates: List<StadiumFanRateItem> ->
                    cachedStadiumFanRateItems.clear()
                    stadiumFanRates.forEach { stadiumFanRateItem: StadiumFanRateItem ->
                        cachedStadiumFanRateItems[stadiumFanRateItem.gameId] = stadiumFanRateItem
                    }
                    stadiumFanRateItems.value = stadiumFanRates
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "API 호출 실패")
                }
        }
    }

    private fun fetchVictoryFairyRanking(year: Int = LocalDate.now().year) {
        viewModelScope.launch {
            val victoryFairyRankingResult: Result<VictoryFairyRanking> =
                statsRepository.getVictoryFairyRankings(year, null)
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
                locationRepository::getDistanceInMeters,
            ) ?: return

        if (!nearestDistance.isWithin(Distance(THRESHOLD_IN_METERS))) {
            _checkInUiEvent.setValue(CheckInUiEvent.OutOfRange)
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

    companion object {
        private const val THRESHOLD_IN_METERS = 2200.0 // TODO: 300.0 으로 변경
    }
}
