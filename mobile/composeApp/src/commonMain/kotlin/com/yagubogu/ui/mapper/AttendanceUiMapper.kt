package com.yagubogu.ui.mapper

import com.yagubogu.domain.model.PastCheckInAdState
import com.yagubogu.ui.attendance.model.PastCheckInAdInfo

fun PastCheckInAdInfo.toDomain(): PastCheckInAdState =
    PastCheckInAdState(
        isEnabled = isEnabled,
        startCount = startCount,
        interval = interval,
    )
