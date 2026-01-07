package com.yagubogu.di

import com.yagubogu.data.service.AuthApiService
import com.yagubogu.data.service.CheckInApiService
import com.yagubogu.data.service.GameApiService
import com.yagubogu.data.service.MemberApiService
import com.yagubogu.data.service.StadiumApiService
import com.yagubogu.data.service.StatsApiService
import com.yagubogu.data.service.TalkApiService
import com.yagubogu.data.service.ThirdPartyApiService
import com.yagubogu.data.service.createAuthApiService
import com.yagubogu.data.service.createCheckInApiService
import com.yagubogu.data.service.createGameApiService
import com.yagubogu.data.service.createMemberApiService
import com.yagubogu.data.service.createStadiumApiService
import com.yagubogu.data.service.createStatsApiService
import com.yagubogu.data.service.createTalkApiService
import com.yagubogu.data.service.createThirdPartyApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.jensklingenberg.ktorfit.Ktorfit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {
    @Provides
    @Singleton
    fun provideAuthApiService(ktorfit: Ktorfit): AuthApiService = ktorfit.createAuthApiService()

    @Provides
    @Singleton
    fun provideThirdPartyApiService(ktorfit: Ktorfit): ThirdPartyApiService = ktorfit.createThirdPartyApiService()

    @Provides
    @Singleton
    fun provideMemberApiService(ktorfit: Ktorfit): MemberApiService = ktorfit.createMemberApiService()

    @Provides
    @Singleton
    fun provideStadiumApiService(ktorfit: Ktorfit): StadiumApiService = ktorfit.createStadiumApiService()

    @Provides
    @Singleton
    fun provideCheckInApiService(ktorfit: Ktorfit): CheckInApiService = ktorfit.createCheckInApiService()

    @Provides
    @Singleton
    fun provideStatsApiService(ktorfit: Ktorfit): StatsApiService = ktorfit.createStatsApiService()

    @Provides
    @Singleton
    fun provideGameApiService(ktorfit: Ktorfit): GameApiService = ktorfit.createGameApiService()

    @Provides
    @Singleton
    fun provideTalkApiService(ktorfit: Ktorfit): TalkApiService = ktorfit.createTalkApiService()
}
