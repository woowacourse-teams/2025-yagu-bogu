package com.yagubogu.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import com.yagubogu.domain.model.Coordinate
import com.yagubogu.domain.model.Stadium
import com.yagubogu.domain.repository.LocationRepository
import com.yagubogu.presentation.home.model.HomeUiEvent
import com.yagubogu.presentation.util.livedata.MutableSingleLiveData
import com.yagubogu.presentation.util.livedata.SingleLiveData

class HomeViewModel(
    private val locationRepository: LocationRepository,
) : ViewModel() {
    private val _uiEvent = MutableSingleLiveData<HomeUiEvent>()
    val uiEvent: SingleLiveData<HomeUiEvent> get() = _uiEvent

    fun checkIn() {
        locationRepository.getCurrentCoordinate(
            onSuccess = { currentCoordinate: Coordinate ->
                val nearestStadiumWithDistance = getNearestStadiumWithDistance(currentCoordinate)
                val nearestStadium = nearestStadiumWithDistance.first
                val distance = nearestStadiumWithDistance.second

                if (isNearEnough(distance)) {
                    _uiEvent.setValue(HomeUiEvent.CheckInSuccess(nearestStadium))
                } else {
                    _uiEvent.setValue(HomeUiEvent.CheckInFailure)
                }
            },
            onFailure = { exception ->
                Log.e(TAG, "위치 불러오기 실패", exception)
                _uiEvent.setValue(HomeUiEvent.LocationFetchFailed)
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
