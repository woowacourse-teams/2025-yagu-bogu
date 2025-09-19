package com.yagubogu.presentation.livetalk.stadium

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

class LivetalkStadiumAdapter(
    private val handler: LivetalkStadiumViewHolder.Handler,
) : ListAdapter<LivetalkStadiumItem, LivetalkStadiumViewHolder>(diffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): LivetalkStadiumViewHolder = LivetalkStadiumViewHolder.from(parent, handler)

    override fun onBindViewHolder(
        holder: LivetalkStadiumViewHolder,
        position: Int,
    ) {
        val item: LivetalkStadiumItem = getItem(position)
        holder.bind(item)
    }

    companion object {
        private val diffCallback =
            object : DiffUtil.ItemCallback<LivetalkStadiumItem>() {
                override fun areItemsTheSame(
                    oldItem: LivetalkStadiumItem,
                    newItem: LivetalkStadiumItem,
                ): Boolean = oldItem.stadiumName == newItem.stadiumName

                override fun areContentsTheSame(
                    oldItem: LivetalkStadiumItem,
                    newItem: LivetalkStadiumItem,
                ): Boolean = oldItem == newItem
            }
    }
}
