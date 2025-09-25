package com.yagubogu.presentation.livetalk.chat

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.yagubogu.R
import kotlin.random.Random

/**
 * 터치한 위치에서 하트가 떠오르는 애니메이션을 표시하는 커스텀 뷰.
 */
class FloatingHeartsView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : FrameLayout(context, attrs, defStyleAttr) {
        private val heartPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = ContextCompat.getColor(context, R.color.red)
                textSize = 60f
                textAlign = Paint.Align.CENTER
            }

        init {
            // onDraw가 호출되도록 설정
            setWillNotDraw(false)
        }

//        /**
//         * 특정 뷰(anchor)의 위치에서 하트 애니메이션을 시작합니다.
//         * @param anchor 애니메이션이 시작될 기준 뷰
//         */
//        fun addHeartFrom(anchor: View) {
//            // 뷰의 크기가 아직 측정되지 않았다면, 측정이 끝난 후 재시도합니다.
//            if (width == 0 || height == 0 || anchor.width == 0 || anchor.height == 0) {
//                post { addHeartFrom(anchor) }
//                return
//            }
//
//            val selfOnScreen = IntArray(2)
//            getLocationOnScreen(selfOnScreen)
//
//            val anchorOnScreen = IntArray(2)
//            anchor.getLocationOnScreen(anchorOnScreen)
//
//            // anchor 뷰의 중심 좌표를 이 View 내부의 상대 좌표로 변환합니다.
//            val startX = (anchorOnScreen[0] - selfOnScreen[0]) + anchor.width / 2f
//            val startY = (anchorOnScreen[1] - selfOnScreen[1]) + anchor.height / 2f
//
//            addHeart(startX, startY)
//        }

        /**
         * 지정된 상대 좌표에서 하트 애니메이션을 시작합니다.
         * @param startX 이 View 내에서의 X 시작 좌표
         * @param startY 이 View 내에서의 Y 시작 좌표
         */
        fun addCheerEmoji(
            emoji: String,
            startX: Float,
            startY: Float,
        ) {
            val bounds = Rect()
            heartPaint.getTextBounds(emoji, 0, emoji.length, bounds)
            val textH = bounds.height().toFloat()

            val heartView =
                object : View(context) {
                    override fun onDraw(canvas: Canvas) {
                        // 뷰의 중앙에 이모지를 그립니다.
                        val y = height / 2f + textH / 2f
                        canvas.drawText(emoji, width / 2f, y, heartPaint)
                    }
                }.apply {
                    // 이모지 크기에 맞춰 뷰의 크기를 설정합니다.
                    val textW = heartPaint.measureText(emoji)
                    val padding = dpToPx(8f)
                    val viewW = (textW + padding * 2).toInt()
                    val viewH = (textH + padding * 2).toInt()
                    layoutParams = LayoutParams(viewW, viewH)

                    // 깜빡임 방지를 위해 초기에는 투명하게 설정합니다.
                    alpha = 0f
                }

            addView(heartView)

            // 뷰가 레이아웃에 추가된 후 위치를 설정하고 애니메이션을 시작합니다.
            heartView.post {
                heartView.x = startX - heartView.width / 2f
                heartView.y = startY - heartView.height / 2f
                animateHeart(heartView)
            }
        }

        private fun animateHeart(heartView: View) {
            val animatorSet = AnimatorSet()

            // 나타나면서 커지는 애니메이션
            val scaleX =
                ObjectAnimator.ofFloat(heartView, View.SCALE_X, 0.5f, 1.2f, 1f).apply { duration = 300 }
            val scaleY =
                ObjectAnimator.ofFloat(heartView, View.SCALE_Y, 0.5f, 1.2f, 1f).apply { duration = 300 }
            val fadeIn = ObjectAnimator.ofFloat(heartView, View.ALPHA, 0f, 1f).apply { duration = 300 }

            // 나타나고 0.5초 뒤부터 사라지는 애니메이션
            val fadeOut =
                ObjectAnimator.ofFloat(heartView, View.ALPHA, 1f, 0f).apply {
                    duration = 1500
                    startDelay = 500
                }

            // 위로 떠오르면서 좌우로 흔들리는 애니메이션
            val finalY = heartView.y - (height.toFloat() / 2) - (Random.nextFloat() * height / 4)
            val positionY =
                ObjectAnimator.ofFloat(heartView, View.Y, heartView.y, finalY).apply {
                    duration = 2000
                    interpolator = AccelerateInterpolator()
                }

            val finalX = heartView.x + ((Random.nextFloat() - 0.5f) * width / 4)
            val positionX =
                ObjectAnimator.ofFloat(heartView, View.X, heartView.x, finalX).apply {
                    duration = 2000
                    interpolator = DecelerateInterpolator()
                }

            // 살짝 회전하는 애니메이션
            val rotation =
                ObjectAnimator
                    .ofFloat(heartView, View.ROTATION, 0f, (Random.nextFloat() - 0.5f) * 30)
                    .apply {
                        duration = 2000
                    }

            animatorSet.playTogether(scaleX, scaleY, fadeIn, fadeOut, positionY, positionX, rotation)
            animatorSet.addListener(
                object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        // 애니메이션이 끝나면 뷰를 제거합니다.
                        removeView(heartView)
                    }
                },
            )
            animatorSet.start()
        }

        private fun dpToPx(dp: Float): Float = dp * resources.displayMetrics.density
    }
