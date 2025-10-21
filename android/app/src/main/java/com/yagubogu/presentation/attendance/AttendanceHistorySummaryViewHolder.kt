package com.yagubogu.presentation.attendance

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yagubogu.databinding.ItemAttendanceHistorySummaryBinding
import com.yagubogu.presentation.attendance.model.AttendanceHistoryItem

class AttendanceHistorySummaryViewHolder private constructor(
    private val binding: ItemAttendanceHistorySummaryBinding,
    handler: Handler,
) : RecyclerView.ViewHolder(binding.root) {
    init {
        binding.handler = handler
    }

    fun bind(item: AttendanceHistoryItem.Summary) {
        binding.attendanceHistoryItem = item
    }

    interface Handler {
        fun onSummaryItemClick(item: AttendanceHistoryItem.Summary)
    }

    companion object {
        fun from(
            parent: ViewGroup,
            handler: Handler,
        ): AttendanceHistorySummaryViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemAttendanceHistorySummaryBinding.inflate(inflater, parent, false)
            return AttendanceHistorySummaryViewHolder(binding, handler)
        }
    }
}
