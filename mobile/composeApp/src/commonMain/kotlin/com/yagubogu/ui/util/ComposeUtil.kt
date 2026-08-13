package com.yagubogu.ui.util

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import com.yagubogu.ui.theme.Gray200
import com.yagubogu.ui.theme.Gray800
import com.yagubogu.ui.theme.PretendardRegular12
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

private const val TOOLTIP_ANIMATION_DURATION_MS = 400

@Composable
fun rememberNoRippleInteractionSource(): MutableInteractionSource =
    remember {
        object : MutableInteractionSource {
            override val interactions: Flow<Interaction> = emptyFlow()

            override suspend fun emit(interaction: Interaction) {}

            override fun tryEmit(interaction: Interaction) = true
        }
    }

/**
 * 아이콘 아래에 툴팁을 표시하는 Composable입니다.
 *
 * 화살표가 위를 향하며 [text] 내용을 담은 툴팁이 앵커 아이콘 아래에 나타났다 사라지는
 * 애니메이션과 함께 표시됩니다. [content] 블록에서 제공되는 [showTooltip] 콜백을
 * 호출하면 툴팁이 표시되고, 외부 영역 터치 시 사라집니다.
 *
 * @param text 툴팁에 표시할 텍스트
 * @param modifier 앵커 영역에 적용할 Modifier
 * @param content 앵커가 될 컨텐츠. showTooltip 콜백을 클릭 이벤트에 연결하세요.
 */
@Composable
fun BalloonTooltip(
    text: String,
    modifier: Modifier = Modifier,
    content: @Composable (showTooltip: () -> Unit) -> Unit,
) {
    var isVisible by remember { mutableStateOf(false) }
    val transitionState = remember { MutableTransitionState(false) }
    transitionState.targetState = isVisible

    val positionProvider =
        remember {
            object : PopupPositionProvider {
                override fun calculatePosition(
                    anchorBounds: IntRect,
                    windowSize: IntSize,
                    layoutDirection: LayoutDirection,
                    popupContentSize: IntSize,
                ): IntOffset =
                    IntOffset(
                        x = anchorBounds.left + (anchorBounds.width - popupContentSize.width) / 2,
                        y = anchorBounds.bottom,
                    )
            }
        }

    Box(modifier = modifier) {
        content { isVisible = !isVisible }

        if (transitionState.currentState || transitionState.targetState) {
            Popup(
                popupPositionProvider = positionProvider,
                onDismissRequest = { isVisible = false },
            ) {
                AnimatedVisibility(
                    visibleState = transitionState,
                    enter = fadeIn(tween(TOOLTIP_ANIMATION_DURATION_MS)),
                    exit = fadeOut(tween(TOOLTIP_ANIMATION_DURATION_MS)),
                ) {
                    BoxWithConstraints {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(maxWidth * 0.5f),
                        ) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Canvas(modifier = Modifier.width(12.dp).height(6.dp)) {
                                val path =
                                    Path().apply {
                                        moveTo(size.width / 2f, 0f)
                                        lineTo(size.width, size.height)
                                        lineTo(0f, size.height)
                                        close()
                                    }
                                drawPath(path = path, color = Gray200)
                            }
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .background(Gray200, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                            ) {
                                Text(
                                    text = text,
                                    style = PretendardRegular12.copy(color = Gray800),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Figma의 CSS box-shadow 스펙을 Compose의 [Shadow]로 변환합니다.
 *
 * Compose의 [Shadow.blurRadius]는 Gaussian blur의 sigma값을 사용하는 반면,
 * CSS/Figma는 sigma의 2배 값을 blur로 표현하기 때문에 변환이 필요합니다.
 *
 * @param color 그림자 색상
 * @param offsetX 그림자 X축 오프셋 (Figma 스펙값 그대로 입력)
 * @param offsetY 그림자 Y축 오프셋 (Figma 스펙값 그대로 입력)
 * @param blur 그림자 번짐 정도 (Figma 스펙값 그대로 입력, 내부적으로 sigma 변환 적용)
 */
@Composable
fun cssShadow(
    color: Color,
    offsetX: Dp = 0.dp,
    offsetY: Dp = 0.dp,
    blur: Dp = 0.dp,
): Shadow {
    val density = LocalDensity.current
    return with(density) {
        Shadow(
            color = color,
            offset =
                Offset(
                    x = offsetX.toPx(),
                    y = offsetY.toPx(),
                ),
            blurRadius = blur.toPx() / 2f,
        )
    }
}
