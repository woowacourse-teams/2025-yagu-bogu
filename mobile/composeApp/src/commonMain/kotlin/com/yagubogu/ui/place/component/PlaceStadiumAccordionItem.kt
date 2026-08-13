package com.yagubogu.ui.place.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yagubogu.ui.theme.Gray900
import com.yagubogu.ui.theme.PretendardMedium16
import com.yagubogu.ui.theme.PretendardSemiBold16
import com.yagubogu.ui.theme.Primary050
import com.yagubogu.ui.theme.Primary500
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.util.rememberNoRippleInteractionSource

@Composable
fun PlaceStadiumAccordionItem(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = name,
        style = if (isSelected) PretendardSemiBold16.copy(fontWeight = FontWeight.Bold) else PretendardMedium16,
        color = if (isSelected) Primary500 else Gray900,
        modifier =
            modifier
                .fillMaxWidth()
                .background(
                    color = if (isSelected) Primary050 else White,
                    shape = RoundedCornerShape(12.dp),
                ).clickable(
                    interactionSource = rememberNoRippleInteractionSource(),
                    indication = null,
                    onClick = onClick,
                ).padding(16.dp),
    )
}
