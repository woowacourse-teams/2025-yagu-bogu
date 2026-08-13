package com.yagubogu.di

import com.yagubogu.ui.attendance.AttendanceHistoryViewModel
import com.yagubogu.ui.attendance.detail.AttendanceDetailViewModel
import com.yagubogu.ui.badge.BadgeViewModel
import com.yagubogu.ui.home.HomeViewModel
import com.yagubogu.ui.livetalk.LivetalkViewModel
import com.yagubogu.ui.livetalk.chat.LivetalkChatViewModel
import com.yagubogu.ui.login.LoginViewModel
import com.yagubogu.ui.main.MainViewModel
import com.yagubogu.ui.main.YaguBoguViewModel
import com.yagubogu.ui.onboarding.favorite.FavoriteTeamViewModel
import com.yagubogu.ui.onboarding.nickname.NicknameViewModel
import com.yagubogu.ui.ranking.RankingViewModel
import com.yagubogu.ui.ranking.model.RankingType
import com.yagubogu.ui.setting.SettingViewModel
import com.yagubogu.ui.stats.StatsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule =
    module {
        viewModelOf(::YaguBoguViewModel)

        viewModelOf(::AttendanceHistoryViewModel)

        viewModelOf(::BadgeViewModel)

        viewModel { params ->
            FavoriteTeamViewModel(
                memberRepository = get(),
                isOnboarding = params.get(),
            )
        }

        viewModel { params ->
            NicknameViewModel(
                memberRepository = get(),
                teamName = params.get(),
            )
        }

        viewModelOf(::HomeViewModel)

        viewModelOf(::LivetalkViewModel)

        viewModel { (gameId: Long) ->
            LivetalkChatViewModel(
                gameId = gameId,
                talkRepository = get(),
                gameRepository = get(),
                memberRepository = get(),
                clock = get(),
            )
        }

        viewModelOf(::LoginViewModel)

        viewModelOf(::MainViewModel)

        viewModel { (type: RankingType) ->
            RankingViewModel(
                type = type,
                statsRepository = get(),
                memberRepository = get(),
                clock = get(),
            )
        }

        viewModelOf(::SettingViewModel)

        viewModelOf(::StatsViewModel)

        viewModel { (checkInId: Long) ->
            AttendanceDetailViewModel(
                checkInId = checkInId,
                checkInRepository = get(),
                thirdPartyRepository = get(),
                loadDiaryUseCase = get(),
                deleteCheckInUseCase = get(),
            )
        }
    }
