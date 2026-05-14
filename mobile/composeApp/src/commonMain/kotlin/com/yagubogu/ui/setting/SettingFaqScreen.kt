package com.yagubogu.ui.setting

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.yagubogu.ui.common.YaguboguWebView

@Composable
fun SettingFaqScreen(modifier: Modifier = Modifier) {
    YaguboguWebView(
        url = "https://board.yagubogu.com/i/faq-w1wq8B34UVY",
        modifier = modifier,
    )
}
