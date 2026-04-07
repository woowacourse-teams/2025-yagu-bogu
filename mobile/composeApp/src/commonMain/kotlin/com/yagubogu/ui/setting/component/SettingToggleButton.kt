package com.yagubogu.ui.setting.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yagubogu.ui.theme.Gray100
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.theme.PretendardSemiBold16
import com.yagubogu.ui.theme.Primary100
import com.yagubogu.ui.theme.Primary500
import com.yagubogu.ui.theme.YaguBoguTheme
import com.yagubogu.ui.util.noRippleClickable

@Composable
fun SettingToggleButton(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .noRippleClickable { }
                .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = text, style = PretendardSemiBold16)

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors =
                SwitchDefaults.colors(
                    checkedThumbColor = Primary500,
                    checkedTrackColor = Primary100,
                    uncheckedThumbColor = Gray400,
                    uncheckedTrackColor = Gray100,
                ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingButtonPreview() {
    var toggle by remember { mutableStateOf(false) }

    YaguBoguTheme {
        SettingToggleButton(
            text = "알림 받기",
            checked = toggle,
            onCheckedChange = { isChecked ->
                toggle = isChecked
            },
        )
    }
}
