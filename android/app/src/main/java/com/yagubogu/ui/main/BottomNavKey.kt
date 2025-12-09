package com.yagubogu.ui.main

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.navigation3.runtime.NavKey
import com.yagubogu.R
import kotlinx.serialization.Serializable

sealed interface BottomNavKey : NavKey {
    @get:DrawableRes
    val icon: Int

    @get:StringRes
    val label: Int

    @Serializable
    data object Home : BottomNavKey {
        override val icon: Int = R.drawable.ic_home
        override val label: Int = R.string.bottom_navigation_home
    }

    @Serializable
    data object Livetalk : BottomNavKey {
        override val icon: Int = R.drawable.ic_livetalk
        override val label: Int = R.string.bottom_navigation_livetalk
    }

    @Serializable
    data object Stats : BottomNavKey {
        override val icon: Int = R.drawable.ic_stats
        override val label: Int = R.string.bottom_navigation_stats
    }

    @Serializable
    data object AttendanceHistory : BottomNavKey {
        override val icon: Int = R.drawable.ic_attendance_history
        override val label: Int = R.string.bottom_navigation_attendance_history
    }

    companion object {
        val items: List<BottomNavKey> = listOf(Home, Livetalk, Stats, AttendanceHistory)
    }
}
