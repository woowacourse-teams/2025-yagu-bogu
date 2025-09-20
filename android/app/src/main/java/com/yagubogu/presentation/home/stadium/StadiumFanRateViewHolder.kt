package com.yagubogu.presentation.home.stadium

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yagubogu.databinding.ItemStadiumFanRateBinding
import kotlin.random.Random

class StadiumFanRateViewHolder private constructor(
    private val binding: ItemStadiumFanRateBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: StadiumFanRateItem) {
        binding.stadiumFanRate = item
    }

    companion object {
        fun from(parent: ViewGroup): StadiumFanRateViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemStadiumFanRateBinding.inflate(inflater, parent, false)
            binding.tvVsDesign.startHeartbeatAnimation()
            return StadiumFanRateViewHolder(binding)
        }

        // 심장 박동처럼 움직이는 애니메이션 함수
        private fun View.startHeartbeatAnimation() {
            val expandStrongX = ObjectAnimator.ofFloat(this, "scaleX", 1f, 1.2f).setDuration(150)
            val expandStrongY = ObjectAnimator.ofFloat(this, "scaleY", 1f, 1.2f).setDuration(150)
            val contractStrongX = ObjectAnimator.ofFloat(this, "scaleX", 1.2f, 1f).setDuration(150)
            val contractStrongY = ObjectAnimator.ofFloat(this, "scaleY", 1.2f, 1f).setDuration(150)

            val strongBeat = AnimatorSet().apply { playTogether(expandStrongX, expandStrongY) }
            val strongRelease =
                AnimatorSet().apply { playTogether(contractStrongX, contractStrongY) }

            val expandSoftX = ObjectAnimator.ofFloat(this, "scaleX", 1f, 1.15f).setDuration(150)
            val expandSoftY = ObjectAnimator.ofFloat(this, "scaleY", 1f, 1.15f).setDuration(150)
            val contractSoftX = ObjectAnimator.ofFloat(this, "scaleX", 1.15f, 1f).setDuration(150)
            val contractSoftY = ObjectAnimator.ofFloat(this, "scaleY", 1.15f, 1f).setDuration(150)

            val softBeat = AnimatorSet().apply { playTogether(expandSoftX, expandSoftY) }
            val softRelease = AnimatorSet().apply { playTogether(contractSoftX, contractSoftY) }

            val fullBeat =
                AnimatorSet().apply {
                    playSequentially(strongBeat, strongRelease, softBeat, softRelease)
                    addListener(
                        object : android.animation.AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                // 2.2초 정도 쉬었다가 다시 실행
                                postDelayed({ startHeartbeatAnimation() }, 2200)
                            }
                        },
                    )
                }

            // 0~1초 사이 랜덤 딜레이 후 실행
            val randomDelay = Random.nextLong(0, 1000)
            postDelayed({ fullBeat.start() }, randomDelay)
        }
    }
}
