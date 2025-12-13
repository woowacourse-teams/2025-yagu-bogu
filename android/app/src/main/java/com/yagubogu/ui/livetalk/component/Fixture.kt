package com.yagubogu.ui.livetalk.component

import com.yagubogu.domain.model.Team
import com.yagubogu.presentation.livetalk.stadium.LivetalkStadiumItem

val LIVETALK_STADIUM_ITEM_VERIFIED =
    LivetalkStadiumItem(
        gameId = 0L,
        stadiumName = "대전 한화생명 볼파크",
        userCount = 100,
        awayTeam = Team.SS,
        homeTeam = Team.HH,
        isVerified = true,
    )

val LIVETALK_STADIUM_ITEM_UNVERIFIED =
    LivetalkStadiumItem(
        gameId = 1L,
        stadiumName = "창원 NC 파크",
        userCount = 10,
        awayTeam = Team.HT,
        homeTeam = Team.NC,
        isVerified = false,
    )

val LIVETALK_STADIUM_ITEMS =
    listOf(
        LIVETALK_STADIUM_ITEM_VERIFIED,
        LIVETALK_STADIUM_ITEM_UNVERIFIED,
        LIVETALK_STADIUM_ITEM_UNVERIFIED,
        LIVETALK_STADIUM_ITEM_UNVERIFIED,
    )
