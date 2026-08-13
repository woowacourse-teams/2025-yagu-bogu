package com.yagubogu.ui.common.component

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow

/**
 * [triggerFlow]에서 이벤트를 수신할 때마다 전면 광고를 표시하는 사이드 이펙트 컴포저블.
 *
 * @param triggerFlow 광고 표시를 트리거하는 Flow. emit될 때마다 광고 1회 노출을 시도한다.
 * @param adUnitId AdMob 광고 단위 ID.
 * @param onAdComplete 광고가 닫혔을 때 호출되는 콜백. 광고가 미로드 상태로 스킵된 경우에도 호출된다.
 */
@Composable
expect fun InterstitialAdEffect(
    triggerFlow: Flow<Unit>,
    adUnitId: String,
    onAdComplete: () -> Unit,
)
