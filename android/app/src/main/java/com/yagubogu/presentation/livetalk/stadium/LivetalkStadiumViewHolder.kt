package com.yagubogu.presentation.livetalk.stadium

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yagubogu.databinding.ItemLivetalkStadiumBinding

class LivetalkStadiumViewHolder(
    private val binding: ItemLivetalkStadiumBinding,
    private val handler: Handler,
) : RecyclerView.ViewHolder(binding.root) {
    init {
        binding.handler = handler
    }

    fun bind(item: LivetalkStadiumItem) {
        binding.livetalkStadiumItem = item
    }

    interface Handler {
        fun onItemClick(item: LivetalkStadiumItem)
    }

    companion object {
        fun from(
            parent: ViewGroup,
            handler: Handler,
        ): LivetalkStadiumViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemLivetalkStadiumBinding.inflate(inflater, parent, false)
            return LivetalkStadiumViewHolder(binding, handler)
        }
    }
}
