package com.yagubogu.ui.common.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.yagubogu.ui.util.noRippleClickable
import org.jetbrains.compose.resources.painterResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.ic_refresh

@Composable
fun RefreshIcon(
    onRefresh: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier,
) {
    var rotation: Float by remember { mutableFloatStateOf(0f) }
    val animatedRotation: Float by animateFloatAsState(
        targetValue = rotation,
        animationSpec = tween(durationMillis = 1_000),
    )

    Icon(
        painter = painterResource(Res.drawable.ic_refresh),
        contentDescription = null,
        tint = color,
        modifier =
            modifier
                .padding(horizontal = 4.dp)
                .size(20.dp)
                .graphicsLayer {
                    rotationZ = animatedRotation
                }.noRippleClickable {
                    rotation += 360f
                    onRefresh()
                },
    )
}
