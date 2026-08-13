package com.yagubogu.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.yagubogu.data.local.APP_CONFIG_PREFS
import com.yagubogu.data.local.AUTH_PREFS
import com.yagubogu.data.local.createDataStore
import org.koin.core.qualifier.named
import org.koin.dsl.module

actual val localModule =
    module {
        single<DataStore<Preferences>>(named(AUTH_PREFS)) {
            createDataStore(fileName = AUTH_PREFS)
        }
        single<DataStore<Preferences>>(named(APP_CONFIG_PREFS)) {
            createDataStore(fileName = APP_CONFIG_PREFS)
        }
    }
