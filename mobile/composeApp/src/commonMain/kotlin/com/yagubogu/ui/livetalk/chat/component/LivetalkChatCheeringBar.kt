package com.yagubogu.ui.livetalk.chat.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yagubogu.domain.model.Team
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.PretendardMedium16
import com.yagubogu.ui.util.formatWithComma
import com.yagubogu.ui.util.mascot
import com.yagubogu.ui.util.noRippleClickable
import com.yagubogu.ui.util.shimmerIf
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.livetalk_like_count_message

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LivetalkChatCheeringBar(
    team: Team,
    cheeringCount: Long?,
    modifier: Modifier = Modifier,
    onCheeringClick: () -> Unit,
    onPositioned: (Offset) -> Unit = {},
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Gray050)
                .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val cheeringText =
            stringResource(
                Res.string.livetalk_like_count_message,
                team.shortname,
                (cheeringCount ?: 0L).formatWithComma(),
            )

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = cheeringText,
                style = PretendardMedium16,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .shimmerIf(cheeringCount == null)
                        .then(
                            when {
                                cheeringCount == null -> {
                                    Modifier.size(
                                        width = 180.dp,
                                        height = 20.dp,
                                    )
                                }

                                else -> {
                                    Modifier.wrapContentSize()
                                }
                            },
                        ),
            )
        }
        Spacer(Modifier.width(8.dp))

        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(team.mascot),
                contentDescription = null,
                modifier =
                    Modifier
                        .size(32.dp)
                        .shimmerIf(cheeringCount == null)
                        .noRippleClickable(
                            onClick = onCheeringClick,
                        ).onGloballyPositioned { coordinates ->
                            val posInRoot = coordinates.positionInRoot()
                            val centerPos =
                                Offset(
                                    x = posInRoot.x + coordinates.size.width / 2f,
                                    y = posInRoot.y + coordinates.size.height / 2f,
                                )
                            onPositioned(centerPos)
                        },
            )
        }
    }
}

@Preview
@Composable
private fun LivetalkChatCheeringBarPreviewShimmer() {
    LivetalkChatCheeringBar(
        team = Team.HH,
        cheeringCount = null,
        onCheeringClick = {},
        onPositioned = {},
    )
}

@Preview
@Composable
private fun LivetalkChatCheeringBarPreview() {
    LivetalkChatCheeringBar(
        team = Team.HH,
        cheeringCount = 12345L,
        onCheeringClick = {},
        onPositioned = {},
    )
}
