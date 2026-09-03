package com.yagubogu.ui.livetalk.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yagubogu.ui.livetalk.model.BallCountUiModel
import com.yagubogu.ui.theme.Gray300
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.Green
import com.yagubogu.ui.theme.PretendardSemiBold12
import com.yagubogu.ui.theme.Red
import com.yagubogu.ui.theme.Yellow

@Composable
fun BallStrikeOutCount(
    ballStrikeOutCount: BallCountUiModel?,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        CountRow(
            label = "B",
            count = ballStrikeOutCount?.ballCount ?: 0,
            maxCount = 3,
            color = Green,
        )
        CountRow(
            label = "S",
            count = ballStrikeOutCount?.strikeCount ?: 0,
            maxCount = 2,
            color = Yellow,
        )
        CountRow(
            label = "O",
            count = ballStrikeOutCount?.outCount ?: 0,
            maxCount = 2,
            color = Red,
        )
    }
}

@Composable
private fun CountRow(
    label: String,
    count: Int,
    maxCount: Int,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Text(
            text = label,
            style = PretendardSemiBold12.copy(color = Gray500),
        )

        repeat(maxCount) { index: Int ->
            Box(
                modifier =
                    Modifier
                        .size(10.dp)
                        .background(
                            color = if (index < count) color else Gray300,
                            shape = CircleShape,
                        ),
            )
        }
    }
}
