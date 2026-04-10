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
import com.tweener.alarmee.AlarmeeService
import com.tweener.alarmee.model.Alarmee
import com.tweener.alarmee.model.AndroidNotificationConfiguration
import com.tweener.alarmee.model.AndroidNotificationPriority
import com.tweener.alarmee.model.IosNotificationConfiguration
import com.yagubogu.analytics.AnalyticsLogger
import com.yagubogu.analytics.FirebaseAnalyticsLogger
import com.yagubogu.di.alarmeeModule
import com.yagubogu.di.authModule
import com.yagubogu.di.commonLocalModule
import com.yagubogu.di.commonModule
import com.yagubogu.di.datasourceModule
import com.yagubogu.di.geofenceModule
import com.yagubogu.di.localModule
import com.yagubogu.di.networkModule
import com.yagubogu.di.repositoryModule
import com.yagubogu.di.serviceModule
import com.yagubogu.di.timeModule
import com.yagubogu.di.viewModelModule
import com.yagubogu.domain.model.Stadium.Companion.getStadiumById
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.notification_geofence_body
import yagubogu.composeapp.generated.resources.notification_geofence_title

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
                geofenceModule,
                alarmeeModule,
            )
        }
    }

    private fun setupGeofence() {
        val logger = Logger.withTag("GeofenceControllerImplAndroid")
        // Initialize context
        GeofenceContext.init(this)

        val alarmeeService: AlarmeeService by inject()

        // Set listener here — Application is always alive
        GeofenceBroadcastReceiver.setEventListener { event: GeofenceEvent ->
            when (event.transitionType) {
                TransitionType.ENTER -> {
                    logger.i { "안드로이드 지오펜스 입장: ${event.geofenceId}" }
                    val stadiumId = event.geofenceId.toIntOrNull() ?: return@setEventListener
                    val stadium = getStadiumById(stadiumId) ?: return@setEventListener
                    CoroutineScope(Dispatchers.IO).launch {
                        val notificationTitle = getString(Res.string.notification_geofence_title, stadium.name)
                        val notificationBody = getString(Res.string.notification_geofence_body)
                        alarmeeService.local.immediate(
                            alarmee =
                                Alarmee(
                                    uuid = "enter_$stadiumId",
                                    notificationTitle = notificationTitle,
                                    notificationBody = notificationBody,
                                    androidNotificationConfiguration =
                                        AndroidNotificationConfiguration(
                                            priority = AndroidNotificationPriority.HIGH,
                                            channelId = "geofenceChannelId",
                                        ),
                                    iosNotificationConfiguration = IosNotificationConfiguration(),
                                ),
                        )
                    }
                }
                TransitionType.EXIT -> {
                    logger.i { "안드로이드 지오펜스 퇴장: ${event.geofenceId}" }
                    // handle exit
                }
            }
        }
    }
}
