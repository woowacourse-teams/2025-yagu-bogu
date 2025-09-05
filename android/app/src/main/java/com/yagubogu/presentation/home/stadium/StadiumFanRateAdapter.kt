package com.yagubogu.presentation.home.stadium

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

class StadiumFanRateAdapter : ListAdapter<StadiumFanRateItem, StadiumFanRateViewHolder>(diffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): StadiumFanRateViewHolder = StadiumFanRateViewHolder.from(parent)

    override fun onBindViewHolder(
        holder: StadiumFanRateViewHolder,
        position: Int,
    ) {
        val item: StadiumFanRateItem = getItem(position)
        holder.bind(item)
    }

    companion object {
        private val diffCallback =
            object : DiffUtil.ItemCallback<StadiumFanRateItem>() {
                override fun areItemsTheSame(
                    oldItem: StadiumFanRateItem,
                    newItem: StadiumFanRateItem,
                ): Boolean = oldItem.awayTeamFanRate.team == newItem.awayTeamFanRate.team

                override fun areContentsTheSame(
                    oldItem: StadiumFanRateItem,
                    newItem: StadiumFanRateItem,
                ): Boolean = oldItem == newItem
            }
    }
}
