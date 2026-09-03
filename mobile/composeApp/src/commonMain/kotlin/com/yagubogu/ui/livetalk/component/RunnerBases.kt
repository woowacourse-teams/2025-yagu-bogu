package com.yagubogu.ui.livetalk.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        verticalArrangement = Arrangement.spacedBy((-6).dp),
        modifier = modifier,
    ) {
        Base(isOccupied = bases?.isSecondBaseOccupied ?: false)
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
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
                .size(14.dp)
                .background(
                    color = if (isOccupied) Yellow else Gray300,
                    shape = DiamondShape,
                ),
    )
}
