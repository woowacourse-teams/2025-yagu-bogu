package com.yagubogu

import android.app.Application
import com.google.android.gms.location.LocationServices
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.yagubogu.common.YaguBoguDebugTree
import com.yagubogu.common.YaguBoguReleaseTree
import com.yagubogu.data.datasource.CheckInsRemoteDataSource
import com.yagubogu.data.datasource.LocationLocalDataSource
import com.yagubogu.data.datasource.MemberRemoteDataSource
import com.yagubogu.data.datasource.StadiumRemoteDataSource
import com.yagubogu.data.datasource.StatsRemoteDataSource
import com.yagubogu.data.network.RetrofitInstance
import com.yagubogu.data.repository.CheckInsDefaultRepository
import com.yagubogu.data.repository.LocationDefaultRepository
import com.yagubogu.data.repository.MemberDefaultRepository
import com.yagubogu.data.repository.StadiumDefaultRepository
import com.yagubogu.data.repository.StatsDefaultRepository
import timber.log.Timber

class YaguBoguApplication : Application() {
    private val retrofit: RetrofitInstance by lazy { RetrofitInstance(this) }

    private val locationClient by lazy { LocationServices.getFusedLocationProviderClient(this) }
    private val locationDataSource by lazy { LocationLocalDataSource(locationClient) }
    val locationRepository by lazy { LocationDefaultRepository(locationDataSource) }

    private val memberDataSource by lazy { MemberRemoteDataSource(retrofit.memberApiService) }
    val memberRepository by lazy { MemberDefaultRepository(memberDataSource) }

    private val stadiumDataSource by lazy { StadiumRemoteDataSource(retrofit.stadiumApiService) }
    val stadiumRepository by lazy { StadiumDefaultRepository(stadiumDataSource) }

    private val checkInsDataSource by lazy { CheckInsRemoteDataSource(retrofit.checkInsApiService) }
    val checkInsRepository by lazy { CheckInsDefaultRepository(checkInsDataSource) }

    private val statsDataSource by lazy { StatsRemoteDataSource(retrofit.statsApiService) }
    val statsRepository by lazy { StatsDefaultRepository(statsDataSource) }

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            // 개발 환경
            FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = false
            Timber.plant(YaguBoguDebugTree())
        } else {
            // 운영 환경
            Timber.plant(YaguBoguReleaseTree())
        }
    }
}
