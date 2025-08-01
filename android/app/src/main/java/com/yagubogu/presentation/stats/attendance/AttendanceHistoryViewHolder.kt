package com.yagubogu.presentation.stats.attendance

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yagubogu.databinding.ItemAttendanceHistoryBinding

class AttendanceHistoryViewHolder(
    private val binding: ItemAttendanceHistoryBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: AttendanceHistoryItem) {
        binding.attendanceHistoryItem = item
    }

    companion object {
        fun from(parent: ViewGroup): AttendanceHistoryViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemAttendanceHistoryBinding.inflate(inflater, parent, false)
            return AttendanceHistoryViewHolder(binding)
        }
    }
}
