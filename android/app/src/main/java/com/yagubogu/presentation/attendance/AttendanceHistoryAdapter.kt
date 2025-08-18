package com.yagubogu.presentation.attendance

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yagubogu.presentation.attendance.model.AttendanceHistoryItem

class AttendanceHistoryAdapter : ListAdapter<AttendanceHistoryItem, RecyclerView.ViewHolder>(diffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder =
        when (AttendanceHistoryItem.ViewType.entries[viewType]) {
            AttendanceHistoryItem.ViewType.SUMMARY -> AttendanceHistorySummaryViewHolder.from(parent)
            AttendanceHistoryItem.ViewType.DETAIL -> AttendanceHistoryDetailViewHolder.from(parent)
        }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        when (val item: AttendanceHistoryItem = getItem(position)) {
            is AttendanceHistoryItem.Summary -> (holder as AttendanceHistorySummaryViewHolder).bind(item)
            is AttendanceHistoryItem.Detail ->
                (holder as AttendanceHistoryDetailViewHolder).bind(item)
        }
    }

    companion object {
        // TODO: diffCallback 수정
        private val diffCallback =
            object : DiffUtil.ItemCallback<AttendanceHistoryItem>() {
                override fun areItemsTheSame(
                    oldItem: AttendanceHistoryItem,
                    newItem: AttendanceHistoryItem,
                ): Boolean =
                    when {
                        oldItem is AttendanceHistoryItem.Summary &&
                            newItem is AttendanceHistoryItem.Summary ->
                            oldItem == newItem

                        oldItem is AttendanceHistoryItem.Detail &&
                            newItem is AttendanceHistoryItem.Detail ->
                            oldItem == newItem

                        else -> false
                    }

                override fun areContentsTheSame(
                    oldItem: AttendanceHistoryItem,
                    newItem: AttendanceHistoryItem,
                ): Boolean = oldItem == newItem
            }
    }
}
