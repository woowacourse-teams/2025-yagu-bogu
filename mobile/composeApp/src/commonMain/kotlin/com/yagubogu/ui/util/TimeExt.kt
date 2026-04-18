package com.yagubogu.ui.util

import kotlinx.datetime.DayOfWeek
import org.jetbrains.compose.resources.StringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.day_friday
import yagubogu.composeapp.generated.resources.day_monday
import yagubogu.composeapp.generated.resources.day_saturday
import yagubogu.composeapp.generated.resources.day_sunday
import yagubogu.composeapp.generated.resources.day_thursday
import yagubogu.composeapp.generated.resources.day_tuesday
import yagubogu.composeapp.generated.resources.day_wednesday

fun DayOfWeek.getDisplayNameResId(): StringResource =
    when (this) {
        DayOfWeek.MONDAY -> Res.string.day_monday
        DayOfWeek.TUESDAY -> Res.string.day_tuesday
        DayOfWeek.WEDNESDAY -> Res.string.day_wednesday
        DayOfWeek.THURSDAY -> Res.string.day_thursday
        DayOfWeek.FRIDAY -> Res.string.day_friday
        DayOfWeek.SATURDAY -> Res.string.day_saturday
        DayOfWeek.SUNDAY -> Res.string.day_sunday
    }
