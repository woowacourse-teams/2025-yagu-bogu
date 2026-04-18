package com.yagubogu.ui.attendance.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.PretendardRegular
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.util.noRippleClickable
import org.jetbrains.compose.resources.painterResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.ic_check

@Composable
fun FilterCheckbox(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.noRippleClickable { onClick() },
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .size(16.dp)
                    .border(width = 1.dp, color = Gray500, shape = CircleShape)
                    .background(
                        color = if (isSelected) Gray500 else Color.Transparent,
                        shape = CircleShape,
                    ),
        ) {
            if (isSelected) {
                Icon(
                    painter = painterResource(Res.drawable.ic_check),
                    contentDescription = null,
                    tint = White,
                    modifier = Modifier.size(12.dp).align(Alignment.Center),
                )
            }
        }
        Text(
            text = text,
            style = PretendardRegular.copy(fontSize = 14.sp, color = Gray500),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FilterCheckboxOnPreview() {
    FilterCheckbox(
        text = "체크박스 On",
        isSelected = true,
        onClick = {},
    )
}

@Preview(showBackground = true)
@Composable
private fun FilterCheckboxOffPreview() {
    FilterCheckbox(
        text = "체크박스 Off",
        isSelected = false,
        onClick = {},
    )
}
