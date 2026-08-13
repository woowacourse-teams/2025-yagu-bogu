package com.yagubogu.ui.attendance.detail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yagubogu.ui.attendance.detail.model.HITTER_RECORDS
import com.yagubogu.ui.attendance.detail.model.PlayerRecordUiModel
import com.yagubogu.ui.theme.Gray300
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.PretendardMedium
import com.yagubogu.ui.theme.PretendardMedium16
import com.yagubogu.ui.theme.PretendardRegular
import com.yagubogu.ui.theme.PretendardSemiBold
import com.yagubogu.ui.theme.Primary050
import com.yagubogu.ui.theme.Primary900
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.theme.dpToSp
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.attendance_detail_at_bats
import yagubogu.composeapp.generated.resources.attendance_detail_game_record_update
import yagubogu.composeapp.generated.resources.attendance_detail_hits
import yagubogu.composeapp.generated.resources.attendance_detail_hitter_name
import yagubogu.composeapp.generated.resources.attendance_detail_rbi
import yagubogu.composeapp.generated.resources.attendance_detail_runs

@Composable
fun HitterRecordTable(
    hitters: List<PlayerRecordUiModel.HitterRecord>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        HorizontalDivider(color = Gray300, thickness = 0.4.dp)
        HitterRecordHeader()
        HorizontalDivider(color = Gray300, thickness = 0.4.dp)

        when (hitters.isEmpty()) {
            true ->
                Text(
                    text = stringResource(Res.string.attendance_detail_game_record_update),
                    style = PretendardMedium16.copy(color = Gray400),
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                )

            false -> {
                hitters.forEachIndexed { index: Int, hitter: PlayerRecordUiModel.HitterRecord ->
                    val isSameAsPrevious =
                        index > 0 && hitters[index - 1].battingOrder == hitter.battingOrder
                    val displayBattingOrder =
                        if (isSameAsPrevious) "" else hitter.battingOrder.toString()

                    HitterRecordRow(
                        hitter = hitter,
                        battingOrder = displayBattingOrder,
                    )
                    HorizontalDivider(color = Gray300, thickness = 0.4.dp)
                }
            }
        }
    }
}

@Composable
private fun HitterRecordHeader(modifier: Modifier = Modifier) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(Primary050)
                .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(Res.string.attendance_detail_hitter_name),
            style = PretendardSemiBold.copy(fontSize = 14.dpToSp, color = Primary900),
            modifier = Modifier.weight(2.8f).padding(start = 8.dp),
        )

        HitterRecordText(
            text = stringResource(Res.string.attendance_detail_at_bats),
            modifier = Modifier.weight(1f),
        )
        HitterRecordText(
            text = stringResource(Res.string.attendance_detail_hits),
            modifier = Modifier.weight(1f),
        )
        HitterRecordText(
            text = stringResource(Res.string.attendance_detail_rbi),
            modifier = Modifier.weight(1f),
        )
        HitterRecordText(
            text = stringResource(Res.string.attendance_detail_runs),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun HitterRecordRow(
    hitter: PlayerRecordUiModel.HitterRecord,
    battingOrder: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(White)
                .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(2.8f),
        ) {
            Text(
                text = battingOrder,
                style = PretendardRegular.copy(fontSize = 12.dpToSp, textAlign = TextAlign.Center),
                modifier = Modifier.width(20.dp),
            )
            Text(
                text = hitter.playerName,
                style = PretendardMedium.copy(fontSize = 14.dpToSp),
            )
            Text(
                text = hitter.position,
                style = PretendardRegular.copy(fontSize = 10.dpToSp, color = Gray500),
                modifier = Modifier.weight(1f),
            )
        }

        HitterRecordText(
            text = hitter.atBats.toString(),
            modifier = Modifier.weight(1f),
        )
        HitterRecordText(
            text = hitter.hits.toString(),
            modifier = Modifier.weight(1f),
        )
        HitterRecordText(
            text = hitter.rbi.toString(),
            modifier = Modifier.weight(1f),
        )
        HitterRecordText(
            text = hitter.runs.toString(),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun HitterRecordText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = PretendardRegular.copy(fontSize = 12.dpToSp, textAlign = TextAlign.Center),
        modifier = modifier,
    )
}

@Preview
@Composable
private fun HitterRecordTablePreview() {
    HitterRecordTable(hitters = HITTER_RECORDS)
}

@Preview(showBackground = true)
@Composable
private fun EmptyHitterRecordTablePreview() {
    HitterRecordTable(hitters = emptyList())
}
