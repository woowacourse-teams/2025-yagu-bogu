package com.yagubogu.presentation.attendance

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yagubogu.presentation.attendance.model.AttendanceHistoryItem

class AttendanceHistoryAdapter(
    private val attendanceHistorySummaryHandler: AttendanceHistorySummaryViewHolder.Handler,
    private val attendanceHistoryDetailHandler: AttendanceHistoryDetailViewHolder.Handler,
) : ListAdapter<AttendanceHistoryItem, RecyclerView.ViewHolder>(diffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder =
        when (AttendanceHistoryItem.ViewType.entries[viewType]) {
            AttendanceHistoryItem.ViewType.SUMMARY ->
                AttendanceHistorySummaryViewHolder.from(parent, attendanceHistorySummaryHandler)

            AttendanceHistoryItem.ViewType.DETAIL ->
                AttendanceHistoryDetailViewHolder.from(parent, attendanceHistoryDetailHandler)
        }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        when (val item: AttendanceHistoryItem = getItem(position)) {
            is AttendanceHistoryItem.Summary ->
                (holder as AttendanceHistorySummaryViewHolder).bind(item)

            is AttendanceHistoryItem.Detail ->
                (holder as AttendanceHistoryDetailViewHolder).bind(item)
        }
    }

    override fun getItemViewType(position: Int): Int = getItem(position).type.ordinal

    companion object {
        private val diffCallback =
            object : DiffUtil.ItemCallback<AttendanceHistoryItem>() {
                override fun areItemsTheSame(
                    oldItem: AttendanceHistoryItem,
                    newItem: AttendanceHistoryItem,
                ): Boolean =
                    when {
                        oldItem is AttendanceHistoryItem.Summary &&
                            newItem is AttendanceHistoryItem.Summary ->
                            oldItem.id == newItem.id

                        oldItem is AttendanceHistoryItem.Detail &&
                            newItem is AttendanceHistoryItem.Detail ->
                            oldItem.summary.id == newItem.summary.id

                        else -> false
                    }

                override fun areContentsTheSame(
                    oldItem: AttendanceHistoryItem,
                    newItem: AttendanceHistoryItem,
                ): Boolean = oldItem == newItem
            }
    }
}
