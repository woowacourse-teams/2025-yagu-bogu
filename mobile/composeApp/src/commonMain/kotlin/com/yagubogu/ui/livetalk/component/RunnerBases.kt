package com.yagubogu.ui.livetalk.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yagubogu.ui.common.component.DiamondShape
import com.yagubogu.ui.livetalk.model.BasesUiModel
import com.yagubogu.ui.theme.Gray300
import com.yagubogu.ui.theme.Yellow

@Composable
fun RunnerBases(
    bases: BasesUiModel?,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy((-8).dp),
        modifier = modifier,
    ) {
        Base(isOccupied = bases?.isSecondBaseOccupied ?: false)
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Base(isOccupied = bases?.isThirdBaseOccupied ?: false)
            Base(isOccupied = bases?.isFirstBaseOccupied ?: false)
        }
    }
}

@Composable
private fun Base(
    isOccupied: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(20.dp)
                .background(
                    color = if (isOccupied) Yellow else Gray300,
                    shape = DiamondShape,
                ),
    )
}

@Preview(showBackground = true)
@Composable
private fun RunnerBasesPreview() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(20.dp),
    ) {
        RunnerBases(bases = null)

        RunnerBases(
            bases =
                BasesUiModel(
                    isFirstBaseOccupied = true,
                    isSecondBaseOccupied = false,
                    isThirdBaseOccupied = false,
                ),
        )

        RunnerBases(
            bases =
                BasesUiModel(
                    isFirstBaseOccupied = true,
                    isSecondBaseOccupied = false,
                    isThirdBaseOccupied = true,
                ),
        )

        RunnerBases(
            bases =
                BasesUiModel(
                    isFirstBaseOccupied = true,
                    isSecondBaseOccupied = true,
                    isThirdBaseOccupied = true,
                ),
        )
    }
}
