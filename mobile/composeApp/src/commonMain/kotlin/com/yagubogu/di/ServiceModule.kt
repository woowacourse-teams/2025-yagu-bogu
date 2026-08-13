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
import de.jensklingenberg.ktorfit.Ktorfit
import org.koin.dsl.module

val serviceModule =
    module {
        single<AuthApiService> { get<Ktorfit>().createAuthApiService() }

        single<CheckInApiService> { get<Ktorfit>().createCheckInApiService() }

        single<GameApiService> { get<Ktorfit>().createGameApiService() }

        single<MemberApiService> { get<Ktorfit>().createMemberApiService() }

        single<StadiumApiService> { get<Ktorfit>().createStadiumApiService() }

        single<StatsApiService> { get<Ktorfit>().createStatsApiService() }

        single<TalkApiService> { get<Ktorfit>().createTalkApiService() }

        single<ThirdPartyApiService> { get<Ktorfit>().createThirdPartyApiService() }
    }
