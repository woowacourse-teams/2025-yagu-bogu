package com.yagubogu.ui.common.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yagubogu.ui.theme.Gray300
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.theme.PretendardRegular
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.util.noRippleClickable
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.all_show_more
import yagubogu.composeapp.generated.resources.home_show_less
import yagubogu.composeapp.generated.resources.ic_arrow_down
import yagubogu.composeapp.generated.resources.ic_arrow_up

@Composable
fun ShowMoreButton(
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier =
            modifier
                .fillMaxWidth()
                .noRippleClickable(onClick = onClick)
                .border((0.6).dp, Gray300, RoundedCornerShape(12.dp))
                .background(color = White, shape = RoundedCornerShape(12.dp))
                .padding(vertical = 8.dp),
    ) {
        Text(
            text = stringResource(if (isExpanded) Res.string.home_show_less else Res.string.all_show_more),
            color = Gray400,
            style = PretendardRegular,
            fontSize = 14.sp,
        )
        Spacer(modifier = Modifier.width(4.dp))
        Image(
            painter = painterResource(if (isExpanded) Res.drawable.ic_arrow_up else Res.drawable.ic_arrow_down),
            contentDescription = stringResource(if (isExpanded) Res.string.home_show_less else Res.string.all_show_more),
            colorFilter = ColorFilter.tint(Gray400),
            modifier = Modifier.size(20.dp),
        )
    }
}

@Preview
@Composable
private fun ShowMoreButtonPreview() {
    ShowMoreButton(isExpanded = false, onClick = {})
}

@Preview
@Composable
private fun ShowMoreButtonExpandedPreview() {
    ShowMoreButton(isExpanded = true, onClick = {})
}
