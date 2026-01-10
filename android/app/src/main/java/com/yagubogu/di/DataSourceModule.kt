package com.yagubogu.di

import com.yagubogu.data.datasource.auth.AuthDataSource
import com.yagubogu.data.datasource.auth.AuthRemoteDataSource
import com.yagubogu.data.datasource.checkin.CheckInDataSource
import com.yagubogu.data.datasource.checkin.CheckInRemoteDataSource
import com.yagubogu.data.datasource.game.GameDataSource
import com.yagubogu.data.datasource.game.GameRemoteDataSource
import com.yagubogu.data.datasource.location.LocationDataSource
import com.yagubogu.data.datasource.location.LocationLocalDataSource
import com.yagubogu.data.datasource.member.MemberDataSource
import com.yagubogu.data.datasource.member.MemberRemoteDataSource
import com.yagubogu.data.datasource.stadium.StadiumDataSource
import com.yagubogu.data.datasource.stadium.StadiumRemoteDataSource
import com.yagubogu.data.datasource.stats.StatsDataSource
import com.yagubogu.data.datasource.stats.StatsRemoteDataSource
import com.yagubogu.data.datasource.stream.StreamDataSource
import com.yagubogu.data.datasource.stream.StreamRemoteDataSource
import com.yagubogu.data.datasource.talk.TalkDataSource
import com.yagubogu.data.datasource.talk.TalkRemoteDataSource
import com.yagubogu.data.datasource.thirdparty.ThirdPartyDataSource
import com.yagubogu.data.datasource.thirdparty.ThirdPartyRemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {
    @Binds
    @Singleton
    abstract fun bindAuthDataSource(impl: AuthRemoteDataSource): AuthDataSource

    @Binds
    @Singleton
    abstract fun bindMemberDataSource(impl: MemberRemoteDataSource): MemberDataSource

    @Binds
    @Singleton
    abstract fun bindCheckInDataSource(impl: CheckInRemoteDataSource): CheckInDataSource

    @Binds
    @Singleton
    abstract fun bindStatsDataSource(impl: StatsRemoteDataSource): StatsDataSource

    @Binds
    @Singleton
    abstract fun bindLocationDataSource(impl: LocationLocalDataSource): LocationDataSource

    @Binds
    @Singleton
    abstract fun bindStadiumDataSource(impl: StadiumRemoteDataSource): StadiumDataSource

    @Binds
    @Singleton
    abstract fun bindStreamDataSource(impl: StreamRemoteDataSource): StreamDataSource

    @Binds
    @Singleton
    abstract fun bindGameDataSource(impl: GameRemoteDataSource): GameDataSource

    @Binds
    @Singleton
    abstract fun bindThirdPartyDataSource(impl: ThirdPartyRemoteDataSource): ThirdPartyDataSource

    @Binds
    @Singleton
    abstract fun bindTalkDataSource(impl: TalkRemoteDataSource): TalkDataSource
}
