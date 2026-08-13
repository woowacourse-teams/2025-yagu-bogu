package com.yagubogu.ui.place

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yagubogu.ui.place.component.PlaceFilterChips
import com.yagubogu.ui.place.component.PlaceRecommendationCard
import com.yagubogu.ui.place.component.PlaceStadiumBottomSheet
import com.yagubogu.ui.place.component.PlaceStadiumSelector
import com.yagubogu.ui.place.model.PlaceCategory
import com.yagubogu.ui.place.model.PlaceItem
import com.yagubogu.ui.place.model.PlaceListUiState
import com.yagubogu.ui.place.model.PlaceStadiumItem
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.Gray600
import com.yagubogu.ui.theme.PretendardBold20
import com.yagubogu.ui.theme.PretendardMedium16
import com.yagubogu.ui.theme.Primary500
import com.yagubogu.ui.util.BackPressHandler
import com.yagubogu.ui.util.rememberNoRippleInteractionSource
import kotlinx.coroutines.flow.SharedFlow
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.place_list_empty
import yagubogu.composeapp.generated.resources.place_list_error
import yagubogu.composeapp.generated.resources.place_list_retry
import yagubogu.composeapp.generated.resources.place_recommendation_title
import yagubogu.composeapp.generated.resources.place_stadium_empty

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceScreen(
    scrollToTopEvent: SharedFlow<Unit>,
    onPlaceItemClick: (Long, String, Int?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlaceViewModel = koinViewModel(),
) {
    val lazyListState: LazyListState = rememberLazyListState()
    val stadiums: List<PlaceStadiumItem> by viewModel.stadiums.collectAsStateWithLifecycle()
    val selectedStadiumId: Long? by viewModel.selectedStadiumId.collectAsStateWithLifecycle()
    val selectedCategory: PlaceCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val placesUiState: PlaceListUiState by viewModel.places.collectAsStateWithLifecycle()
    var isStadiumSheetVisible: Boolean by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadStadiums()
    }

    LaunchedEffect(Unit) {
        scrollToTopEvent.collect {
            lazyListState.animateScrollToItem(0)
        }
    }

    BackPressHandler()

    if (isStadiumSheetVisible) {
        PlaceStadiumBottomSheet(
            stadiums = stadiums,
            selectedStadiumId = selectedStadiumId,
            onStadiumClick = { stadiumId: Long ->
                viewModel.selectStadium(stadiumId)
                isStadiumSheetVisible = false
            },
            onDismiss = { isStadiumSheetVisible = false },
        )
    }

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
                onClick = { isStadiumSheetVisible = true },
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            PlaceFilterChips(
                selectedCategory = selectedCategory,
                onCategoryClick = { category: PlaceCategory -> viewModel.selectCategory(category) },
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
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

            is PlaceListUiState.Success ->
                items(
                    items = state.items,
                    key = { item: PlaceItem -> item.id },
                ) { item: PlaceItem ->
                    PlaceRecommendationCard(
                        item = item,
                        onClick = { onPlaceItemClick(item.id, item.name, item.distanceMeters) },
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

            PlaceListUiState.Empty ->
                item {
                    Text(
                        text = stringResource(Res.string.place_list_empty),
                        style = PretendardMedium16,
                        color = Gray600,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

            PlaceListUiState.NoStadium ->
                item {
                    Text(
                        text = stringResource(Res.string.place_stadium_empty),
                        style = PretendardMedium16,
                        color = Gray600,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

            is PlaceListUiState.Error ->
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(Res.string.place_list_error),
                            style = PretendardMedium16,
                            color = Gray600,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(Res.string.place_list_retry),
                            style = PretendardMedium16,
                            color = Primary500,
                            modifier =
                                Modifier.clickable(
                                    interactionSource = rememberNoRippleInteractionSource(),
                                    indication = null,
                                    onClick = { viewModel.retry() },
                                ),
                        )
                    }
                }
        }
    }
}
