package com.yagubogu.presentation.attendance

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yagubogu.databinding.ItemAttendanceHistoryDetailBinding
import com.yagubogu.presentation.attendance.model.AttendanceHistoryItem

class AttendanceHistoryDetailViewHolder private constructor(
    private val binding: ItemAttendanceHistoryDetailBinding,
    handler: Handler,
) : RecyclerView.ViewHolder(binding.root) {
    init {
        binding.handler = handler
    }

    fun bind(item: AttendanceHistoryItem.Detail) {
        binding.attendanceHistoryItem = item
        binding.bindingPosition = bindingAdapterPosition
        binding.tvAwayTeamPitcher.text =
            itemView.context.getString(item.awayTeamPitcherRes, item.awayTeamPitcherName)
        binding.tvHomeTeamPitcher.text =
            itemView.context.getString(item.homeTeamPitcherRes, item.homeTeamPitcherName)
    }

    interface Handler {
        fun onDetailItemClick(position: Int)
    }

    companion object {
        fun from(
            parent: ViewGroup,
            handler: Handler,
        ): AttendanceHistoryDetailViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemAttendanceHistoryDetailBinding.inflate(inflater, parent, false)
            return AttendanceHistoryDetailViewHolder(binding, handler)
        }
    }
}
