package com.yagubogu.ui.common

import com.yagubogu.BuildKonfig

actual object AdUnitIds {
    actual val homeBanner: String = BuildKonfig.ADMOB_ANDROID_HOME_BANNER
    actual val rankingBanner: String = BuildKonfig.ADMOB_ANDROID_RANKING_BANNER
    actual val livetalkBanner: String = BuildKonfig.ADMOB_ANDROID_LIVETALK_BANNER
    actual val statsBanner: String = BuildKonfig.ADMOB_ANDROID_STATS_BANNER
    actual val attendanceCalendarBanner: String =
        BuildKonfig.ADMOB_ANDROID_ATTENDANCE_CALENDAR_BANNER
    actual val exitDialogBanner: String = BuildKonfig.ADMOB_ANDROID_EXIT_DIALOG_BANNER
    actual val profileDialogBanner: String = BuildKonfig.ADMOB_ANDROID_PROFILE_DIALOG_BANNER
    actual val pastCheckInInterstitial: String =
        BuildKonfig.ADMOB_ANDROID_PAST_CHECK_IN_INTERSTITIAL
}
