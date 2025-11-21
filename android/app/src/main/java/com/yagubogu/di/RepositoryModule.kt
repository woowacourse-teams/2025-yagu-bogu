package com.yagubogu.di

import com.yagubogu.data.repository.AuthDefaultRepository
import com.yagubogu.data.repository.CheckInDefaultRepository
import com.yagubogu.data.repository.GameDefaultRepository
import com.yagubogu.data.repository.LocationDefaultRepository
import com.yagubogu.data.repository.MemberDefaultRepository
import com.yagubogu.data.repository.StadiumDefaultRepository
import com.yagubogu.data.repository.StatsDefaultRepository
import com.yagubogu.data.repository.StreamDefaultRepository
import com.yagubogu.data.repository.TalkDefaultRepository
import com.yagubogu.data.repository.ThirdPartyDefaultRepository
import com.yagubogu.data.repository.TokenDefaultRepository
import com.yagubogu.domain.repository.AuthRepository
import com.yagubogu.domain.repository.CheckInRepository
import com.yagubogu.domain.repository.GameRepository
import com.yagubogu.domain.repository.LocationRepository
import com.yagubogu.domain.repository.MemberRepository
import com.yagubogu.domain.repository.StadiumRepository
import com.yagubogu.domain.repository.StatsRepository
import com.yagubogu.domain.repository.StreamRepository
import com.yagubogu.domain.repository.TalkRepository
import com.yagubogu.domain.repository.ThirdPartyRepository
import com.yagubogu.domain.repository.TokenRepository
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
    abstract fun bindTokenRepository(impl: TokenDefaultRepository): TokenRepository

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
