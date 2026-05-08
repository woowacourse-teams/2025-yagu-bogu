package com.yagubogu.ui.common.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.Flow

@Composable
actual fun InterstitialAdEffect(
    triggerFlow: Flow<Unit>,
    adUnitId: String,
    onAdComplete: () -> Unit,
) {
    // 컴포저블 진입 시 미리 로드해 표시 딜레이 방지 (Android LaunchedEffect(adUnitId)와 동일)
    LaunchedEffect(adUnitId) {
        InterstitialAdProvider.preload?.invoke(adUnitId)
    }

    LaunchedEffect(triggerFlow) {
        triggerFlow.collect {
            val showFn = InterstitialAdProvider.show
            if (showFn != null) {
                showFn(adUnitId, onAdComplete)
            } else {
                // 브릿지 미등록 시 광고는 스킵하되 완료 콜백은 그대로 호출
                onAdComplete()
            }
        }
    }
}
