package com.yagubogu.ui.place.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yagubogu.ui.place.model.PlaceCategory
import com.yagubogu.ui.place.model.labelResource
import com.yagubogu.ui.theme.Gray100
import com.yagubogu.ui.theme.Gray200
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.PretendardMedium16
import com.yagubogu.ui.theme.PretendardSemiBold16
import com.yagubogu.ui.theme.Primary500
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.util.rememberNoRippleInteractionSource
import org.jetbrains.compose.resources.stringResource

@Composable
fun PlaceFilterChips(
    selectedCategory: PlaceCategory,
    onCategoryClick: (PlaceCategory) -> Unit,
    modifier: Modifier = Modifier,
    categories: List<PlaceCategory> = PlaceCategory.entries,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
    ) {
        categories.forEachIndexed { index: Int, category: PlaceCategory ->
            if (index > 0) {
                Spacer(modifier = Modifier.width(8.dp))
            }
            PlaceFilterChip(
                label = stringResource(category.labelResource),
                isSelected = selectedCategory == category,
                onClick = { onCategoryClick(category) },
            )
        }
    }
}

@Composable
private fun PlaceFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = label,
        style = if (isSelected) PretendardSemiBold16 else PretendardMedium16,
        color = if (isSelected) White else Gray500,
        modifier =
            modifier
                .background(
                    color = if (isSelected) Primary500 else Gray100,
                    shape = CircleShape,
                ).then(
                    if (isSelected) {
                        Modifier
                    } else {
                        Modifier.border(1.dp, Gray200, CircleShape)
                    },
                ).clickable(
                    interactionSource = rememberNoRippleInteractionSource(),
                    indication = null,
                    onClick = onClick,
                ).padding(horizontal = 20.dp, vertical = 12.dp),
    )
}
