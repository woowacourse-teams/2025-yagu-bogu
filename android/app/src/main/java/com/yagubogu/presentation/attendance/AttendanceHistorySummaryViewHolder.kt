package com.yagubogu.presentation.attendance

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yagubogu.databinding.ItemAttendanceHistorySummaryBinding
import com.yagubogu.presentation.attendance.model.AttendanceHistoryUiModel

class AttendanceHistorySummaryViewHolder private constructor(
    private val binding: ItemAttendanceHistorySummaryBinding,
    handler: Handler,
) : RecyclerView.ViewHolder(binding.root) {
    init {
        binding.handler = handler
    }

    fun bind(item: AttendanceHistoryUiModel.Summary) {
        binding.attendanceHistoryItem = item
    }

    interface Handler {
        fun onSummaryItemClick(item: AttendanceHistoryUiModel.Summary)
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
