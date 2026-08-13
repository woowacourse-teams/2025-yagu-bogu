package com.yagubogu.ui.place.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yagubogu.ui.place.model.PlaceStadiumItem
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.Gray900
import com.yagubogu.ui.theme.PretendardBold20
import com.yagubogu.ui.theme.PretendardMedium16
import com.yagubogu.ui.theme.PretendardSemiBold16
import com.yagubogu.ui.theme.Primary050
import com.yagubogu.ui.theme.Primary500
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.util.rememberNoRippleInteractionSource
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.place_stadium_bottom_sheet_title
import yagubogu.composeapp.generated.resources.place_stadium_empty

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceStadiumBottomSheet(
    stadiums: List<PlaceStadiumItem>,
    selectedStadiumId: Long?,
    onStadiumClick: (Long) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
        containerColor = Gray050,
        modifier = modifier,
    ) {
        val lazyListState: LazyListState = rememberLazyListState()
        LazyColumn(
            state = lazyListState,
            modifier = modifier.fillMaxWidth(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = stringResource(Res.string.place_stadium_bottom_sheet_title),
                    style = PretendardBold20,
                    color = Gray900,
                )
            }

            if (stadiums.isEmpty()) {
                item {
                    Text(
                        text = stringResource(Res.string.place_stadium_empty),
                        style = PretendardMedium16,
                        color = Gray900,
                        modifier = Modifier.padding(vertical = 20.dp),
                    )
                }
            } else {
                items(
                    items = stadiums,
                    key = { item: PlaceStadiumItem -> item.id },
                ) { item: PlaceStadiumItem ->
                    val isSelected: Boolean = item.id == selectedStadiumId
                    Text(
                        text = item.name,
                        style = if (isSelected) PretendardSemiBold16.copy(fontWeight = FontWeight.Bold) else PretendardMedium16,
                        color = if (isSelected) Primary500 else Gray900,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .background(
                                    color = if (isSelected) Primary050 else White,
                                    shape = RoundedCornerShape(12.dp),
                                ).clickable(
                                    interactionSource = rememberNoRippleInteractionSource(),
                                    indication = null,
                                    onClick = { onStadiumClick(item.id) },
                                ).padding(16.dp),
                    )
                }
            }
        }
    }
}
