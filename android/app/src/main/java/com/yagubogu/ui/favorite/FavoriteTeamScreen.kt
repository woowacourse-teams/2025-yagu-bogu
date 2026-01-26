package com.yagubogu.ui.favorite

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.yagubogu.R
import com.yagubogu.domain.model.Team
import com.yagubogu.ui.common.component.DefaultDialog
import com.yagubogu.ui.theme.EsamanruMedium
import com.yagubogu.ui.theme.EsamanruMedium20
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.Gray100
import com.yagubogu.ui.theme.PretendardSemiBold16
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.util.emoji
import com.yagubogu.ui.util.noRippleClickable

@Composable
fun FavoriteTeamScreen(
    navigateToMain: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FavoriteTeamViewModel = hiltViewModel(),
) {
    val teams: List<FavoriteTeamItem> = Team.entries.map { FavoriteTeamItem.of(it) }
    var selectedTeam: FavoriteTeamItem? by rememberSaveable { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        viewModel.favoriteTeamUpdateEvent.collect {
            navigateToMain()
        }
    }

    Scaffold(containerColor = Gray050) { innerPadding ->
        FavoriteTeamScreen(
            teams = teams,
            onTeamClick = { item -> selectedTeam = item },
            modifier = modifier.padding(innerPadding),
        )
    }

    selectedTeam?.let { team: FavoriteTeamItem ->
        FavoriteTeamDialog(
            emoji = team.team.emoji,
            teamName = team.team.shortname,
            onConfirm = {
                viewModel.saveFavoriteTeam(team.team)
                selectedTeam = null
            },
            onCancel = { selectedTeam = null },
            modifier = modifier,
        )
    }
}

@Composable
private fun FavoriteTeamScreen(
    teams: List<FavoriteTeamItem>,
    onTeamClick: (FavoriteTeamItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Gray050),
    ) {
        Text(
            text = stringResource(R.string.favorite_team_selection),
            style = EsamanruMedium,
            fontSize = 32.sp,
            modifier =
                Modifier
                    .padding(horizontal = 30.dp)
                    .padding(top = 20.dp),
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(all = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(teams) { item: FavoriteTeamItem ->
                FavoriteTeamItem(
                    item = item,
                    onClick = { onTeamClick(item) },
                )
            }
        }
    }
}

@Composable
private fun FavoriteTeamItem(
    item: FavoriteTeamItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            modifier
                .fillMaxWidth()
                .background(White, RoundedCornerShape(12.dp))
                .border(1.dp, Gray100, RoundedCornerShape(12.dp))
                .padding(vertical = 24.dp)
                .noRippleClickable { onClick() },
    ) {
        Text(
            text = item.emoji,
            fontSize = 32.sp,
        )

        Text(
            text = item.team.shortname,
            style = PretendardSemiBold16,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun FavoriteTeamDialog(
    emoji: String,
    teamName: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DefaultDialog(
        negativeText = stringResource(R.string.all_cancel),
        positiveText = stringResource(R.string.all_confirm),
        onConfirm = onConfirm,
        onCancel = onCancel,
        modifier = modifier,
    ) {
        Text(
            text = stringResource(R.string.favorite_team_selection_confirm),
            style = EsamanruMedium20,
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = emoji,
            fontSize = 32.sp,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = teamName,
            style = PretendardSemiBold16,
        )
    }
}

@Preview
@Composable
private fun FavoriteTeamScreenPreview() {
    FavoriteTeamScreen(
        teams = Team.entries.map { FavoriteTeamItem.of(it) },
        onTeamClick = {},
    )
}

@Preview
@Composable
private fun FavoriteTeamDialogPreview() {
    FavoriteTeamDialog(
        emoji = "🐯",
        teamName = "KIA 타이거즈",
        onConfirm = {},
        onCancel = {},
    )
}
