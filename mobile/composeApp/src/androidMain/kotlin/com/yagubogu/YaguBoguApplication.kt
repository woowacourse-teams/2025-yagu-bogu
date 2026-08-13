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
import com.yagubogu.analytics.AnalyticsLogger
import com.yagubogu.analytics.FirebaseAnalyticsLogger
import com.yagubogu.di.authModule
import com.yagubogu.di.commonModule
import com.yagubogu.di.configModule
import com.yagubogu.di.datasourceModule
import com.yagubogu.di.localModule
import com.yagubogu.di.networkModule
import com.yagubogu.di.repositoryModule
import com.yagubogu.di.serviceModule
import com.yagubogu.di.timeModule
import com.yagubogu.di.useCaseModule
import com.yagubogu.di.viewModelModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

            modules(
                authModule,
                commonModule,
                datasourceModule,
                networkModule,
                repositoryModule,
                serviceModule,
                timeModule,
                useCaseModule,
                viewModelModule,
                localModule,
                configModule,
            )
        }
    }
}
