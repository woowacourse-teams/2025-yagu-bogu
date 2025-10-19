package com.yagubogu.ui.pastcheckin.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yagubogu.domain.model.Team
import com.yagubogu.presentation.livetalk.stadium.LivetalkStadiumItem
import com.yagubogu.ui.theme.Black
import com.yagubogu.ui.theme.EsamanruMedium12
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.PretendardBold16
import com.yagubogu.ui.theme.Primary100
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.theme.YaguBoguTheme

@Composable
fun GameListItem(
    modifier: Modifier = Modifier,
    game: LivetalkStadiumItem,
    onGameClick: (LivetalkStadiumItem) -> Unit,
) {
    Card(
        onClick = { onGameClick(game) },
        modifier = modifier.fillMaxWidth(),
        colors =
            if (game.isVerified) {
                CardDefaults.cardColors(containerColor = Primary100)
            } else {
                CardDefaults.cardColors(containerColor = White)
            },
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // 🏟️ 상단 헤더 (경기장)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = game.stadiumName,
                        style = PretendardBold16,
                        color = Black,
                    )
                }
            }

            // 🏟️ 팀 vs 팀 레이아웃 (VS 중앙 배치)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 🏠 홈팀 (왼쪽)
                TeamInfo(
                    team = game.homeTeam,
                    emoji = game.homeTeamEmoji,
                    modifier = Modifier.weight(1f),
                )

                // ⚔️ VS (중앙)
                Text(
                    text = "VS",
                    style = PretendardBold16,
                    color = Gray500,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )

                // 🚌 어웨이팀 (오른쪽)
                TeamInfo(
                    team = game.awayTeam,
                    emoji = game.awayTeamEmoji,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun TeamInfo(
    team: Team,
    emoji: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 팀 이모지
        Text(
            text = emoji,
            style = MaterialTheme.typography.headlineSmall,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 팀명
        Text(
            text = team.shortname,
            style = EsamanruMedium12,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

// Preview
@Preview(name = "GameListItem - 기본", showBackground = true)
@Composable
private fun GameListItemPreview() {
    YaguBoguTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            // 샘플 데이터로 Preview
            val sampleGame = createPreviewSampleGame(isVerified = false)

            GameListItem(
                game = sampleGame,
                onGameClick = { },
            )
        }
    }
}

@Preview(name = "GameListItem - 이미 인증한 경기", showBackground = true)
@Composable
private fun GameListItemVerifiedPreview() {
    YaguBoguTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            val sampleGame = createPreviewSampleGame(isVerified = true)

            GameListItem(
                game = sampleGame,
                onGameClick = { },
            )
        }
    }
}

@Preview(name = "GameListItem 목록", showBackground = true, heightDp = 600)
@Composable
private fun GameListItemsPreview() {
    YaguBoguTheme {
        LazyColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(5) { index ->
                GameListItem(
                    game = createPreviewSampleGame(isVerified = index % 2 == 0),
                    onGameClick = { },
                )
            }
        }
    }
}

// 샘플 데이터 생성 함수 (Preview용)
private fun createPreviewSampleGame(isVerified: Boolean): LivetalkStadiumItem =
    LivetalkStadiumItem(
        gameId = 1L,
        stadiumName = "고척 스카이돔",
        userCount = 42,
        awayTeam = Team.LG,
        homeTeam = Team.HH,
        isVerified = isVerified,
    )
