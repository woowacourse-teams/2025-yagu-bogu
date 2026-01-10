package com.yagubogu.ui.setting.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yagubogu.R
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.PretendardSemiBold16
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.util.noRippleClickable

@Preview(showBackground = true)
@Composable
fun SettingButton(
    modifier: Modifier = Modifier,
    text: String = "",
    onClick: () -> Unit = {},
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .noRippleClickable { onClick() },
    ) {
        Spacer(
            modifier =
                Modifier
                    .height(10.dp)
                    .fillMaxWidth(),
        )
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = text, style = PretendardSemiBold16)
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Gray500,
            )
        }
        Spacer(
            modifier =
                Modifier
                    .height(10.dp)
                    .fillMaxWidth(),
        )
    }
}

@Composable
fun SettingButtonGroup(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(White, RoundedCornerShape(12.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            content()
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}
