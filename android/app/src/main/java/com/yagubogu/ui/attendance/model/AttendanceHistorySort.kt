package com.yagubogu.ui.attendance.model

enum class AttendanceHistorySort {
    LATEST {
        override fun toggle(): AttendanceHistorySort = OLDEST
    },
    OLDEST {
        override fun toggle(): AttendanceHistorySort = LATEST
    }, ;

    abstract fun toggle(): AttendanceHistorySort
}
