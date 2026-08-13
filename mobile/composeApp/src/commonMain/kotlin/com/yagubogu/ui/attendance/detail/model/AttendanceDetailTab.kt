package com.yagubogu.ui.attendance.detail.model

import org.jetbrains.compose.resources.StringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.attendance_detail_tab_diary
import yagubogu.composeapp.generated.resources.attendance_detail_tab_game_record

enum class AttendanceDetailTab(
    val titleRes: StringResource,
) {
    GAME_RECORD(Res.string.attendance_detail_tab_game_record),
    DIARY(Res.string.attendance_detail_tab_diary),
}
