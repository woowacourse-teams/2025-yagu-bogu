package com.yagubogu.presentation.attendance

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TableRow
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yagubogu.databinding.ItemAttendanceHistoryDetailBinding
import com.yagubogu.presentation.attendance.model.AttendanceHistoryItem
import com.yagubogu.presentation.attendance.model.GameScoreBoard

class AttendanceHistoryDetailViewHolder private constructor(
    private val binding: ItemAttendanceHistoryDetailBinding,
    handler: Handler,
) : RecyclerView.ViewHolder(binding.root) {
    private val tableRows: List<TableRow> =
        (0 until binding.tableScoreboard.childCount)
            .mapNotNull { binding.tableScoreboard.getChildAt(it) as? TableRow }

    init {
        binding.handler = handler
    }

    fun bind(item: AttendanceHistoryItem.Detail) {
        binding.attendanceHistoryItem = item

        binding.tvAwayTeamPitcher.text =
            itemView.context.getString(item.awayTeamPitcherStringRes, item.awayTeam.pitcher)
        binding.tvHomeTeamPitcher.text =
            itemView.context.getString(item.homeTeamPitcherStringRes, item.homeTeam.pitcher)

        updateScoreRow(tableRows[1], item.awayTeam.name, item.awayTeamScoreBoard)
        updateScoreRow(tableRows[2], item.homeTeam.name, item.homeTeamScoreBoard)
    }

    private fun updateScoreRow(
        row: TableRow,
        teamName: String,
        scoreBoard: GameScoreBoard,
    ) {
        (row.getChildAt(0) as TextView).text = teamName

        scoreBoard.inningScore.forEachIndexed { index: Int, score: String ->
            (row.getChildAt(index + 2) as TextView).text = score
        }
        (row.getChildAt(row.childCount - 1) as TextView).text = scoreBoard.runs.toString()
    }

    interface Handler {
        fun onDetailItemClick(item: AttendanceHistoryItem.Detail)
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
