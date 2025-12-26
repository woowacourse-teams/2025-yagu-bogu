package com.yagubogu.ui.livetalk.chat.component

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.yagubogu.R
import com.yagubogu.ui.livetalk.chat.model.LivetalkTeams
import com.yagubogu.ui.theme.Black
import com.yagubogu.ui.theme.Gray050

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LivetalkChatToolbar(
    teams: LivetalkTeams?,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val awayName = teams?.awayTeam?.shortname ?: ""
    val homeName = teams?.homeTeam?.shortname ?: ""
    val matchText =
        stringResource(
            R.string.livetalk_away_home_name,
            awayName,
            homeName,
        )

    CenterAlignedTopAppBar(
        title = {
            LivetalkChatScreenTitle(
                teams?.stadiumName,
                matchText,
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_left),
                    contentDescription = stringResource(R.string.all_back_button_content_description),
                )
            }
        },
        colors =
            TopAppBarColors(
                containerColor = Gray050,
                navigationIconContentColor = Black,
                titleContentColor = Black,
                actionIconContentColor = Black,
                scrolledContainerColor = Gray050,
            ),
        modifier = modifier,
    )
}

// @Preview
// @Composable
// private fun LivetalkChatToolbarPreview() {
//    LivetalkChatToolbar(onBackClick = {}, "고척 스카이돔", "두산 vs 키움")
// }
//
// @Preview
// @Composable
// private fun LivetalkChatToolbarShimmerPreview() {
//    LivetalkChatToolbar(onBackClick = {}, null)
// }
