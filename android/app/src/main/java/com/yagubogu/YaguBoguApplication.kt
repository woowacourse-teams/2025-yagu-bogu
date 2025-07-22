package com.yagubogu

import android.app.Application
import com.google.android.gms.location.LocationServices
import com.yagubogu.data.datasource.LocationLocalDataSource
import com.yagubogu.data.repository.LocationDefaultRepository

class YaguBoguApplication : Application() {
    private val locationClient by lazy { LocationServices.getFusedLocationProviderClient(this) }

    private val locationLocalDataSource by lazy { LocationLocalDataSource(locationClient) }
    val locationRepository by lazy { LocationDefaultRepository(locationLocalDataSource) }
}
