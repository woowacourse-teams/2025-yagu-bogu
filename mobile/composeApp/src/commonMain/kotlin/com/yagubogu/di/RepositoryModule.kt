package com.yagubogu.di

import com.yagubogu.data.repository.appconfig.AppConfigDefaultRepository
import com.yagubogu.data.repository.appconfig.AppConfigRepository
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
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val repositoryModule =
    module {
        singleOf(::AuthDefaultRepository) { bind<AuthRepository>() }

        singleOf(::MemberDefaultRepository) { bind<MemberRepository>() }

        singleOf(::CheckInDefaultRepository) { bind<CheckInRepository>() }

        singleOf(::StatsDefaultRepository) { bind<StatsRepository>() }

        singleOf(::LocationDefaultRepository) { bind<LocationRepository>() }

        singleOf(::StadiumDefaultRepository) { bind<StadiumRepository>() }

        singleOf(::StreamDefaultRepository) { bind<StreamRepository>() }

        singleOf(::GameDefaultRepository) { bind<GameRepository>() }

        singleOf(::ThirdPartyDefaultRepository) { bind<ThirdPartyRepository>() }

        singleOf(::TalkDefaultRepository) { bind<TalkRepository>() }

        singleOf(::AuthDefaultRepository) { bind<AuthRepository>() }

        singleOf(::AppConfigDefaultRepository) { bind<AppConfigRepository>() }
    }
