package com.yagubogu

import android.app.Application
import co.touchlab.kermit.ExperimentalKermitApi
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.crashlytics.CrashlyticsLogWriter
import co.touchlab.kermit.platformLogWriter
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.kmp.geofence.GeofenceBroadcastReceiver
import com.kmp.geofence.GeofenceContext
import com.kmp.geofence.GeofenceEvent
import com.kmp.geofence.TransitionType
import com.yagubogu.analytics.AnalyticsLogger
import com.yagubogu.analytics.FirebaseAnalyticsLogger
import com.yagubogu.di.sharedModules
import com.yagubogu.domain.geofence.SendGeofenceNotificationUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

@OptIn(ExperimentalKermitApi::class)
class YaguBoguApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        setupLogging()
        setupAnalytics()
        setupAds()
        setupKoin()
        setupGeofence()
    }

    private fun setupAds() {
        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(
                this@YaguBoguApplication,
                InitializationConfig.Builder(BuildKonfig.ADMOB_ANDROID_APP_ID).build(),
            )
        }
    }

    private fun setupAnalytics() {
        AnalyticsLogger.initialize(FirebaseAnalyticsLogger())
    }

    private fun setupLogging() {
        if (BuildKonfig.IS_DEBUG) {
            // 개발 환경
            FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = false
            FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(false)
        }
        Logger.setLogWriters(
            if (BuildKonfig.IS_DEBUG) {
                platformLogWriter()
            } else {
                CrashlyticsLogWriter(Severity.Info)
            },
        )
    }

    private fun setupKoin() {
        startKoin {
            androidContext(androidContext = this@YaguBoguApplication)

            modules(sharedModules)
        }
    }

    private fun setupGeofence() {
        val logger = Logger.withTag("GeofenceAndroid")
        GeofenceContext.init(this)

        val sendGeofenceNotificationUseCase: SendGeofenceNotificationUseCase by inject()

        GeofenceBroadcastReceiver.setEventListener { event: GeofenceEvent ->
            when (event.transitionType) {
                TransitionType.ENTER -> {
                    logger.i { "Android 지오펜스 입장 감지: ${event.geofenceId}" }
                    val stadiumId = event.geofenceId.toIntOrNull() ?: return@setEventListener
                    logger.i { "안드로이드 지오펜스 입장 브로드캐스트 리스너 수신: $stadiumId" }
                    CoroutineScope(Dispatchers.IO).launch {
                        sendGeofenceNotificationUseCase(stadiumId)
                    }
                }
                TransitionType.EXIT -> logger.i { "Android 지오펜스 퇴장 감지: ${event.geofenceId}" }
            }
        }
    }
}
