package com.yagubogu.ui.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yagubogu.data.local.ScoreWidgetSettings
import com.yagubogu.notification.ScoreWidgetMessageProcessor
import com.yagubogu.ui.setting.component.SettingButtonGroup
import com.yagubogu.ui.theme.PretendardSemiBold16
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
actual fun ScoreWidgetDebugControls() {
    val processor: ScoreWidgetMessageProcessor = koinInject()
    val scoreWidgetSettings: ScoreWidgetSettings = koinInject()
    val coroutineScope = rememberCoroutineScope()
    var nextRevision by remember { mutableStateOf(1L) }
    val isEnabled by scoreWidgetSettings.isEnabled.collectAsState(initial = false)

    fun sendFixture(
        gameId: Long,
        type: String,
        gameState: String,
        revision: Long = nextRevision++,
        homeScore: String? = "2",
        awayScore: String? = "3",
        resetState: Boolean = false,
    ) {
        coroutineScope.launch {
            if (resetState) processor.resetForDebug()
            processor.process(
                scoreWidgetFixture(
                    gameId = gameId,
                    type = type,
                    gameState = gameState,
                    revision = revision,
                    homeScore = homeScore,
                    awayScore = awayScore,
                ),
            )
        }
    }

    SettingButtonGroup {
        Text(
            text = "실시간 스코어 알림 테스트",
            style = PretendardSemiBold16,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        )
        Text(
            text = "현재 위젯 수신 상태: ${if (isEnabled) "켜짐" else "꺼짐"}",
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        )
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = {
                    sendFixture(
                        gameId = GAME_A_ID,
                        type = "START",
                        gameState = "SCHEDULED",
                        homeScore = "0",
                        awayScore = "0",
                        resetState = true,
                    )
                },
            ) {
                Text("경기 시작")
            }
            Button(
                onClick = { sendFixture(gameId = GAME_A_ID, type = "UPDATE", gameState = "LIVE") },
            ) {
                Text("점수 변경")
            }
            Button(
                onClick = { sendFixture(gameId = GAME_A_ID, type = "END", gameState = "COMPLETED") },
            ) {
                Text("경기 종료")
            }
            Button(
                onClick = {
                    sendFixture(
                        gameId = GAME_A_ID,
                        type = "END",
                        gameState = "CANCELED",
                        homeScore = null,
                        awayScore = null,
                    )
                },
            ) {
                Text("우천 취소")
            }
            Button(
                onClick = {
                    coroutineScope.launch {
                        processor.resetForDebug()
                        val fixtures =
                            listOf(
                                scoreWidgetFixture(
                                    GAME_A_ID,
                                    "START",
                                    "SCHEDULED",
                                    nextRevision++,
                                    "0",
                                    "0",
                                ),
                                scoreWidgetFixture(GAME_A_ID, "UPDATE", "LIVE", nextRevision++, "2", "3"),
                                scoreWidgetFixture(GAME_A_ID, "END", "COMPLETED", nextRevision++, "2", "3"),
                                scoreWidgetFixture(GAME_B_ID, "START", "SCHEDULED", nextRevision++, "0", "0"),
                            )
                        for (fixture in fixtures) {
                            processor.process(fixture)
                        }
                    }
                },
            ) {
                Text("더블헤더 전환")
            }
            Button(
                onClick = {
                    sendFixture(
                        gameId = GAME_A_ID,
                        type = "END",
                        gameState = "COMPLETED",
                        revision = (nextRevision - 2).coerceAtLeast(0),
                    )
                },
            ) {
                Text("늦게 도착한 A 종료")
            }
        }
    }
}

private fun scoreWidgetFixture(
    gameId: Long,
    type: String,
    gameState: String,
    revision: Long,
    homeScore: String?,
    awayScore: String?,
): Map<String, String> =
    buildMap {
        put("type", type)
        put("gameId", gameId.toString())
        put("displayRevision", revision.toString())
        put("updatedAt", "2026-08-08T09:30:00Z")
        put("stadiumName", "고척 스카이돔")
        put("homeTeamCode", "OB")
        put("homeTeamName", "두산")
        put("awayTeamCode", "LG")
        put("awayTeamName", "LG")
        put("myTeamCode", "OB")
        put("gameState", gameState)
        homeScore?.let { put("homeScore", it) }
        awayScore?.let { put("awayScore", it) }
        if (gameState == "LIVE") {
            put("inning", "5")
            put("inningHalf", "TOP")
            put("baseFirst", "true")
            put("baseSecond", "false")
            put("baseThird", "true")
            put("balls", "1")
            put("strikes", "2")
            put("outs", "1")
            put("pitcherName", "김투수")
            put("batterName", "김타자")
        }
    }

private const val GAME_A_ID = 4021L
private const val GAME_B_ID = 4022L
