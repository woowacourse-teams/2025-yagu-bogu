package com.yagubogu.di

import com.yagubogu.data.local.AUTH_PREFS
import com.yagubogu.data.network.TokenManager
import org.koin.core.qualifier.named
import org.koin.dsl.module

actual val commonModule =
    module {
        single<TokenManager> { TokenManager(dataStore = get(named(AUTH_PREFS))) }
    }
