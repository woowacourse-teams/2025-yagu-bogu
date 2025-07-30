package com.yagubogu.presentation.home

import android.util.Log
import androidx.lifecycle.LiveData
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
import com.yagubogu.presentation.home.model.HomeUiModel
import com.yagubogu.presentation.home.model.StadiumStatsUiModel
import com.yagubogu.presentation.home.model.TeamOccupancyRate
import com.yagubogu.presentation.home.model.TeamOccupancyRates
import com.yagubogu.presentation.home.model.TeamOccupancyStatus
import com.yagubogu.presentation.util.livedata.MutableSingleLiveData
import com.yagubogu.presentation.util.livedata.SingleLiveData
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.math.roundToInt

class HomeViewModel(
    private val memberRepository: MemberRepository,
    private val checkInsRepository: CheckInsRepository,
    private val statsRepository: StatsRepository,
    private val locationRepository: LocationRepository,
    private val stadiumRepository: StadiumRepository,
) : ViewModel() {
    private val _homeUiModel = MutableLiveData<HomeUiModel>()
    val homeUiModel: LiveData<HomeUiModel> get() = _homeUiModel

    private val _checkInUiEvent = MutableSingleLiveData<CheckInUiEvent>()
    val checkInUiEvent: SingleLiveData<CheckInUiEvent> get() = _checkInUiEvent

    private val _stadiumStatsUiModel: MutableLiveData<StadiumStatsUiModel> = MutableLiveData()
    val stadiumStatsUiModel: LiveData<StadiumStatsUiModel> get() = _stadiumStatsUiModel

    init {
        fetchMemberInformation(MEMBER_ID, YEAR)
    }

    fun checkIn() {
        locationRepository.getCurrentCoordinate(
            onSuccess = { currentCoordinate: Coordinate ->
                handleCheckIn(currentCoordinate)
            },
            onFailure = { exception: Exception ->
                Log.e(TAG, "위치 불러오기 실패", exception)
                _checkInUiEvent.setValue(CheckInUiEvent.LocationFetchFailed)
            },
        )
    }

    fun fetchStadiumStats(date: LocalDate) {
        viewModelScope.launch {
            val teamOccupancyRatesResult: Result<TeamOccupancyRates> =
                statsRepository.getTeamOccupancyRates(2, date)
            teamOccupancyRatesResult
                .onSuccess { teamOccupancyRates: TeamOccupancyRates ->
                    val teamOccupancyStatuses: List<TeamOccupancyStatus> =
                        teamOccupancyRates.rates.map { teamOccupancyRate: TeamOccupancyRate ->
                            TeamOccupancyStatus(
                                teamOccupancyRate.team,
                                teamOccupancyRate.occupancyRate,
                            )
                        }

                    _stadiumStatsUiModel.value =
                        StadiumStatsUiModel(
                            teamOccupancyRates.stadiumName,
                            teamOccupancyStatuses,
                        )
                }.onFailure { exception: Throwable ->
                    Log.e(TAG, "API 호출 실패", exception)
                }
        }
    }

    private fun fetchMemberInformation(
        memberId: Long,
        year: Int,
    ) {
        viewModelScope.launch {
            val myTeamDeferred: Deferred<Result<String>> =
                async { memberRepository.getFavoriteTeam(memberId) }
            val attendanceCountDeferred: Deferred<Result<Int>> =
                async { checkInsRepository.getCheckInCounts(memberId, year) }
            val winRateDeferred: Deferred<Result<Double>> =
                async { statsRepository.getStatsWinRate(memberId, year) }

            val myTeamResult: Result<String> = myTeamDeferred.await()
            val attendanceCountResult: Result<Int> = attendanceCountDeferred.await()
            val winRateResult: Result<Double> = winRateDeferred.await()

            if (myTeamResult.isSuccess && attendanceCountResult.isSuccess && winRateResult.isSuccess) {
                val myTeam: String = myTeamResult.getOrThrow()
                val attendanceCount: Int = attendanceCountResult.getOrThrow()
                val winRate: Double = winRateResult.getOrThrow()

                val homeUiModel =
                    HomeUiModel(
                        myTeam = myTeam,
                        attendanceCount = attendanceCount,
                        winRate = winRate.roundToInt(),
                    )
                _homeUiModel.value = homeUiModel
            } else {
                val errors: List<String> =
                    listOf(myTeamResult, attendanceCountResult, winRateResult)
                        .filter { it.isFailure }
                        .mapNotNull { it.exceptionOrNull()?.message }
                Log.e(TAG, "API 호출 실패: ${errors.joinToString()}")
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
                    Log.e(TAG, "API 호출 실패", stadiumsResult.exceptionOrNull())
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
            .addCheckIn(MEMBER_ID, nearestStadium.id, today)
            .onSuccess {
                _homeUiModel.value =
                    homeUiModel.value?.let { currentHomeUiModel: HomeUiModel ->
                        currentHomeUiModel.copy(attendanceCount = currentHomeUiModel.attendanceCount + 1)
                    }
                _checkInUiEvent.setValue(CheckInUiEvent.CheckInSuccess(nearestStadium))
            }.onFailure { exception: Throwable ->
                Log.e(TAG, "API 호출 실패", exception)
            }
    }

    companion object {
        private const val TAG = "HomeViewModel"
        private const val THRESHOLD_IN_METERS = 2200.0 // TODO: 300.0 으로 변경
        private const val MEMBER_ID = 5009L
        private const val YEAR = 2025
    }
}
