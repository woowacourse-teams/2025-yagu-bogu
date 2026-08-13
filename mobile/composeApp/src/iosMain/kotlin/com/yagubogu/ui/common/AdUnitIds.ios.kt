package com.yagubogu.ui.common

import com.yagubogu.BuildKonfig

actual object AdUnitIds {
    actual val homeBanner: String = BuildKonfig.ADMOB_IOS_HOME_BANNER
    actual val rankingBanner: String = BuildKonfig.ADMOB_IOS_RANKING_BANNER
    actual val livetalkBanner: String = BuildKonfig.ADMOB_IOS_LIVETALK_BANNER
    actual val statsBanner: String = BuildKonfig.ADMOB_IOS_STATS_BANNER
    actual val attendanceCalendarBanner: String = BuildKonfig.ADMOB_IOS_ATTENDANCE_CALENDAR_BANNER
    actual val exitDialogBanner: String = ""
    actual val profileDialogBanner: String = BuildKonfig.ADMOB_IOS_PROFILE_DIALOG_BANNER
    actual val pastCheckInInterstitial: String = BuildKonfig.ADMOB_IOS_PAST_CHECK_IN_INTERSTITIAL
}
