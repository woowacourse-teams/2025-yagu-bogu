package com.yagubogu.ui.common.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yagubogu.ui.theme.Black
import com.yagubogu.ui.theme.Gray300
import com.yagubogu.ui.theme.PretendardMedium
import com.yagubogu.ui.theme.Primary050

@Composable
fun <T> Picker(
    items: List<T>,
    onValueChange: (T) -> Unit,
    modifier: Modifier = Modifier,
    startIndex: Int = 0,
    visibleCount: Int = 3,
    itemHeight: Dp = 36.dp,
    selectedItemBackgroundColor: Color = Color.Transparent,
    selectedItemBackgroundShape: Shape = RoundedCornerShape(12.dp),
    textAlign: TextAlign = TextAlign.Center,
    label: (T) -> String = { it.toString() },
) {
    require(visibleCount % 2 == 1) { "visibleCount must be odd, but was $visibleCount" }

    val listState: LazyListState = rememberLazyListState(initialFirstVisibleItemIndex = startIndex)
    val flingBehavior: FlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val currentScrollIndex: Int by remember { derivedStateOf { listState.firstVisibleItemIndex } }

    // 보여질 아이템의 앞뒤 여백 개수 (예: 3개 보이면 위아래 1개씩)
    val centerIndexOffset: Int = (visibleCount - 1) / 2

    // 스크롤이 멈췄을 때 값 반환
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val selectedItem: T? = items.getOrNull(listState.firstVisibleItemIndex)
            if (selectedItem != null) {
                onValueChange(selectedItem)
            }
        }
    }

    Box(
        modifier = modifier.height(itemHeight * visibleCount),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .height(itemHeight)
                    .background(
                        color = selectedItemBackgroundColor,
                        shape = selectedItemBackgroundShape,
                    ),
        )

        LazyColumn(
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally,
            flingBehavior = flingBehavior,
        ) {
            items(centerIndexOffset) {
                Spacer(modifier = Modifier.height(itemHeight))
            }

            itemsIndexed(items) { index: Int, item: T ->
                val isSelected: Boolean = index == currentScrollIndex
                Text(
                    text = label(item),
                    style = PretendardMedium.copy(fontSize = 14.sp),
                    color = if (isSelected) Black else Gray300,
                    modifier =
                        Modifier
                            .height(itemHeight)
                            .fillMaxWidth()
                            .wrapContentHeight(),
                    textAlign = textAlign,
                )
            }

            items(centerIndexOffset) {
                Spacer(modifier = Modifier.height(itemHeight))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PickerPreview() {
    Picker(
        items = (1..31).toList(),
        onValueChange = { },
        selectedItemBackgroundColor = Primary050,
    )
}
