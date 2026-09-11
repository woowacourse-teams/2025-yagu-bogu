package com.yagubogu.ui.common.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.theme.Primary500
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.util.rememberNoRippleInteractionSource

private val TRACK_HEIGHT = 20.dp
private val TRACK_PADDING = 2.dp
private val KNOB_SIZE = 16.dp
private const val ANIMATION_DURATION_MILLIS = 200

@Composable
fun ToggleSwitch(
    isOn: Boolean,
    onClick: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val knobOffset: Dp by animateDpAsState(
        targetValue = if (isOn) KNOB_SIZE else 0.dp,
        animationSpec = tween(durationMillis = ANIMATION_DURATION_MILLIS),
    )
    val trackColor: Color by animateColorAsState(
        targetValue = if (isOn) Primary500 else Gray400,
        animationSpec = tween(durationMillis = ANIMATION_DURATION_MILLIS),
    )

    Box(
        contentAlignment = Alignment.CenterStart,
        modifier =
            modifier
                .size(width = (KNOB_SIZE + TRACK_PADDING) * 2, height = TRACK_HEIGHT)
                .clip(CircleShape)
                .background(color = trackColor)
                .toggleable(
                    value = isOn,
                    onValueChange = onClick,
                    role = Role.Switch,
                    interactionSource = rememberNoRippleInteractionSource(),
                    indication = null,
                ).padding(TRACK_PADDING),
    ) {
        Box(
            modifier =
                Modifier
                    .offset(x = knobOffset)
                    .size(KNOB_SIZE)
                    .clip(CircleShape)
                    .background(color = White),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ToggleSwitchPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.padding(20.dp),
    ) {
        ToggleSwitch(isOn = true, onClick = {})
        ToggleSwitch(isOn = false, onClick = {})
    }
}
