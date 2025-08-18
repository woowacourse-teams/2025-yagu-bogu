package com.yagubogu.presentation.attendance

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yagubogu.databinding.ItemAttendanceHistoryDetailBinding
import com.yagubogu.presentation.attendance.model.AttendanceHistoryItem

class AttendanceHistoryDetailViewHolder(
    private val binding: ItemAttendanceHistoryDetailBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: AttendanceHistoryItem.Detail) {
        binding.layoutAttendanceHistory.attendanceHistoryItem = item.summary
    }

    companion object {
        fun from(parent: ViewGroup): AttendanceHistoryDetailViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemAttendanceHistoryDetailBinding.inflate(inflater, parent, false)
            return AttendanceHistoryDetailViewHolder(binding)
        }
    }
}
