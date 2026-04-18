package com.yagubogu.ui.favorite

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yagubogu.domain.model.Team
import com.yagubogu.ui.common.component.DefaultDialog
import com.yagubogu.ui.theme.EsamanruMedium
import com.yagubogu.ui.theme.EsamanruMedium20
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.Gray100
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.theme.PretendardMedium16
import com.yagubogu.ui.theme.PretendardSemiBold16
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.util.mascot
import com.yagubogu.ui.util.noRippleClickable
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.all_cancel
import yagubogu.composeapp.generated.resources.all_confirm
import yagubogu.composeapp.generated.resources.favorite_team_random
import yagubogu.composeapp.generated.resources.favorite_team_selection
import yagubogu.composeapp.generated.resources.favorite_team_selection_confirm
import yagubogu.composeapp.generated.resources.img_mascot_ht

@Composable
fun FavoriteTeamScreen(
    onFavoriteTeamUpdate: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FavoriteTeamViewModel = koinViewModel(),
) {
    val teams: List<Team> = remember { Team.entries.shuffled() }
    var selectedTeam: Team? by rememberSaveable { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        viewModel.favoriteTeamUpdateEvent.collect {
            onFavoriteTeamUpdate()
        }
    }

    Scaffold(containerColor = Gray050) { innerPadding ->
        FavoriteTeamScreen(
            teams = teams,
            onTeamClick = { item -> selectedTeam = item },
            modifier = modifier.padding(innerPadding),
        )
    }

    selectedTeam?.let { team: Team ->
        FavoriteTeamDialog(
            mascot = team.mascot,
            teamName = team.shortname,
            onConfirm = {
                viewModel.saveFavoriteTeam(team)
                selectedTeam = null
            },
            onCancel = { selectedTeam = null },
            modifier = modifier,
        )
    }
}

@Composable
private fun FavoriteTeamScreen(
    teams: List<Team>,
    onTeamClick: (Team) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Gray050),
    ) {
        Text(
            text = stringResource(Res.string.favorite_team_selection),
            style = EsamanruMedium,
            fontSize = 32.sp,
            modifier =
                Modifier
                    .padding(horizontal = 30.dp)
                    .padding(top = 20.dp),
        )
        Text(
            text = stringResource(Res.string.favorite_team_random),
            style = PretendardMedium16.copy(color = Gray400),
            modifier =
                Modifier
                    .padding(horizontal = 30.dp)
                    .padding(top = 4.dp),
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(all = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(teams) { team: Team ->
                FavoriteTeamItem(
                    team = team,
                    onClick = { onTeamClick(team) },
                )
            }
        }
    }
}

@Composable
private fun FavoriteTeamItem(
    team: Team,
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
                .padding(vertical = 16.dp)
                .noRippleClickable { onClick() },
    ) {
        Image(
            painter = painterResource(team.mascot),
            contentDescription = null,
            modifier = Modifier.size(60.dp),
        )

        Text(
            text = team.shortname,
            style = PretendardSemiBold16,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun FavoriteTeamDialog(
    mascot: DrawableResource,
    teamName: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DefaultDialog(
        negativeText = stringResource(Res.string.all_cancel),
        positiveText = stringResource(Res.string.all_confirm),
        onConfirm = onConfirm,
        onCancel = onCancel,
        modifier = modifier,
    ) {
        Text(
            text = stringResource(Res.string.favorite_team_selection_confirm),
            style = EsamanruMedium20,
        )
        Spacer(modifier = Modifier.height(20.dp))
        Image(
            painter = painterResource(mascot),
            contentDescription = null,
            modifier = Modifier.size(60.dp),
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
        teams = Team.entries,
        onTeamClick = {},
    )
}

@Preview
@Composable
private fun FavoriteTeamDialogPreview() {
    FavoriteTeamDialog(
        mascot = Res.drawable.img_mascot_ht,
        teamName = "KIA",
        onConfirm = {},
        onCancel = {},
    )
}
