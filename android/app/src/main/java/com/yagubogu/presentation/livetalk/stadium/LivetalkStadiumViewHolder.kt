package com.yagubogu.presentation.livetalk.stadium

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yagubogu.databinding.ItemLivetalkStadiumBinding
import com.yagubogu.presentation.livetalk.LivetalkChatActivity

class LivetalkStadiumViewHolder(
    private val binding: ItemLivetalkStadiumBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: LivetalkStadiumItem) {
        binding.livetalkStadiumItem = item
        binding.constraintLivetalkStadium.setOnClickListener {
            // Todo: 구장별 채팅 연동 필요
            val intent = Intent(binding.root.context, LivetalkChatActivity::class.java)
            binding.root.context.startActivity(intent)
        }
    }

    companion object {
        fun from(parent: ViewGroup): LivetalkStadiumViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemLivetalkStadiumBinding.inflate(inflater, parent, false)
            return LivetalkStadiumViewHolder(binding)
        }
    }
}
