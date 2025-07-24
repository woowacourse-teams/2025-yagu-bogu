package com.yagubogu.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.domain.model.Coordinate
import com.yagubogu.domain.model.Distance
import com.yagubogu.domain.model.Stadium
import com.yagubogu.domain.model.Stadiums
import com.yagubogu.domain.repository.CheckInsRepository
import com.yagubogu.domain.repository.LocationRepository
import com.yagubogu.domain.repository.StadiumRepository
import com.yagubogu.presentation.home.model.CheckInUiEvent
import com.yagubogu.presentation.util.livedata.MutableSingleLiveData
import com.yagubogu.presentation.util.livedata.SingleLiveData
import kotlinx.coroutines.launch
import java.time.LocalDate

class HomeViewModel(
    private val locationRepository: LocationRepository,
    private val stadiumRepository: StadiumRepository,
    private val checkInsRepository: CheckInsRepository,
) : ViewModel() {
    private val _checkInUiEvent = MutableSingleLiveData<CheckInUiEvent>()
    val checkInUiEvent: SingleLiveData<CheckInUiEvent> get() = _checkInUiEvent

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

    private fun handleCheckIn(currentCoordinate: Coordinate) {
        viewModelScope.launch {
            stadiumRepository
                .getStadiums()
                .onSuccess { stadiums: Stadiums ->
                    val (nearestStadium: Stadium, nearestDistance: Distance) =
                        stadiums.findNearestTo(
                            currentCoordinate,
                            locationRepository::getDistanceInMeters,
                        )
                    if (nearestDistance.isWithin(Distance(THRESHOLD_IN_METERS))) {
                        val today = LocalDate.now()
                        checkInsRepository.addCheckIn(MEMBER_ID, nearestStadium.id, today)
                        _checkInUiEvent.setValue(CheckInUiEvent.CheckInSuccess(nearestStadium))
                    } else {
                        _checkInUiEvent.setValue(CheckInUiEvent.CheckInFailure)
                    }
                }.onFailure { exception: Throwable ->
                    Log.e(TAG, "API 호출 실패", exception)
                }
        }
    }

    companion object {
        private const val TAG = "HomeViewModel"
        private const val THRESHOLD_IN_METERS = 2200.0
        private const val MEMBER_ID = 1L
    }
}
