package com.yagubogu.ui.place

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yagubogu.ui.place.component.PlaceFilterChips
import com.yagubogu.ui.place.component.PlaceRecommendationCard
import com.yagubogu.ui.place.component.PlaceStadiumAccordionItem
import com.yagubogu.ui.place.component.PlaceStadiumSelector
import com.yagubogu.ui.place.model.PlaceCategory
import com.yagubogu.ui.place.model.PlaceItem
import com.yagubogu.ui.place.model.PlaceListUiState
import com.yagubogu.ui.place.model.PlaceStadiumItem
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.Gray600
import com.yagubogu.ui.theme.PretendardBold20
import com.yagubogu.ui.theme.PretendardMedium16
import com.yagubogu.ui.util.BackPressHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.place_recommendation_title
import yagubogu.composeapp.generated.resources.place_stadium_empty

@Composable
fun PlaceScreen(
    scrollToTopEvent: SharedFlow<Unit>,
    onPlaceItemClick: (Long, String, Int?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlaceViewModel = koinViewModel(),
) {
    val stadiums: List<PlaceStadiumItem> by viewModel.stadiums.collectAsStateWithLifecycle()
    val selectedStadiumId: Long? by viewModel.selectedStadiumId.collectAsStateWithLifecycle()
    val selectedCategory: PlaceCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val placesUiState: PlaceListUiState by viewModel.places.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadStadiums()
    }

    PlaceScreen(
        stadiums = stadiums,
        selectedStadiumId = selectedStadiumId,
        selectedCategory = selectedCategory,
        placesUiState = placesUiState,
        onStadiumClick = viewModel::selectStadium,
        onCategoryClick = viewModel::selectCategory,
        onPlaceItemClick = onPlaceItemClick,
        modifier = modifier,
        scrollToTopEvent = scrollToTopEvent,
    )
}

@Composable
private fun PlaceScreen(
    stadiums: List<PlaceStadiumItem>,
    selectedStadiumId: Long?,
    selectedCategory: PlaceCategory,
    placesUiState: PlaceListUiState,
    onStadiumClick: (Long) -> Unit,
    onCategoryClick: (PlaceCategory) -> Unit,
    onPlaceItemClick: (Long, String, Int?) -> Unit,
    modifier: Modifier = Modifier,
    scrollToTopEvent: SharedFlow<Unit> = MutableSharedFlow(),
    initiallyStadiumListExpanded: Boolean = false,
) {
    val lazyListState: LazyListState = rememberLazyListState()
    var isStadiumListExpanded: Boolean by rememberSaveable { mutableStateOf(initiallyStadiumListExpanded) }

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
            val selectedStadiumName: String =
                stadiums.firstOrNull { it.id == selectedStadiumId }?.name
                    ?: stringResource(Res.string.place_stadium_empty)
            PlaceStadiumSelector(
                stadiumName = selectedStadiumName,
                onClick = { isStadiumListExpanded = !isStadiumListExpanded },
            )
        }

        if (isStadiumListExpanded) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (stadiums.isEmpty()) {
                item {
                    Text(
                        text = stringResource(Res.string.place_stadium_empty),
                        style = PretendardMedium16,
                        color = Gray600,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    )
                }
            } else {
                items(
                    items = stadiums,
                    key = { item: PlaceStadiumItem -> "stadium-${item.id}" },
                ) { item: PlaceStadiumItem ->
                    PlaceStadiumAccordionItem(
                        name = item.name,
                        isSelected = item.id == selectedStadiumId,
                        onClick = {
                            onStadiumClick(item.id)
                            isStadiumListExpanded = false
                        },
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            PlaceFilterChips(
                selectedCategory = selectedCategory,
                onCategoryClick = onCategoryClick,
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        item {
            Text(
                text = stringResource(Res.string.place_recommendation_title),
                style = PretendardBold20,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        when (val state = placesUiState) {
            PlaceListUiState.Loading ->
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

            is PlaceListUiState.Success ->
                items(
                    items = state.items,
                    key = { item: PlaceItem -> "place-${item.id}" },
                ) { item: PlaceItem ->
                    PlaceRecommendationCard(
                        item = item,
                        onClick = { onPlaceItemClick(item.id, item.name, item.distanceMeters) },
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

            PlaceListUiState.Empty -> Unit

            PlaceListUiState.NoStadium ->
                item {
                    Text(
                        text = stringResource(Res.string.place_stadium_empty),
                        style = PretendardMedium16,
                        color = Gray600,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
        }
    }
}

private val PLACE_STADIUM_ITEMS: List<PlaceStadiumItem> =
    listOf(
        PlaceStadiumItem(id = 1L, name = "잠실야구장"),
        PlaceStadiumItem(id = 2L, name = "고척스카이돔"),
        PlaceStadiumItem(id = 3L, name = "수원 케이티위즈파크"),
    )

private val PLACE_ITEMS: List<PlaceItem> =
    listOf(
        PlaceItem(
            id = 1L,
            category = PlaceCategory.FOOD,
            name = "잠실 원조 순대국밥",
            distance = "도보 320m",
            distanceMeters = 320,
            imageUrl = null,
        ),
        PlaceItem(
            id = 2L,
            category = PlaceCategory.CAFE,
            name = "카페 베이스런",
            distance = "도보 810m",
            distanceMeters = 810,
            imageUrl = "https://picsum.photos/seed/place-preview-cafe/400/300",
        ),
    )

@Preview("플레이스 목록 화면")
@Composable
private fun PlaceScreenPreview() {
    PlaceScreen(
        stadiums = PLACE_STADIUM_ITEMS,
        selectedStadiumId = PLACE_STADIUM_ITEMS.first().id,
        selectedCategory = PlaceCategory.FOOD,
        placesUiState = PlaceListUiState.Success(PLACE_ITEMS),
        onStadiumClick = {},
        onCategoryClick = {},
        onPlaceItemClick = { _, _, _ -> },
    )
}

@Preview("플레이스 목록 화면 - 구장 선택 펼침")
@Composable
private fun PlaceScreenStadiumExpandedPreview() {
    PlaceScreen(
        stadiums = PLACE_STADIUM_ITEMS,
        selectedStadiumId = PLACE_STADIUM_ITEMS.first().id,
        selectedCategory = PlaceCategory.FOOD,
        placesUiState = PlaceListUiState.Success(PLACE_ITEMS),
        onStadiumClick = {},
        onCategoryClick = {},
        onPlaceItemClick = { _, _, _ -> },
        initiallyStadiumListExpanded = true,
    )
}

@Preview("플레이스 목록 화면 - 로딩")
@Composable
private fun PlaceScreenLoadingPreview() {
    PlaceScreen(
        stadiums = PLACE_STADIUM_ITEMS,
        selectedStadiumId = PLACE_STADIUM_ITEMS.first().id,
        selectedCategory = PlaceCategory.STAY,
        placesUiState = PlaceListUiState.Loading,
        onStadiumClick = {},
        onCategoryClick = {},
        onPlaceItemClick = { _, _, _ -> },
    )
}

@Preview("플레이스 목록 화면 - 빈 카테고리")
@Composable
private fun PlaceScreenEmptyPreview() {
    PlaceScreen(
        stadiums = PLACE_STADIUM_ITEMS,
        selectedStadiumId = PLACE_STADIUM_ITEMS.first().id,
        selectedCategory = PlaceCategory.SHOW,
        placesUiState = PlaceListUiState.Empty,
        onStadiumClick = {},
        onCategoryClick = {},
        onPlaceItemClick = { _, _, _ -> },
    )
}

@Preview("플레이스 목록 화면 - 오늘 경기 없음")
@Composable
private fun PlaceScreenNoStadiumPreview() {
    PlaceScreen(
        stadiums = emptyList(),
        selectedStadiumId = null,
        selectedCategory = PlaceCategory.STAY,
        placesUiState = PlaceListUiState.NoStadium,
        onStadiumClick = {},
        onCategoryClick = {},
        onPlaceItemClick = { _, _, _ -> },
    )
}
