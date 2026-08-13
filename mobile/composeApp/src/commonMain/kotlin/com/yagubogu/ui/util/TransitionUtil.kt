package com.yagubogu.ui.util

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import androidx.navigationevent.NavigationEvent

private const val TRANSITION_FADE_DURATION_MILLISECOND = 700
private const val TRANSITION_SLIDE_DURATION_MILLISECOND = 500

fun fadeTransition(): ContentTransform =
    fadeIn(tween(TRANSITION_FADE_DURATION_MILLISECOND)) togetherWith
        fadeOut(tween(TRANSITION_FADE_DURATION_MILLISECOND))

fun AnimatedContentTransitionScope<Scene<NavKey>>.slidePushTransition(): ContentTransform =
    slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Left,
        animationSpec = tween(TRANSITION_SLIDE_DURATION_MILLISECOND),
    ) togetherWith
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Left,
            targetOffset = { it / 4 },
            animationSpec = tween(TRANSITION_SLIDE_DURATION_MILLISECOND),
        )

fun AnimatedContentTransitionScope<Scene<NavKey>>.slidePopTransition(): ContentTransform =
    slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Right,
        initialOffset = { it / 4 },
        animationSpec = tween(TRANSITION_SLIDE_DURATION_MILLISECOND),
    ) togetherWith
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Right,
            animationSpec = tween(TRANSITION_SLIDE_DURATION_MILLISECOND),
        )

fun AnimatedContentTransitionScope<Scene<NavKey>>.slidePredictivePopTransition(edge: Int): ContentTransform {
    val towards =
        if (edge == NavigationEvent.EDGE_LEFT) {
            AnimatedContentTransitionScope.SlideDirection.Right
        } else {
            AnimatedContentTransitionScope.SlideDirection.Left
        }
    return slideIntoContainer(
        towards = towards,
        initialOffset = { it / 4 },
        animationSpec = tween(TRANSITION_SLIDE_DURATION_MILLISECOND),
    ) togetherWith
        slideOutOfContainer(
            towards = towards,
            animationSpec = tween(TRANSITION_SLIDE_DURATION_MILLISECOND),
        )
}
