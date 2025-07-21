package com.yagubogu.presentation.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.yagubogu.presentation.util.PermissionUtil

class LocationProvider(
    private val context: Context,
) {
    private val locationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    fun fetchCurrentLocation(
        onSuccess: (Location) -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        if (!isLocationPermissionGranted()) return
        locationClient
            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location: Location ->
                onSuccess(location)
            }.addOnFailureListener { exception: Exception ->
                onFailure(exception)
            }
    }

    fun isLocationPermissionGranted(): Boolean = isFineLocationPermissionGranted() || isCoarseLocationPermissionGranted()

    private fun isFineLocationPermissionGranted(): Boolean = PermissionUtil.isGranted(context, Manifest.permission.ACCESS_FINE_LOCATION)

    private fun isCoarseLocationPermissionGranted(): Boolean = PermissionUtil.isGranted(context, Manifest.permission.ACCESS_COARSE_LOCATION)
}
