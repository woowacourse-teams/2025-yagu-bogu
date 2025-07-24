package com.yagubogu

import android.app.Application
import com.google.android.gms.location.LocationServices
import com.yagubogu.data.datasource.CheckInsRemoteDataSource
import com.yagubogu.data.datasource.LocationLocalDataSource
import com.yagubogu.data.datasource.StadiumRemoteDataSource
import com.yagubogu.data.datasource.StatsRemoteDataSource
import com.yagubogu.data.network.RetrofitInstance
import com.yagubogu.data.repository.CheckInsDefaultRepository
import com.yagubogu.data.repository.LocationDefaultRepository
import com.yagubogu.data.repository.StadiumDefaultRepository
import com.yagubogu.data.repository.StatsDefaultRepository

class YaguBoguApplication : Application() {
    private val locationClient by lazy { LocationServices.getFusedLocationProviderClient(this) }

    private val locationLocalDataSource by lazy { LocationLocalDataSource(locationClient) }
    val locationRepository by lazy { LocationDefaultRepository(locationLocalDataSource) }

    private val stadiumRemoteDataSource by lazy { StadiumRemoteDataSource(RetrofitInstance.stadiumApiService) }
    val stadiumRepository by lazy { StadiumDefaultRepository(stadiumRemoteDataSource) }

    private val checkInsRemoteDataSource by lazy { CheckInsRemoteDataSource(RetrofitInstance.checkInsApiService) }
    val checkInsRepository by lazy { CheckInsDefaultRepository(checkInsRemoteDataSource) }

    private val statsRemoteDataSource by lazy { StatsRemoteDataSource(RetrofitInstance.statsApiService) }
    val statsRepository by lazy { StatsDefaultRepository(statsRemoteDataSource) }
}
