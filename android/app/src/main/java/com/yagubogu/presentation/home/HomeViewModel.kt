package com.yagubogu.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import com.yagubogu.domain.model.Coordinate
import com.yagubogu.domain.model.Stadium
import com.yagubogu.domain.repository.LocationRepository
import com.yagubogu.presentation.home.model.CheckInUiEvent
import com.yagubogu.presentation.util.livedata.MutableSingleLiveData
import com.yagubogu.presentation.util.livedata.SingleLiveData

class HomeViewModel(
    private val locationRepository: LocationRepository,
) : ViewModel() {
    private val _checkInUiEvent = MutableSingleLiveData<CheckInUiEvent>()
    val checkInUiEvent: SingleLiveData<CheckInUiEvent> get() = _checkInUiEvent

    fun checkIn() {
        locationRepository.getCurrentCoordinate(
            onSuccess = { currentCoordinate: Coordinate ->
                val nearestStadiumWithDistance = getNearestStadiumWithDistance(currentCoordinate)
                val (nearestStadium: Stadium, distance: Float) = nearestStadiumWithDistance

                if (isNearEnough(distance)) {
                    _checkInUiEvent.setValue(CheckInUiEvent.CheckInSuccess(nearestStadium))
                } else {
                    _checkInUiEvent.setValue(CheckInUiEvent.CheckInFailure)
                }
            },
            onFailure = { exception: Exception ->
                Log.e(TAG, "위치 불러오기 실패", exception)
                _checkInUiEvent.setValue(CheckInUiEvent.LocationFetchFailed)
            },
        )
    }

    private fun getNearestStadiumWithDistance(currentCoordinate: Coordinate): Pair<Stadium, Float> =
        Stadium.entries
            .map { stadium: Stadium ->
                val distance =
                    locationRepository.getDistanceInMeters(currentCoordinate, stadium.coordinate)
                stadium to distance
            }.minBy { it.second }

    private fun isNearEnough(distance: Float): Boolean = distance <= THRESHOLD_IN_METERS

    companion object {
        private const val THRESHOLD_IN_METERS = 300
        private const val TAG = "HomeViewModel"
    }
}
