package com.yagubogu.di

import com.yagubogu.data.datasource.location.IosLocationDataSource
import com.yagubogu.data.datasource.location.LocationDataSource
import com.yagubogu.data.datasource.thirdparty.IosThirdPartyRemoteDataSource
import com.yagubogu.data.datasource.thirdparty.ThirdPartyDataSource
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf

actual fun Module.registerPlatformDataSources() {
    singleOf(::IosLocationDataSource) { bind<LocationDataSource>() }

    singleOf(::IosThirdPartyRemoteDataSource) { bind<ThirdPartyDataSource>() }
}
