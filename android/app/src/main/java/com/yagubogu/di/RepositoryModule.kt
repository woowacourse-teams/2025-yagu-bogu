package com.yagubogu.di

import com.yagubogu.data.repository.auth.AuthDefaultRepository
import com.yagubogu.data.repository.auth.AuthRepository
import com.yagubogu.data.repository.checkin.CheckInDefaultRepository
import com.yagubogu.data.repository.checkin.CheckInRepository
import com.yagubogu.data.repository.game.GameDefaultRepository
import com.yagubogu.data.repository.game.GameRepository
import com.yagubogu.data.repository.location.LocationDefaultRepository
import com.yagubogu.data.repository.location.LocationRepository
import com.yagubogu.data.repository.member.MemberDefaultRepository
import com.yagubogu.data.repository.member.MemberRepository
import com.yagubogu.data.repository.stadium.StadiumDefaultRepository
import com.yagubogu.data.repository.stadium.StadiumRepository
import com.yagubogu.data.repository.stats.StatsDefaultRepository
import com.yagubogu.data.repository.stats.StatsRepository
import com.yagubogu.data.repository.stream.StreamDefaultRepository
import com.yagubogu.data.repository.stream.StreamRepository
import com.yagubogu.data.repository.talk.TalkDefaultRepository
import com.yagubogu.data.repository.talk.TalkRepository
import com.yagubogu.data.repository.thirdparty.ThirdPartyDefaultRepository
import com.yagubogu.data.repository.thirdparty.ThirdPartyRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthDefaultRepository): AuthRepository

    @Binds
    @Singleton
    abstract fun bindMemberRepository(impl: MemberDefaultRepository): MemberRepository

    @Binds
    @Singleton
    abstract fun bindCheckInRepository(impl: CheckInDefaultRepository): CheckInRepository

    @Binds
    @Singleton
    abstract fun bindStatsRepository(impl: StatsDefaultRepository): StatsRepository

    @Binds
    @Singleton
    abstract fun bindLocationRepository(impl: LocationDefaultRepository): LocationRepository

    @Binds
    @Singleton
    abstract fun bindStadiumRepository(impl: StadiumDefaultRepository): StadiumRepository

    @Binds
    @Singleton
    abstract fun bindStreamRepository(impl: StreamDefaultRepository): StreamRepository

    @Binds
    @Singleton
    abstract fun bindGameRepository(impl: GameDefaultRepository): GameRepository

    @Binds
    @Singleton
    abstract fun bindThirdPartyRepository(impl: ThirdPartyDefaultRepository): ThirdPartyRepository

    @Binds
    @Singleton
    abstract fun bindTalkRepository(impl: TalkDefaultRepository): TalkRepository
}
