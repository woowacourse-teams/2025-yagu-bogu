package com.yagubogu.ui.place.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.yagubogu.ui.place.model.PlaceStadiumItem
import com.yagubogu.ui.theme.Gray300
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.PretendardRegular16
import com.yagubogu.ui.theme.PretendardSemiBold16
import com.yagubogu.ui.theme.Primary500
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.util.crop

@Composable
fun PlaceStadiumDropdown(
    stadiumName: String,
    stadiums: List<PlaceStadiumItem>,
    selectedStadiumId: Long?,
    onStadiumClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isExpanded: Boolean by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        PlaceStadiumSelector(
            stadiumName = stadiumName,
            onClick = { isExpanded = !isExpanded },
        )
        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            offset = DpOffset(0.dp, 4.dp),
            containerColor = White,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(0.4.dp, Gray300),
            modifier = Modifier.crop(vertical = 8.dp),
        ) {
            stadiums.forEach { stadium: PlaceStadiumItem ->
                val isSelected: Boolean = stadium.id == selectedStadiumId
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stadium.name,
                            style =
                                if (isSelected) {
                                    PretendardSemiBold16.copy(color = Primary500)
                                } else {
                                    PretendardRegular16.copy(color = Gray500)
                                },
                        )
                    },
                    onClick = {
                        onStadiumClick(stadium.id)
                        isExpanded = false
                    },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.crop(horizontal = 0.dp, vertical = 8.dp),
                )
            }
        }
    }
}
