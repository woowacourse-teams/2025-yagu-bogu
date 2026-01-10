package com.yagubogu.ui.attendance.model

enum class AttendanceHistoryViewType {
    CALENDAR {
        override fun toggle(): AttendanceHistoryViewType = LIST
    },
    LIST {
        override fun toggle(): AttendanceHistoryViewType = CALENDAR
    },
    ;

    abstract fun toggle(): AttendanceHistoryViewType
}
