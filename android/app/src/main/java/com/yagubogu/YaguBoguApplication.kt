package com.yagubogu

import android.app.Application
import com.google.android.gms.location.LocationServices
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.yagubogu.common.YaguBoguDebugTree
import com.yagubogu.common.YaguBoguReleaseTree
import com.yagubogu.data.datasource.auth.AuthRemoteDataSource
import com.yagubogu.data.datasource.checkin.CheckInRemoteDataSource
import com.yagubogu.data.datasource.game.GameRemoteDataSource
import com.yagubogu.data.datasource.location.LocationLocalDataSource
import com.yagubogu.data.datasource.member.MemberRemoteDataSource
import com.yagubogu.data.datasource.stadium.StadiumRemoteDataSource
import com.yagubogu.data.datasource.stats.StatsRemoteDataSource
import com.yagubogu.data.datasource.talk.TalkRemoteDataSource
import com.yagubogu.data.datasource.token.TokenRemoteDataSource
import com.yagubogu.data.network.RetrofitInstance
import com.yagubogu.data.network.TokenManager
import com.yagubogu.data.repository.AuthDefaultRepository
import com.yagubogu.data.repository.CheckInDefaultRepository
import com.yagubogu.data.repository.GameDefaultRepository
import com.yagubogu.data.repository.LocationDefaultRepository
import com.yagubogu.data.repository.MemberDefaultRepository
import com.yagubogu.data.repository.StadiumDefaultRepository
import com.yagubogu.data.repository.StatsDefaultRepository
import com.yagubogu.data.repository.TalkDefaultRepository
import com.yagubogu.data.repository.TokenDefaultRepository
import timber.log.Timber

class YaguBoguApplication : Application() {
    private val tokenManager by lazy { TokenManager(this) }
    private val retrofit by lazy { RetrofitInstance(tokenManager) }

    private val locationClient by lazy { LocationServices.getFusedLocationProviderClient(this) }
    private val locationDataSource by lazy { LocationLocalDataSource(locationClient) }
    val locationRepository by lazy { LocationDefaultRepository(locationDataSource) }

    private val tokenDataSource by lazy { TokenRemoteDataSource(retrofit.tokenApiService) }
    val tokenRepository by lazy { TokenDefaultRepository(tokenDataSource, tokenManager) }

    private val authDataSource by lazy { AuthRemoteDataSource(retrofit.authApiService) }
    val authRepository by lazy { AuthDefaultRepository(authDataSource, tokenManager) }

    private val memberDataSource by lazy { MemberRemoteDataSource(retrofit.memberApiService) }
    val memberRepository by lazy { MemberDefaultRepository(memberDataSource) }

    private val stadiumDataSource by lazy { StadiumRemoteDataSource(retrofit.stadiumApiService) }
    val stadiumRepository by lazy { StadiumDefaultRepository(stadiumDataSource) }

    private val checkInsDataSource by lazy { CheckInRemoteDataSource(retrofit.checkInApiService) }
    val checkInsRepository by lazy { CheckInDefaultRepository(checkInsDataSource) }

    private val statsDataSource by lazy { StatsRemoteDataSource(retrofit.statsApiService) }
    val statsRepository by lazy { StatsDefaultRepository(statsDataSource) }

    private val gamesDataSource by lazy { GameRemoteDataSource(retrofit.gameApiService) }
    val gamesRepository by lazy { GameDefaultRepository(gamesDataSource) }

    private val talksDataSource by lazy { TalkRemoteDataSource(retrofit.talkApiService) }
    val talksRepository by lazy { TalkDefaultRepository(talksDataSource) }

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
