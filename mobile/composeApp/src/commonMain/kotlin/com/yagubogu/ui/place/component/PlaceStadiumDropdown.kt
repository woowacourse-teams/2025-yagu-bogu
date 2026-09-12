package com.yagubogu.ui.place.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yagubogu.ui.place.model.PlaceStadiumItem
import com.yagubogu.ui.theme.Gray300
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.PretendardRegular
import com.yagubogu.ui.theme.PretendardSemiBold
import com.yagubogu.ui.theme.Primary500
import com.yagubogu.ui.theme.White

@Composable
fun PlaceStadiumDropdown(
    stadiumName: String,
    stadiums: List<PlaceStadiumItem>,
    selectedStadiumId: Long?,
    onStadiumClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isExpanded: Boolean by remember { mutableStateOf(false) }
    var anchorWidthPx: Int by remember { mutableIntStateOf(0) }
    val density: Density = LocalDensity.current
    val anchorWidth: Dp = with(density) { anchorWidthPx.toDp() }

    Box(modifier = modifier) {
        PlaceStadiumSelector(
            stadiumName = stadiumName,
            onClick = { isExpanded = !isExpanded },
            modifier = Modifier.onSizeChanged { size -> anchorWidthPx = size.width },
        )
        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            offset = DpOffset(0.dp, 4.dp),
            containerColor = White,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(0.4.dp, Gray300),
            modifier = Modifier.width(anchorWidth),
        ) {
            stadiums.forEach { stadium: PlaceStadiumItem ->
                val isSelected: Boolean = stadium.id == selectedStadiumId
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stadium.name,
                            style =
                                if (isSelected) {
                                    PretendardSemiBold.copy(fontSize = 18.sp, color = Primary500)
                                } else {
                                    PretendardRegular.copy(fontSize = 18.sp, color = Gray500)
                                },
                        )
                    },
                    onClick = {
                        onStadiumClick(stadium.id)
                        isExpanded = false
                    },
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                )
            }
        }
    }
}
