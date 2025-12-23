package com.yagubogu.ui.livetalk.chat.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yagubogu.R
import com.yagubogu.domain.model.Team
import com.yagubogu.ui.theme.PretendardMedium16
import com.yagubogu.ui.util.emoji
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LivetalkChatCheeringBar(
    modifier: Modifier = Modifier,
    team: Team,
    cheeringCount: Long,
    onCheeringClick: () -> Unit,
    onPositioned: (Offset) -> Unit = {},
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Color.White)
                .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val cheeringText =
            stringResource(
                id = R.string.livetalk_like_count_message,
                team.shortname,
                cheeringCount,
            )

        Text(
            text = cheeringText,
            style = PretendardMedium16,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(8.dp))

        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = team.emoji,
                fontSize = 28.sp,
                modifier =
                    Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onCheeringClick,
                        ).onGloballyPositioned { coordinates ->
                            val posInRoot = coordinates.positionInRoot()
                            val centerPos =
                                Offset(
                                    x = posInRoot.x + coordinates.size.width / 2f,
                                    y = posInRoot.y + coordinates.size.height / 2f,
                                )
                            Timber.d("응원 버튼 이모지 중앙 좌표 : $centerPos")
                            onPositioned(centerPos)
                        },
            )
        }
    }
}

@Preview
@Composable
private fun LivetalkChatCheeringBarPreviewEmptyInput() {
    LivetalkChatCheeringBar(
        team = Team.HH,
        cheeringCount = 12345L,
        onCheeringClick = {},
        onPositioned = {},
    )
}
