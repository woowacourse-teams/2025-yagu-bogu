package com.yagubogu

import android.app.Application
import co.touchlab.kermit.ExperimentalKermitApi
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.crashlytics.CrashlyticsLogWriter
import co.touchlab.kermit.platformLogWriter
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.kmp.geofence.GeofenceBroadcastReceiver
import com.kmp.geofence.GeofenceContext
import com.kmp.geofence.GeofenceEvent
import com.kmp.geofence.TransitionType
import com.yagubogu.analytics.AnalyticsLogger
import com.yagubogu.analytics.FirebaseAnalyticsLogger
import com.yagubogu.di.authModule
import com.yagubogu.di.commonLocalModule
import com.yagubogu.di.commonModule
import com.yagubogu.di.datasourceModule
import com.yagubogu.di.localModule
import com.yagubogu.di.networkModule
import com.yagubogu.di.repositoryModule
import com.yagubogu.di.serviceModule
import com.yagubogu.di.timeModule
import com.yagubogu.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

@OptIn(ExperimentalKermitApi::class)
class YaguBoguApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        setupLogging()
        setupAnalytics()
        setupKoin()
        setupGeofence()
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

            modules(
                authModule,
                commonModule,
                datasourceModule,
                networkModule,
                repositoryModule,
                serviceModule,
                timeModule,
                viewModelModule,
                localModule,
                commonLocalModule,
            )
        }
    }

    private fun setupGeofence() {
        // Initialize context
        GeofenceContext.init(this)

        // Set listener here — Application is always alive
        GeofenceBroadcastReceiver.setEventListener { event: GeofenceEvent ->
            when (event.transitionType) {
                TransitionType.ENTER -> {
                    println("✅ 지오펜스 입장: ${event.geofenceId}")
                    // handle enter — call API, save to DB, send notification, etc.
                }
                TransitionType.EXIT -> {
                    println("✅ 지오펜스 퇴장: ${event.geofenceId}")
                    // handle exit
                }
            }
        }
    }
}
