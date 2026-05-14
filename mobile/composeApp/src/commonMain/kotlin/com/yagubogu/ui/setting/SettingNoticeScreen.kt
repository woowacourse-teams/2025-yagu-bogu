package com.yagubogu.ui.setting

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.yagubogu.ui.common.YaguboguWebView

@Composable
fun SettingNoticeScreen(modifier: Modifier = Modifier) {
    YaguboguWebView(
        url = "https://board.yagubogu.com",
        modifier = modifier,
    )
}
