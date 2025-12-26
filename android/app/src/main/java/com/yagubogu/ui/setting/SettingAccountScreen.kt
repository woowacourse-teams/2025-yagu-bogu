package com.yagubogu.ui.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yagubogu.R
import com.yagubogu.ui.theme.Gray050

@Composable
fun SettingAccountScreen(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Gray050)
                .padding(20.dp)
                .scrollable(state = rememberScrollState(), orientation = Orientation.Vertical),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        SettingButtonGroup {
            SettingButton(
                text = stringResource(R.string.setting_logout),
                onClick = {},
            )
            SettingButton(
                text = stringResource(R.string.setting_delete_account),
                onClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun SettingAccountScreenPreview() {
    SettingAccountScreen()
}
