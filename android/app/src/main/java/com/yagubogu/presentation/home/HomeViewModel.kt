package com.yagubogu.presentation.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.domain.model.Coordinate
import com.yagubogu.domain.model.Distance
import com.yagubogu.domain.model.Stadium
import com.yagubogu.domain.model.Stadiums
import com.yagubogu.domain.repository.CheckInRepository
import com.yagubogu.domain.repository.LocationRepository
import com.yagubogu.domain.repository.MemberRepository
import com.yagubogu.domain.repository.StadiumRepository
import com.yagubogu.domain.repository.StatsRepository
import com.yagubogu.presentation.home.model.CheckInUiEvent
import com.yagubogu.presentation.home.model.MemberStatsUiModel
import com.yagubogu.presentation.home.model.StadiumStatsUiModel
import com.yagubogu.presentation.home.ranking.VictoryFairyRanking
import com.yagubogu.presentation.home.stadium.StadiumFanRateItem
import com.yagubogu.presentation.util.livedata.MutableSingleLiveData
import com.yagubogu.presentation.util.livedata.SingleLiveData
import com.yagubogu.ui.dialog.model.MemberProfile
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
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
) : ViewModel() {
    private val _memberStatsUiModel = MutableLiveData<MemberStatsUiModel>()
    val memberStatsUiModel: LiveData<MemberStatsUiModel> get() = _memberStatsUiModel

    private val _checkInUiEvent = MutableSingleLiveData<CheckInUiEvent>()
    val checkInUiEvent: SingleLiveData<CheckInUiEvent> get() = _checkInUiEvent

    private val _stadiumFanRateItems = MutableLiveData<List<StadiumFanRateItem>>()
    val stadiumFanRateItems: LiveData<List<StadiumFanRateItem>> get() = _stadiumFanRateItems

    private val _isStadiumStatsExpanded = MutableLiveData(false)
    val isStadiumStatsExpanded: LiveData<Boolean> get() = _isStadiumStatsExpanded

    val stadiumStatsUiModel: LiveData<StadiumStatsUiModel> =
        MediatorLiveData<StadiumStatsUiModel>().apply {
            addSource(stadiumFanRateItems) { value = updateStadiumStats(isRefreshed = true) }
            addSource(isStadiumStatsExpanded) { value = updateStadiumStats() }
        }

    private val _victoryFairyRanking = MutableLiveData<VictoryFairyRanking>()
    val victoryFairyRanking: LiveData<VictoryFairyRanking> get() = _victoryFairyRanking

    private val _isCheckInLoading = MutableLiveData<Boolean>()
    val isCheckInLoading: LiveData<Boolean> get() = _isCheckInLoading

    private val _hasAlreadyCheckedIn = MutableLiveData<Boolean>()
    val hasAlreadyCheckedIn: LiveData<Boolean> get() = _hasAlreadyCheckedIn

    private val _profileImageClickEvent = MutableLiveData<MemberProfile?>()
    val profileImageClickEvent: LiveData<MemberProfile?> = _profileImageClickEvent

    init {
        fetchAll()
    }

    fun fetchAll() {
        fetchCheckInStatus()
        fetchMemberStats()
        fetchStadiumStats()
        fetchVictoryFairyRanking()
    }

    fun checkIn() {
        _isCheckInLoading.value = true
        locationRepository.getCurrentCoordinate(
            onSuccess = { currentCoordinate: Coordinate ->
                handleCheckIn(currentCoordinate)
            },
            onFailure = { exception: Exception ->
                Timber.w(exception, "위치 불러오기 실패")
                _checkInUiEvent.setValue(CheckInUiEvent.LocationFetchFailed)
                _isCheckInLoading.value = false
            },
        )
    }

    fun fetchStadiumStats(date: LocalDate = LocalDate.now()) {
        viewModelScope.launch {
            val stadiumFanRatesResult: Result<List<StadiumFanRateItem>> =
                checkInRepository.getStadiumFanRates(date)
            stadiumFanRatesResult
                .onSuccess { stadiumFanRates: List<StadiumFanRateItem> ->
                    _stadiumFanRateItems.value = stadiumFanRates
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "API 호출 실패")
                }
        }
    }

    fun toggleStadiumStats() {
        _isStadiumStatsExpanded.value = isStadiumStatsExpanded.value?.not() ?: true
    }

    fun fetchMemberProfile(memberId: Long) {
//        viewModelScope.launch {
//            val memberProfileResult: Result<MemberProfile> =
//                memberRepository.getMemberProfile(memberId)
//            memberProfileResult
//                .onSuccess { memberProfile: MemberProfile ->
//                    _profileImageClickEvent.setValue(memberProfile)
//                }.onFailure { exception: Throwable ->
//                    Timber.w(exception, "사용자 프로필 조회 API 호출 실패")
//                }
//        }
        // TODO API 배포되면 주석 제거
        _profileImageClickEvent.setValue(
            MemberProfile(
                nickname = "우가우가귀여운보욱이우가",
                enterDate = "2025-08-22",
                profileImageUrl = "",
                favoriteTeam = "KIA",
                representativeBadgeName = "말문이 트이다",
                representativeBadgeImageUrl = "",
                victoryFairyRanking = 275,
                victoryFairyScore = 33,
                checkInCounts = 11,
                checkInWinRate = "60%",
            ),
        )
    }

    fun clearMemberProfileEvent() {
        _profileImageClickEvent.setValue(null)
    }

    private fun fetchCheckInStatus(date: LocalDate = LocalDate.now()) {
        viewModelScope.launch {
            val checkInStatusResult: Result<Boolean> = checkInRepository.getCheckInStatus(date)
            checkInStatusResult
                .onSuccess { checkInStatus: Boolean ->
                    _hasAlreadyCheckedIn.value = checkInStatus
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "API 호출 실패")
                }
        }
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

    private fun fetchVictoryFairyRanking() {
        viewModelScope.launch {
            val victoryFairyRankingResult: Result<VictoryFairyRanking> =
                checkInRepository.getVictoryFairyRankings()
            victoryFairyRankingResult
                .onSuccess { ranking: VictoryFairyRanking ->
                    _victoryFairyRanking.value = ranking
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "API 호출 실패")
                }
        }
    }

    private fun handleCheckIn(currentCoordinate: Coordinate) {
        viewModelScope.launch {
            val stadiumsResult: Result<Stadiums> = stadiumRepository.getStadiums()
            stadiumsResult
                .onSuccess { stadiums: Stadiums ->
                    checkInIfWithinThreshold(currentCoordinate, stadiums)
                }.onFailure {
                    Timber.w(stadiumsResult.exceptionOrNull(), "API 호출 실패")
                    _checkInUiEvent.setValue(CheckInUiEvent.NetworkFailed)
                    _isCheckInLoading.value = false
                }
        }
    }

    private suspend fun checkInIfWithinThreshold(
        currentCoordinate: Coordinate,
        stadiums: Stadiums,
    ) {
        val (nearestStadium: Stadium, nearestDistance: Distance) =
            stadiums.findNearestTo(currentCoordinate, locationRepository::getDistanceInMeters)

        if (!nearestDistance.isWithin(Distance(THRESHOLD_IN_METERS))) {
            _checkInUiEvent.setValue(CheckInUiEvent.OutOfRange)
            _isCheckInLoading.value = false
            return
        }

        val today = LocalDate.now()
        checkInRepository
            .addCheckIn(nearestStadium.id, today)
            .onSuccess {
                _checkInUiEvent.setValue(CheckInUiEvent.Success(nearestStadium))
                _hasAlreadyCheckedIn.value = true
                _memberStatsUiModel.value =
                    memberStatsUiModel.value?.let { currentMemberStatsUiModel: MemberStatsUiModel ->
                        currentMemberStatsUiModel.copy(attendanceCount = currentMemberStatsUiModel.attendanceCount + 1)
                    }
                _isCheckInLoading.value = false
            }.onFailure { exception: Throwable ->
                Timber.w(exception, "API 호출 실패")
                _checkInUiEvent.setValue(CheckInUiEvent.NetworkFailed)
                _isCheckInLoading.value = false
            }
    }

    private fun updateStadiumStats(isRefreshed: Boolean = false): StadiumStatsUiModel {
        val items: List<StadiumFanRateItem> = stadiumFanRateItems.value.orEmpty()
        val isExpanded: Boolean = isStadiumStatsExpanded.value ?: false
        val currentStadiumStats: StadiumStatsUiModel =
            stadiumStatsUiModel.value ?: StadiumStatsUiModel(emptyList(), LocalTime.now())

        val newItems = if (!isExpanded) listOfNotNull(items.firstOrNull()) else items
        return if (isRefreshed) {
            currentStadiumStats.copy(stadiumFanRates = newItems, refreshTime = LocalTime.now())
        } else {
            currentStadiumStats.copy(stadiumFanRates = newItems)
        }
    }

    companion object {
        private const val THRESHOLD_IN_METERS = 2200.0 // TODO: 300.0 으로 변경
    }
}
