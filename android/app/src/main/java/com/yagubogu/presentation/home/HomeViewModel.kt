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
import com.yagubogu.domain.repository.CheckInsRepository
import com.yagubogu.domain.repository.LocationRepository
import com.yagubogu.domain.repository.MemberRepository
import com.yagubogu.domain.repository.StadiumRepository
import com.yagubogu.domain.repository.StatsRepository
import com.yagubogu.presentation.home.model.CheckInUiEvent
import com.yagubogu.presentation.home.model.MemberStatsUiModel
import com.yagubogu.presentation.home.model.StadiumStatsUiModel
import com.yagubogu.presentation.home.stadium.StadiumFanRateItem
import com.yagubogu.presentation.util.livedata.MutableSingleLiveData
import com.yagubogu.presentation.util.livedata.SingleLiveData
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import kotlin.math.roundToInt

class HomeViewModel(
    private val memberRepository: MemberRepository,
    private val checkInsRepository: CheckInsRepository,
    private val statsRepository: StatsRepository,
    private val locationRepository: LocationRepository,
    private val stadiumRepository: StadiumRepository,
) : ViewModel() {
    private val _memberStatsUiModel = MutableLiveData<MemberStatsUiModel>()
    val memberStatsUiModel: LiveData<MemberStatsUiModel> get() = _memberStatsUiModel

    private val _checkInUiEvent = MutableSingleLiveData<CheckInUiEvent>()
    val checkInUiEvent: SingleLiveData<CheckInUiEvent> get() = _checkInUiEvent

    private val stadiumFanRateItems = MutableLiveData<List<StadiumFanRateItem>>()

    private val _isStadiumStatsExpanded = MutableLiveData(false)
    val isStadiumStatsExpanded: LiveData<Boolean> get() = _isStadiumStatsExpanded

    val stadiumStatsUiModel: LiveData<StadiumStatsUiModel> =
        MediatorLiveData<StadiumStatsUiModel>().apply {
            addSource(stadiumFanRateItems) { value = updateStadiumStats() }
            addSource(_isStadiumStatsExpanded) { value = updateStadiumStats() }
        }

    init {
        fetchAll()
    }

    fun fetchAll() {
        fetchMemberStats(YEAR)
        fetchStadiumStats(DATE)
    }

    fun checkIn() {
        locationRepository.getCurrentCoordinate(
            onSuccess = { currentCoordinate: Coordinate ->
                handleCheckIn(currentCoordinate)
            },
            onFailure = { exception: Exception ->
                Timber.w(exception, "위치 불러오기 실패")
                _checkInUiEvent.setValue(CheckInUiEvent.LocationFetchFailed)
            },
        )
    }

    fun fetchStadiumStats(date: LocalDate) {
        viewModelScope.launch {
            val stadiumFanRatesResult: Result<List<StadiumFanRateItem>> =
                checkInsRepository.getStadiumFanRates(date)
            stadiumFanRatesResult
                .onSuccess { stadiumFanRates: List<StadiumFanRateItem> ->
                    stadiumFanRateItems.value = stadiumFanRates
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "API 호출 실패")
                }
        }
    }

    fun toggleStadiumStats() {
        _isStadiumStatsExpanded.value = isStadiumStatsExpanded.value?.not() ?: true
    }

    private fun fetchMemberStats(year: Int) {
        viewModelScope.launch {
            val myTeamDeferred: Deferred<Result<String>> =
                async { memberRepository.getFavoriteTeam() }
            val attendanceCountDeferred: Deferred<Result<Int>> =
                async { checkInsRepository.getCheckInCounts(year) }
            val winRateDeferred: Deferred<Result<Double>> =
                async { statsRepository.getStatsWinRate(year) }

            val myTeamResult: Result<String> = myTeamDeferred.await()
            val attendanceCountResult: Result<Int> = attendanceCountDeferred.await()
            val winRateResult: Result<Double> = winRateDeferred.await()

            if (myTeamResult.isSuccess && attendanceCountResult.isSuccess && winRateResult.isSuccess) {
                val myTeam: String = myTeamResult.getOrThrow()
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

    private fun handleCheckIn(currentCoordinate: Coordinate) {
        viewModelScope.launch {
            val stadiumsResult: Result<Stadiums> = stadiumRepository.getStadiums()
            stadiumsResult
                .onSuccess { stadiums: Stadiums ->
                    checkInIfWithinThreshold(currentCoordinate, stadiums)
                }.onFailure {
                    Timber.w(stadiumsResult.exceptionOrNull(), "API 호출 실패")
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
            _checkInUiEvent.setValue(CheckInUiEvent.CheckInFailure)
            return
        }

        val today = LocalDate.now()
        checkInsRepository
            .addCheckIn(nearestStadium.id, today)
            .onSuccess {
                _memberStatsUiModel.value =
                    memberStatsUiModel.value?.let { currentMemberStatsUiModel: MemberStatsUiModel ->
                        currentMemberStatsUiModel.copy(attendanceCount = currentMemberStatsUiModel.attendanceCount + 1)
                    }
                _checkInUiEvent.setValue(CheckInUiEvent.CheckInSuccess(nearestStadium))
            }.onFailure { exception: Throwable ->
                Timber.w(exception, "API 호출 실패")
            }
    }

    private fun updateStadiumStats(): StadiumStatsUiModel {
        val items: List<StadiumFanRateItem> = stadiumFanRateItems.value.orEmpty()
        val isExpanded: Boolean = isStadiumStatsExpanded.value ?: false

        return if (!isExpanded) {
            val firstItem: StadiumFanRateItem? = items.firstOrNull()
            StadiumStatsUiModel(if (firstItem != null) listOf(firstItem) else emptyList())
        } else {
            StadiumStatsUiModel(items)
        }
    }

    companion object {
        private const val THRESHOLD_IN_METERS = 2200.0 // TODO: 300.0 으로 변경
        private const val YEAR = 2025
        private val DATE = LocalDate.of(2025, 7, 25) // TODO: LocalDate.now()로 변경
    }
}
