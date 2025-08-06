package com.yagubogu.common

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

class YaguBoguReleaseTree : Timber.Tree() {
    override fun log(
        priority: Int,
        tag: String?,
        message: String,
        t: Throwable?,
    ) {
        // DEBUG 하위 레벨 로그는 무시
        if (priority <= Log.DEBUG) return

        // Crashlytics 로그 기록
        FirebaseCrashlytics.getInstance().log("$tag: $message")

        // 예외가 있으면 Crashlytics에 전송
        t?.let { FirebaseCrashlytics.getInstance().recordException(it) }
    }
}
