package com.yagubogu.ui.navigation.model

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.bottom_navigation_attendance_history
import yagubogu.composeapp.generated.resources.bottom_navigation_home
import yagubogu.composeapp.generated.resources.bottom_navigation_livetalk
import yagubogu.composeapp.generated.resources.bottom_navigation_stats
import yagubogu.composeapp.generated.resources.ic_attendance_history
import yagubogu.composeapp.generated.resources.ic_home
import yagubogu.composeapp.generated.resources.ic_livetalk
import yagubogu.composeapp.generated.resources.ic_stats

@Serializable
sealed interface BottomNavKey : NavKey {
    val icon: DrawableResource

    val label: StringResource

    @Serializable
    data object Home : BottomNavKey {
        override val icon: DrawableResource = Res.drawable.ic_home
        override val label: StringResource = Res.string.bottom_navigation_home
    }

    @Serializable
    data object Livetalk : BottomNavKey {
        override val icon: DrawableResource = Res.drawable.ic_livetalk
        override val label: StringResource = Res.string.bottom_navigation_livetalk
    }

    @Serializable
    data object AttendanceHistory : BottomNavKey {
        override val icon: DrawableResource = Res.drawable.ic_attendance_history
        override val label: StringResource = Res.string.bottom_navigation_attendance_history
    }

    @Serializable
    data object Stats : BottomNavKey {
        override val icon: DrawableResource = Res.drawable.ic_stats
        override val label: StringResource = Res.string.bottom_navigation_stats
    }

    companion object {
        val items: List<BottomNavKey> = listOf(Home, Livetalk, AttendanceHistory, Stats)
    }
}
