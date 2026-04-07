package com.yagubogu.ui.setting.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yagubogu.ui.theme.White

@Composable
fun SettingButtonGroup(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(White, RoundedCornerShape(12.dp))
                .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        content()
    }
}

@Preview
@Composable
private fun SettingButtonGroupPreview() {
    var toggle by remember { mutableStateOf(false) }
    Column {
        SettingButtonGroup {
            SettingButton("1")
            SettingToggleButton(
                "2",
                checked = toggle,
                onCheckedChange = { isChecked ->
                    toggle = isChecked
                },
            )
            SettingButton("3")
        }
    }
}
