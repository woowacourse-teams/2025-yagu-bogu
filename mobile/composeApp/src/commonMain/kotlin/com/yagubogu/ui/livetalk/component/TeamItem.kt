package com.yagubogu.ui.livetalk.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.yagubogu.domain.model.Team
import com.yagubogu.ui.theme.PretendardSemiBold12
import com.yagubogu.ui.util.color
import com.yagubogu.ui.util.mascot
import org.jetbrains.compose.resources.painterResource

@Composable
fun TeamItem(
    team: Team,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(team.mascot),
            contentDescription = null,
            modifier =
                Modifier
                    .clip(CircleShape)
                    .size(52.dp)
                    .background(team.color.copy(alpha = 0.2f))
                    .padding(8.dp),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = team.shortname,
            style = PretendardSemiBold12,
        )
    }
}
