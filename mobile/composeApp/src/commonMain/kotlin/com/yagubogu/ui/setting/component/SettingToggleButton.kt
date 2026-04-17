package com.yagubogu.ui.setting.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
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
import com.yagubogu.analytics.AnalyticsLogger
import com.yagubogu.ui.theme.Gray100
import com.yagubogu.ui.theme.Gray300
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.theme.PretendardSemiBold16
import com.yagubogu.ui.theme.Primary100
import com.yagubogu.ui.theme.Primary500
import com.yagubogu.ui.theme.YaguBoguTheme
import com.yagubogu.ui.util.BalloonTooltip
import com.yagubogu.ui.util.noRippleClickable
import org.jetbrains.compose.resources.painterResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.ic_info

@Composable
fun SettingToggleButton(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    toolTipText: String? = null,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .noRippleClickable { }
                .padding(horizontal = 20.dp)
                .heightIn(min = 40.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = text, style = PretendardSemiBold16)
            if (toolTipText != null) {
                BalloonTooltip(
                    text = toolTipText,
                ) { showTooltip ->
                    Icon(
                        painter = painterResource(Res.drawable.ic_info),
                        contentDescription = null,
                        tint = Gray300,
                        modifier =
                            Modifier
                                .padding(horizontal = 8.dp)
                                .noRippleClickable {
                                    showTooltip()
                                    AnalyticsLogger.logEvent("tooltip_geofence_switch")
                                },
                    )
                }
            }
        }

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
        Column {
            SettingToggleButton(
                text = "알림 받기",
                checked = toggle,
                onCheckedChange = { isChecked ->
                    toggle = isChecked
                },
            )
            SettingToggleButton(
                text = "알림 받기 & 툴팁",
                toolTipText = "테스트 툴팁이에요",
                checked = toggle,
                onCheckedChange = { isChecked ->
                    toggle = isChecked
                },
            )
        }
    }
}
