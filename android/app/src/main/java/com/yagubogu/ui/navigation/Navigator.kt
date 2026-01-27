package com.yagubogu.ui.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import timber.log.Timber

/**
 * Handles navigation events (forward and back) by updating the navigation state.
 *
 * https://developer.android.com/guide/navigation/navigation-3/migration-guide
 */
class Navigator(
    val state: NavigationState,
) {
    private val currentStack: NavBackStack<NavKey>
        get() =
            state.backStacks[state.topLevelRoute]
                ?: error("Stack for ${state.topLevelRoute} not found")
    val currentRoute: NavKey
        get() = currentStack.last()

    fun navigate(route: NavKey) {
        if (route in state.backStacks.keys) {
            // This is a top level route, just switch to it.
            state.topLevelRoute = route
        } else {
            currentStack.add(route)
        }
        showBackStack()
    }

    fun canGoBack(): Boolean = currentRoute != state.topLevelRoute

    fun goBack() {
        // If we're at the base of the current route, go back to the start route stack.
        if (!canGoBack()) {
            state.topLevelRoute = state.startRoute
        } else {
            currentStack.removeLastOrNull()
        }
        showBackStack()
    }

    fun clearStack() {
        while (canGoBack()) {
            goBack()
        }
    }

    private fun showBackStack() {
        Timber.d("backStacks: ${state.backStacks.keys}")
        Timber.d("currentStack: ${currentStack.joinToString()}")
    }
}
