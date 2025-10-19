package com.yagubogu.ui.pastcheckin.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.Gray700
import com.yagubogu.ui.theme.PretendardBold20
import com.yagubogu.ui.theme.PretendardMedium16
import com.yagubogu.ui.theme.YaguBoguTheme

/**
 * 다양한 상태 정보를 표시하는 범용 패널 컴포넌트
 * 로딩, 빈 상태, 에러 상태 등을 통합적으로 처리
 */
@Composable
fun InfoPanel(
    modifier: Modifier = Modifier,
    emoji: String,
    title: String,
    subtitle: String? = null,
    showLoading: Boolean = false,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = Gray050,
            ),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement =
                    Arrangement.spacedBy(
                        if (showLoading) 16.dp else 12.dp,
                    ),
            ) {
                // 로딩 인디케이터 또는 이모지
                if (showLoading) {
                    CircularProgressIndicator()
                } else {
                    Text(
                        text = emoji,
                        style = MaterialTheme.typography.headlineLarge,
                    )
                }

                Text(
                    text = title,
                    style = PretendardBold20,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )

                subtitle?.let {
                    Text(
                        text = it,
                        style = PretendardMedium16,
                        color = Gray700,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Preview(name = "InfoPanel - 날짜 미선택", showBackground = true)
@Composable
private fun InfoPanelPreview_SelectDate() {
    YaguBoguTheme {
        InfoPanel(
            emoji = "⚾",
            title = "날짜를 선택해주세요",
            subtitle = "과거에 직관한 경기 날짜를 선택하면\n해당 날짜의 경기 목록을 확인할 수 있습니다",
        )
    }
}

@Preview(name = "InfoPanel - 로딩중", showBackground = true)
@Composable
private fun InfoPanelPreview_Loading() {
    YaguBoguTheme {
        InfoPanel(
            emoji = "",
            title = "경기 목록을 불러오는 중...",
            subtitle = "잠시만 기다려주세요",
            showLoading = true,
        )
    }
}

@Preview(name = "InfoPanel - 경기 없음", showBackground = true)
@Composable
private fun InfoPanelPreview_NoGames() {
    YaguBoguTheme {
        InfoPanel(
            emoji = "📅",
            title = "해당 날짜에 경기가 없습니다",
            subtitle = "다른 날짜를 선택해주세요",
        )
    }
}

@Preview(name = "InfoPanel - 에러 상태", showBackground = true)
@Composable
private fun InfoPanelPreview_Error() {
    YaguBoguTheme {
        InfoPanel(
            emoji = "⚠️",
            title = "데이터를 불러올 수 없습니다",
            subtitle = "네트워크 연결을 확인하고 다시 시도해주세요",
        )
    }
}
