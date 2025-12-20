package com.yagubogu.ui.livetalk.chat.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import com.yagubogu.domain.model.Team
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.Gray300

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LivetalkChatScreen(
    onBackClick: () -> Unit,
    stadiumName: String?,
    team: Team?,
    matchText: String?,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = { LivetalkChatToolbar(onBackClick = onBackClick, stadiumName, matchText) },
        bottomBar = {
            LivetalkChatInputBar(
                messageFormText = "임시 텍스트인 것이다",
                stadiumName = stadiumName,
                isVerified = true,
                onTextChange = {},
                onSendMessage = {},
            )
        },
        containerColor = Gray050,
        modifier = modifier.background(Gray300),
    ) { innerPadding: PaddingValues ->

        Box(
            modifier =
                Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
        ) {
            Column(
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
            ) {
                // 구분선
                HorizontalDivider(thickness = max(0.4.dp, Dp.Hairline), color = Gray300)

                // 응원 바
                if (team != null) {
                    LivetalkChatCheeringBar(
                        team = Team.WO,
                        cheeringCount = 12345L,
                        onCheeringClick = { /* 응원 전송 이벤트 */ },
                    )
                }

            }
        }
    }
}

@Preview
@Composable
private fun LivetalkChatScreenPreview() {
    LivetalkChatScreen(
        onBackClick = {},
        team = Team.WO,
        stadiumName = "고척 스카이돔",
        matchText = "두산 vs 키움",
    )
}

@Preview
@Composable
private fun LivetalkChatLoadingScreenPreview() {
    LivetalkChatScreen(
        onBackClick = {},
        team = null,
        stadiumName = null,
        matchText = null,
    )
}
