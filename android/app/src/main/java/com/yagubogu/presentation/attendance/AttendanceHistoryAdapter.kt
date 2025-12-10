package com.yagubogu.presentation.attendance

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yagubogu.presentation.attendance.model.AttendanceHistoryUiModel

class AttendanceHistoryAdapter(
    private val attendanceHistorySummaryHandler: AttendanceHistorySummaryViewHolder.Handler,
    private val attendanceHistoryDetailHandler: AttendanceHistoryDetailViewHolder.Handler,
) : ListAdapter<AttendanceHistoryUiModel, RecyclerView.ViewHolder>(diffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder =
        when (AttendanceHistoryUiModel.ViewType.entries[viewType]) {
            AttendanceHistoryUiModel.ViewType.SUMMARY ->
                AttendanceHistorySummaryViewHolder.from(parent, attendanceHistorySummaryHandler)

            AttendanceHistoryUiModel.ViewType.DETAIL ->
                AttendanceHistoryDetailViewHolder.from(parent, attendanceHistoryDetailHandler)

            AttendanceHistoryUiModel.ViewType.CANCELED ->
                AttendanceHistorySummaryViewHolder.from(
                    parent,
                    object : AttendanceHistorySummaryViewHolder.Handler {
                        override fun onSummaryItemClick(item: AttendanceHistoryUiModel.Summary) = Unit
                    },
                )
        }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        when (val item: AttendanceHistoryUiModel = getItem(position)) {
            is AttendanceHistoryUiModel.Summary ->
                (holder as AttendanceHistorySummaryViewHolder).bind(item)

            is AttendanceHistoryUiModel.Detail ->
                (holder as AttendanceHistoryDetailViewHolder).bind(item)

            is AttendanceHistoryUiModel.Canceled ->
                (holder as AttendanceHistorySummaryViewHolder).bind(item.summary)
        }
    }

    override fun getItemViewType(position: Int): Int = getItem(position).type.ordinal

    companion object {
        private val diffCallback =
            object : DiffUtil.ItemCallback<AttendanceHistoryUiModel>() {
                override fun areItemsTheSame(
                    oldItem: AttendanceHistoryUiModel,
                    newItem: AttendanceHistoryUiModel,
                ): Boolean =
                    when {
                        oldItem is AttendanceHistoryUiModel.Summary &&
                            newItem is AttendanceHistoryUiModel.Summary ->
                            oldItem.id == newItem.id

                        oldItem is AttendanceHistoryUiModel.Detail &&
                            newItem is AttendanceHistoryUiModel.Detail ->
                            oldItem.summary.id == newItem.summary.id

                        oldItem is AttendanceHistoryUiModel.Canceled &&
                            newItem is AttendanceHistoryUiModel.Canceled ->
                            oldItem.summary.id == newItem.summary.id

                        else -> false
                    }

                override fun areContentsTheSame(
                    oldItem: AttendanceHistoryUiModel,
                    newItem: AttendanceHistoryUiModel,
                ): Boolean = oldItem == newItem
            }
    }
}
