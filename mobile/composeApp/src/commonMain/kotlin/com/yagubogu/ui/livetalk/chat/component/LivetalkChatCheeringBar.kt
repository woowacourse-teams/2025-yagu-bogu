package com.yagubogu.ui.livetalk.chat.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yagubogu.domain.model.Team
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.util.mascot
import com.yagubogu.ui.util.noRippleClickable
import com.yagubogu.ui.util.shimmerIf
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LivetalkChatCheeringBar(
    myTeam: Team,
    otherTeam: Team,
    myTeamCheeringCount: Long?,
    otherTeamCheeringCount: Long?,
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
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LiveTalkChatCheeringRateHorizontalBar(
                myTeam = myTeam,
                otherTeam = otherTeam,
                myTeamCheeringCount = myTeamCheeringCount,
                otherTeamCheeringCount = otherTeamCheeringCount,
            )
        }
        Spacer(Modifier.width(8.dp))

        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(myTeam.mascot),
                contentDescription = null,
                modifier =
                    Modifier
                        .size(32.dp)
                        .shimmerIf(myTeamCheeringCount == null)
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
        myTeam = Team.HH,
        otherTeam = Team.HT,
        myTeamCheeringCount = null,
        otherTeamCheeringCount = null,
        onCheeringClick = {},
        onPositioned = {},
    )
}

@Preview
@Composable
private fun LivetalkChatCheeringBarPreview() {
    LivetalkChatCheeringBar(
        myTeam = Team.HH,
        otherTeam = Team.HT,
        myTeamCheeringCount = 12345L,
        otherTeamCheeringCount = 54321L,
        onCheeringClick = {},
        onPositioned = {},
    )
}
