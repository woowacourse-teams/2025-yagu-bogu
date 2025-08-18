package com.yagubogu.presentation.attendance

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yagubogu.databinding.ItemAttendanceHistorySummaryBinding
import com.yagubogu.presentation.attendance.model.AttendanceHistoryItem

class AttendanceHistoryViewHolder(
    private val binding: ItemAttendanceHistorySummaryBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: AttendanceHistoryItem) {
        binding.layoutAttendanceHistory.attendanceHistoryItem = item
    }

    companion object {
        fun from(parent: ViewGroup): AttendanceHistoryViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemAttendanceHistorySummaryBinding.inflate(inflater, parent, false)
            return AttendanceHistoryViewHolder(binding)
        }
    }
}
