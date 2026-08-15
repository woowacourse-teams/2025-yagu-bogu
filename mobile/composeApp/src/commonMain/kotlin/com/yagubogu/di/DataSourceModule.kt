package com.yagubogu.di

import com.yagubogu.data.datasource.appconfig.AppConfigDataStoreLocalDataSource
import com.yagubogu.data.datasource.appconfig.AppConfigFirebaseDataSource
import com.yagubogu.data.datasource.appconfig.AppConfigLocalDataSource
import com.yagubogu.data.datasource.appconfig.AppConfigRemoteDataSource
import com.yagubogu.data.datasource.auth.AuthDataSource
import com.yagubogu.data.datasource.auth.AuthRemoteDataSource
import com.yagubogu.data.datasource.checkin.CheckInDataSource
import com.yagubogu.data.datasource.checkin.CheckInRemoteDataSource
import com.yagubogu.data.datasource.game.GameDataSource
import com.yagubogu.data.datasource.game.GameRemoteDataSource
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
import com.yagubogu.data.datasource.widget.WidgetDeviceDataSource
import com.yagubogu.data.datasource.widget.WidgetDeviceRemoteDataSource
import com.yagubogu.data.datasource.widget.WidgetSettingsDataSource
import com.yagubogu.data.datasource.widget.WidgetSettingsRemoteDataSource
import com.yagubogu.data.local.APP_CONFIG_PREFS
import com.yagubogu.data.local.ScoreWidgetDeviceId
import com.yagubogu.data.local.ScoreWidgetSettings
import com.yagubogu.data.local.WIDGET_PREFS
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

expect fun Module.registerPlatformDataSources()

val datasourceModule =
    module {
        singleOf(::AuthRemoteDataSource) { bind<AuthDataSource>() }

        singleOf(::MemberRemoteDataSource) { bind<MemberDataSource>() }

        singleOf(::CheckInRemoteDataSource) { bind<CheckInDataSource>() }

        singleOf(::StatsRemoteDataSource) { bind<StatsDataSource>() }

        singleOf(::StadiumRemoteDataSource) { bind<StadiumDataSource>() }

        singleOf(::StreamRemoteDataSource) { bind<StreamDataSource>() }

        singleOf(::GameRemoteDataSource) { bind<GameDataSource>() }

        singleOf(::TalkRemoteDataSource) { bind<TalkDataSource>() }

        singleOf(::WidgetSettingsRemoteDataSource) { bind<WidgetSettingsDataSource>() }

        singleOf(::WidgetDeviceRemoteDataSource) { bind<WidgetDeviceDataSource>() }

        singleOf(::AppConfigFirebaseDataSource) { bind<AppConfigRemoteDataSource>() }

        single<AppConfigLocalDataSource> {
            AppConfigDataStoreLocalDataSource(dataStore = get(named(APP_CONFIG_PREFS)))
        }

        single { ScoreWidgetSettings(dataStore = get(named(WIDGET_PREFS))) }

        single { ScoreWidgetDeviceId(dataStore = get(named(WIDGET_PREFS))) }

        registerPlatformDataSources()
    }
