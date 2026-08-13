package com.yagubogu.ui.navigation.model

import androidx.navigation3.runtime.NavKey
import co.touchlab.kermit.Logger

/**
 * Handles navigation events (forward and back) by updating the navigation state.
 *
 * https://developer.android.com/guide/navigation/navigation-3/migration-guide
 */
class Navigator(
    val state: NavigationState,
) {
    private val logger = Logger.withTag("Navigator")

    fun navigate(route: NavKey) {
        if (route in state.topLevelRoutes) {
            // This is a top level route, just switch to it.
            state.topLevelRoute = route
        } else {
            state.currentStack.add(route)
        }
        showBackStack()
    }

    fun canGoBack(): Boolean = state.currentRoute != state.topLevelRoute

    fun goBack() {
        if (canGoBack()) {
            state.currentStack.removeLastOrNull()
        }
        showBackStack()
    }

    fun clearStack() {
        while (canGoBack()) {
            goBack()
        }
    }

    private fun showBackStack() {
        logger.d { "topLevelRoutes: ${state.topLevelRoutes}" }
        logger.d { "currentStack: ${state.currentStack.joinToString()}" }
    }
}
