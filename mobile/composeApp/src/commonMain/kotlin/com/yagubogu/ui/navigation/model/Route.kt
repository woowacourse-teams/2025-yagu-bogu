package com.yagubogu.ui.navigation.model

import androidx.navigation3.runtime.NavKey
import com.yagubogu.ui.attendance.model.AttendanceHistoryItem
import com.yagubogu.ui.ranking.model.RankingType
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey {
    @Serializable
    data object Main : Route

    @Serializable
    data object Setting : Route

    @Serializable
    data object Login : Route

    @Serializable
    data class FavoriteTeam(
        val isOnboarding: Boolean,
    ) : Route

    @Serializable
    data class NicknameSetup(
        val teamName: String,
    ) : Route

    @Serializable
    data object Badge : Route

    @Serializable
    data class Ranking(
        val type: RankingType,
    ) : Route

    @Serializable
    data class LivetalkChat(
        val gameId: Long,
        val isVerified: Boolean,
    ) : Route

    @Serializable
    data class AttendanceHistoryDetail(
        val attendanceItem: AttendanceHistoryItem,
    ) : Route
}
