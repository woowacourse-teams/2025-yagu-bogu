package com.yagubogu.di

import android.content.ContentResolver
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.yagubogu.analytics.Analytics
import com.yagubogu.analytics.FirebaseAnalyticsLogger
import com.yagubogu.data.local.AUTH_PREFS
import com.yagubogu.data.network.TokenManager
import org.koin.android.ext.koin.androidApplication
import org.koin.core.qualifier.named
import org.koin.dsl.module

actual val commonModule =
    module {
        single<TokenManager> { TokenManager(dataStore = get(named(AUTH_PREFS))) }

        single<ContentResolver> { androidApplication().contentResolver }

        single<FusedLocationProviderClient> {
            LocationServices.getFusedLocationProviderClient(androidApplication())
        }

        single<Analytics> { FirebaseAnalyticsLogger() }
    }
