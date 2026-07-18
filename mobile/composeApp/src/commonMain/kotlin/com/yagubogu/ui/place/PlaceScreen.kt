package com.yagubogu.ui.place

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yagubogu.ui.place.component.PlaceFilterChips
import com.yagubogu.ui.place.component.PlaceRecommendationCard
import com.yagubogu.ui.place.component.PlaceStadiumSelector
import com.yagubogu.ui.place.model.PlaceCategory
import com.yagubogu.ui.place.model.PlaceItem
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.Gray900
import com.yagubogu.ui.theme.PretendardBold20
import com.yagubogu.ui.theme.PretendardRegular16
import com.yagubogu.ui.theme.PretendardSemiBold16
import com.yagubogu.ui.util.BackPressHandler
import com.yagubogu.ui.util.rememberNoRippleInteractionSource
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.place_recommendation_title
import yagubogu.composeapp.generated.resources.place_sort_distance
import yagubogu.composeapp.generated.resources.place_sort_popular

@Composable
fun PlaceScreen(
    scrollToTopEvent: SharedFlow<Unit>,
    modifier: Modifier = Modifier,
) {
    val lazyListState: LazyListState = rememberLazyListState()
    var selectedCategory: PlaceCategory by rememberSaveable { mutableStateOf(PlaceCategory.STAY) }
    var selectedSort: PlaceSort by rememberSaveable { mutableStateOf(PlaceSort.DISTANCE) }

    LaunchedEffect(Unit) {
        scrollToTopEvent.collect {
            lazyListState.animateScrollToItem(0)
        }
    }

    BackPressHandler()

    LazyColumn(
        state = lazyListState,
        contentPadding = PaddingValues(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 20.dp),
        modifier =
            modifier
                .fillMaxSize()
                .background(Gray050),
    ) {
        item {
            PlaceStadiumSelector(
                stadiumName = "잠실 야구장",
                onClick = {},
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            PlaceFilterChips(
                selectedCategory = selectedCategory,
                onCategoryClick = { selectedCategory = it },
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        item {
            PlaceSectionHeader(
                selectedSort = selectedSort,
                onSortClick = { selectedSort = it },
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        items(
            items = PLACE_ITEMS,
            key = { item: PlaceItem -> item.name },
        ) { item: PlaceItem ->
            PlaceRecommendationCard(
                item = item,
                onClick = {},
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun PlaceSectionHeader(
    selectedSort: PlaceSort,
    onSortClick: (PlaceSort) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(Res.string.place_recommendation_title),
            style = PretendardBold20,
            color = Gray900,
            modifier = Modifier.weight(1f),
        )

        PlaceSort.entries.forEachIndexed { index: Int, sort: PlaceSort ->
            if (index > 0) {
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = stringResource(sort.labelResource),
                style = if (selectedSort == sort) PretendardSemiBold16 else PretendardRegular16,
                color = Gray500,
                modifier =
                    Modifier.clickable(
                        interactionSource = rememberNoRippleInteractionSource(),
                        indication = null,
                        onClick = { onSortClick(sort) },
                    ),
            )
        }
    }
}

private enum class PlaceSort {
    DISTANCE,
    POPULAR,
}

private val PlaceSort.labelResource: StringResource
    get() =
        when (this) {
            PlaceSort.DISTANCE -> Res.string.place_sort_distance
            PlaceSort.POPULAR -> Res.string.place_sort_popular
        }

private val PLACE_ITEMS: List<PlaceItem> =
    listOf(
        PlaceItem(
            category = PlaceCategory.FOOD,
            name = "잠실 돈까스 본점",
            distance = "도보 320m",
            rating = "4.8",
            reviewCount = 245,
            thumbnailLabel = "돈까스",
            thumbnailColor = Color(0xFFE8A444),
        ),
        PlaceItem(
            category = PlaceCategory.FOOD,
            name = "승리 떡볶이",
            distance = "도보 450m",
            rating = "4.6",
            reviewCount = 182,
            thumbnailLabel = "분식",
            thumbnailColor = Color(0xFFE85D4A),
        ),
        PlaceItem(
            category = PlaceCategory.FOOD,
            name = "홈런 한우구이",
            distance = "도보 680m",
            rating = "4.9",
            reviewCount = 52,
            thumbnailLabel = "한우",
            thumbnailColor = Color(0xFF8F5A3C),
        ),
        PlaceItem(
            category = PlaceCategory.FOOD,
            name = "카페 베이스런",
            distance = "도보 810m",
            rating = "4.4",
            reviewCount = 120,
            thumbnailLabel = "카페",
            thumbnailColor = Color(0xFF6B8F71),
        ),
    )

@Preview(showBackground = true)
@Composable
private fun PlaceScreenPreview() {
    PlaceScreen(scrollToTopEvent = MutableSharedFlow<Unit>())
}
