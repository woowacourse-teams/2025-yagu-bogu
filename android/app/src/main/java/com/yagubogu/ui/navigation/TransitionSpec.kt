package com.yagubogu.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.navigation3.ui.NavDisplay

val slideInTransition: Map<String, Any> =
    NavDisplay.transitionSpec {
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = tween(200),
        ) togetherWith ExitTransition.KeepUntilTransitionsFinished
    }

val slideOutTransition: Map<String, Any> =
    NavDisplay.popTransitionSpec {
        EnterTransition.None togetherWith
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300),
            )
    }
