package com.yagubogu.presentation.attendance

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.yagubogu.presentation.attendance.model.AttendanceHistoryItem

class AttendanceHistoryAdapter :
    ListAdapter<AttendanceHistoryItem, AttendanceHistoryViewHolder>(
        diffCallback,
    ) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): AttendanceHistoryViewHolder = AttendanceHistoryViewHolder.from(parent)

    override fun onBindViewHolder(
        holder: AttendanceHistoryViewHolder,
        position: Int,
    ) {
        val item: AttendanceHistoryItem = getItem(position)
        holder.bind(item)
    }

    companion object {
        private val diffCallback =
            object : DiffUtil.ItemCallback<AttendanceHistoryItem>() {
                override fun areItemsTheSame(
                    oldItem: AttendanceHistoryItem,
                    newItem: AttendanceHistoryItem,
                ): Boolean = oldItem.attendanceDate == newItem.attendanceDate

                override fun areContentsTheSame(
                    oldItem: AttendanceHistoryItem,
                    newItem: AttendanceHistoryItem,
                ): Boolean = oldItem == newItem
            }
    }
}
