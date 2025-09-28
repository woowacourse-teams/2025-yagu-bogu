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
 * 터치한 위치에서 이모지가 떠오르는 애니메이션을 표시하는 커스텀 뷰.
 */
class FloatingLiveTalkEmojiView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : FrameLayout(context, attrs, defStyleAttr) {
        private val emojiPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = ContextCompat.getColor(context, R.color.red)
                textSize = 60f
                textAlign = Paint.Align.CENTER
            }

        init {
            setWillNotDraw(false)
        }

        /**
         * 지정된 상대 좌표에서 이모지 애니메이션을 시작합니다.
         * @param startX 이 View 내에서의 X 시작 좌표
         * @param startY 이 View 내에서의 Y 시작 좌표
         */
        fun addCheerEmoji(
            emoji: String,
            startX: Float,
            startY: Float,
        ) {
            val bounds = Rect()
            emojiPaint.getTextBounds(emoji, 0, emoji.length, bounds)
            val textH = bounds.height().toFloat()

            val emojiView =
                object : View(context) {
                    override fun onDraw(canvas: Canvas) {
                        // 뷰의 중앙에 이모지를 그립니다.
                        val y = height / 2f + textH / 2f
                        canvas.drawText(emoji, width / 2f, y, emojiPaint)
                    }
                }.apply {
                    // 이모지 크기에 맞춰 뷰의 크기를 설정합니다.
                    val textW = emojiPaint.measureText(emoji)
                    val padding = dpToPx(8f)
                    val viewW = (textW + padding * 2).toInt()
                    val viewH = (textH + padding * 2).toInt()
                    layoutParams = LayoutParams(viewW, viewH)

                    // 깜빡임 방지를 위해 초기에는 투명하게 설정합니다.
                    alpha = 0f
                }

            addView(emojiView)

            // 뷰가 레이아웃에 추가된 후 위치를 설정하고 애니메이션을 시작합니다.
            emojiView.post {
                emojiView.x = startX - emojiView.width / 2f
                emojiView.y = startY - emojiView.height / 2f
                animateEmoji(emojiView)
            }
        }

        private fun animateEmoji(emojiView: View) {
            val animatorSet = AnimatorSet()

            // 나타나면서 커지는 애니메이션
            val scaleX =
                ObjectAnimator.ofFloat(emojiView, View.SCALE_X, 0.5f, 1.2f, 1f).apply { duration = 300 }
            val scaleY =
                ObjectAnimator.ofFloat(emojiView, View.SCALE_Y, 0.5f, 1.2f, 1f).apply { duration = 300 }
            val fadeIn = ObjectAnimator.ofFloat(emojiView, View.ALPHA, 0f, 1f).apply { duration = 300 }

            // 나타나고 0.5초 뒤부터 사라지는 애니메이션
            val fadeOut =
                ObjectAnimator.ofFloat(emojiView, View.ALPHA, 1f, 0f).apply {
                    duration = 1500
                    startDelay = 500
                }

            // 위로 떠오르면서 좌우로 흔들리는 애니메이션
            val finalY = emojiView.y - (height.toFloat() / 2) - (Random.nextFloat() * height / 4)
            val positionY =
                ObjectAnimator.ofFloat(emojiView, View.Y, emojiView.y, finalY).apply {
                    duration = 2000
                    interpolator = AccelerateInterpolator()
                }

            val finalX = emojiView.x + ((Random.nextFloat() - 0.5f) * width / 4)
            val positionX =
                ObjectAnimator.ofFloat(emojiView, View.X, emojiView.x, finalX).apply {
                    duration = 2000
                    interpolator = DecelerateInterpolator()
                }

            // 살짝 회전하는 애니메이션
            val rotation =
                ObjectAnimator
                    .ofFloat(emojiView, View.ROTATION, 0f, (Random.nextFloat() - 0.5f) * 30)
                    .apply {
                        duration = 2000
                    }

            animatorSet.playTogether(scaleX, scaleY, fadeIn, fadeOut, positionY, positionX, rotation)
            animatorSet.addListener(
                object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        // 애니메이션이 끝나면 뷰를 제거합니다.
                        removeView(emojiView)
                    }
                },
            )
            animatorSet.start()
        }

        private fun dpToPx(dp: Float): Float = dp * resources.displayMetrics.density
    }
