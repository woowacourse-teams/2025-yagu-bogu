package com.yagubogu.ui.util

import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember

/**
 * LazyList가 바닥에 도달했는지 여부를 반환하는 확장 함수
 * @param buffer 마지막 아이템이 보이기 몇 개 전부터 바닥으로 간주할지 설정 (기본값: 2)
 */
@Composable
fun LazyListState.isAtBottom(buffer: Int = 2): Boolean =
    remember(this) {
        // derivedStateOf를 사용하여 불필요한 리컴포지션 방지
        derivedStateOf {
            val layoutInfo: LazyListLayoutInfo = layoutInfo
            val totalItemsCount: Int = layoutInfo.totalItemsCount
            val lastVisibleItemIndex: Int = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

            // 데이터가 없으면 false
            if (totalItemsCount == 0) {
                false
            } else {
                // (마지막으로 보이는 인덱스 + 버퍼)가 전체 개수보다 크거나 같으면 바닥에 도달한 것으로 간주
                lastVisibleItemIndex + buffer >= totalItemsCount
            }
        }
    }.value
