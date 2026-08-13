package com.yagubogu.ui.util

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

val LocalSnackbarHostState =
    staticCompositionLocalOf<SnackbarHostState> {
        error("SnackbarHostState 없음")
    }
val LocalSnackbarScope =
    staticCompositionLocalOf<CoroutineScope> {
        error("SnackbarScope 없음")
    }

fun SnackbarHostState.showSingleSnackbar(
    scope: CoroutineScope,
    message: String,
    actionLabel: String? = null,
    duration: SnackbarDuration = SnackbarDuration.Short,
) {
    scope.launch {
        currentSnackbarData?.dismiss()
        showSnackbar(
            message = message,
            actionLabel = actionLabel,
            duration = duration,
        )
    }
}

fun SnackbarHostState.showSingleSnackbar(
    scope: CoroutineScope,
    stringResource: StringResource,
    actionLabel: String? = null,
    duration: SnackbarDuration = SnackbarDuration.Short,
) {
    scope.launch {
        currentSnackbarData?.dismiss()
        showSnackbar(
            message = getString(stringResource),
            actionLabel = actionLabel,
            duration = duration,
        )
    }
}

private object SnackBarConfig {
    val BottomNavBarHeight = 80.dp
    val StandardPadding = 10.dp
}

@Composable
fun snackbarPadding(isMainScreen: Boolean): Dp {
    // 시스템 내비게이션 바(3버튼 혹은 제스처 바)의 높이
    val systemNavigationPadding =
        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    // 스낵바 하단 패딩 계산: 시스템 바 높이 + (메인 화면일 경우 내비바 높이) + 기본 여백
    if (isMainScreen) {
        return systemNavigationPadding + SnackBarConfig.BottomNavBarHeight + SnackBarConfig.StandardPadding
    }
    return systemNavigationPadding + SnackBarConfig.StandardPadding
}
