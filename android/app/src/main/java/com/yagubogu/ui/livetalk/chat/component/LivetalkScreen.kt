package com.yagubogu.ui.livetalk.chat.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.Gray300

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LivetalkChatScreen(
    onBackClick: () -> Unit,
    stadiumName: String?,
    matchText: String?,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = { LivetalkChatToolbar(onBackClick = onBackClick, stadiumName, matchText) },
        containerColor = Gray050,
        modifier = modifier.background(Gray300),
    ) { innerPadding: PaddingValues ->

        Text(modifier = Modifier.padding(innerPadding), text = "hello compose chat!")
    }
}

@Preview
@Composable
private fun BadgeScreenPreview() {
    LivetalkChatScreen(
        onBackClick = {},
        stadiumName = "고척 스카이돔",
        matchText = "두산 vs 키움",
    )
}


@Preview
@Composable
private fun LivetalkChatLoadingScreenPreview() {
    LivetalkChatScreen(
        onBackClick = {},
        stadiumName = null,
        matchText = null,
    )
}

