package com.yagubogu.di

import com.yagubogu.data.datasource.location.AndroidLocationLocalDataSource
import com.yagubogu.data.datasource.location.LocationDataSource
import com.yagubogu.data.datasource.thirdparty.AndroidThirdPartyRemoteDataSource
import com.yagubogu.data.datasource.thirdparty.ThirdPartyDataSource
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf

actual fun Module.registerPlatformDataSources() {
    singleOf(::AndroidLocationLocalDataSource) { bind<LocationDataSource>() }

    singleOf(::AndroidThirdPartyRemoteDataSource) { bind<ThirdPartyDataSource>() }
}
