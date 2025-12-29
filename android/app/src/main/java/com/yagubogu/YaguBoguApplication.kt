package com.yagubogu

import android.app.Application
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.yagubogu.common.YaguBoguDebugTree
import com.yagubogu.common.YaguBoguReleaseTree
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class YaguBoguApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            // 개발 환경
            FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = false
            FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(false)
            Timber.plant(YaguBoguDebugTree())
        } else {
            // 운영 환경
            Timber.plant(YaguBoguReleaseTree())
        }
    }
}
