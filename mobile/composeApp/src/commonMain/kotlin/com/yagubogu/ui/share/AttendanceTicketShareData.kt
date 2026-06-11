package com.yagubogu.ui.share

import androidx.compose.runtime.Immutable
import co.touchlab.kermit.Logger
import com.yagubogu.domain.model.GameResult
import com.yagubogu.ui.attendance.model.AttendanceHistoryItem
import com.yagubogu.ui.attendance.model.AttendanceHistorySort
import com.yagubogu.ui.attendance.model.TeamType
import com.yagubogu.ui.stats.my.model.StatsCounts
import com.yagubogu.ui.theme.Gray600
import com.yagubogu.ui.theme.Primary600
import com.yagubogu.ui.theme.Red
import kotlinx.datetime.DayOfWeek
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.attendance_share_quote_draw
import yagubogu.composeapp.generated.resources.attendance_share_quote_lose
import yagubogu.composeapp.generated.resources.attendance_share_quote_neutral
import yagubogu.composeapp.generated.resources.attendance_share_quote_win
import yagubogu.composeapp.generated.resources.day_friday
import yagubogu.composeapp.generated.resources.day_monday
import yagubogu.composeapp.generated.resources.day_saturday
import yagubogu.composeapp.generated.resources.day_sunday
import yagubogu.composeapp.generated.resources.day_thursday
import yagubogu.composeapp.generated.resources.day_tuesday
import yagubogu.composeapp.generated.resources.day_wednesday
import yagubogu.composeapp.generated.resources.img_ticket_bg_draw
import yagubogu.composeapp.generated.resources.img_ticket_bg_lose
import yagubogu.composeapp.generated.resources.img_ticket_bg_win
import yagubogu.composeapp.generated.resources.img_ticket_draw
import yagubogu.composeapp.generated.resources.img_ticket_lose
import yagubogu.composeapp.generated.resources.img_ticket_normal
import yagubogu.composeapp.generated.resources.img_ticket_win
import kotlin.math.roundToInt

private val shareLogger = Logger.withTag("AttendanceShareTicket")

@Immutable
internal data class AttendanceShareTicketUiState(
    val shareYear: Int,
    val style: AttendanceShareStyle,
    val stats: AttendanceShareStats,
)

@Immutable
internal data class AttendanceShareStyle(
    val background: DrawableResource,
    val stamp: DrawableResource,
    val mainColor: androidx.compose.ui.graphics.Color,
    val quote: StringResource,
)

@Immutable
internal data class AttendanceShareStats(
    val totalCount: Int,
    val winCount: Int,
    val drawCount: Int,
    val loseCount: Int,
    val winRate: Int,
)

@Immutable
data class AttendanceShareRecordStats(
    val winCount: Int = 0,
    val drawCount: Int = 0,
    val loseCount: Int = 0,
    val winRate: Int = 0,
)

@Immutable
data class AttendanceTicketShareData(
    val favoriteTeam: String?,
    val yearItems: List<AttendanceHistoryItem>,
    val recordStats: AttendanceShareRecordStats,
) {
    val isReady: Boolean get() = yearItems.isNotEmpty()
}

internal fun StatsCounts.toAttendanceShareRecordStats(winRate: Double): AttendanceShareRecordStats =
    AttendanceShareRecordStats(
        winCount = winCounts,
        drawCount = drawCounts,
        loseCount = loseCounts,
        winRate = winRate.roundToInt(),
    )

internal fun AttendanceHistoryItem.toShareTicketUiState(
    yearItems: List<AttendanceHistoryItem>,
    favoriteTeamCode: String?,
    recordStats: AttendanceShareRecordStats,
): AttendanceShareTicketUiState {
    validateShareData(favoriteTeamCode)

    return AttendanceShareTicketUiState(
        shareYear = dateTime.year,
        style = resolveShareStyle(favoriteTeamCode),
        stats =
            yearItems.calculateShareStats(
                currentItem = this,
                recordStats = recordStats,
            ),
    )
}

private fun AttendanceHistoryItem.resolveShareStyle(favoriteTeamCode: String?): AttendanceShareStyle =
    shareResult(favoriteTeamCode)?.shareStyle() ?: neutralShareStyle()

private fun AttendanceHistoryItem.shareResult(favoriteTeamCode: String?): GameResult? {
    val normalizedFavoriteTeam = favoriteTeamCode?.trim().orEmpty()

    if (normalizedFavoriteTeam.isNotBlank()) {
        return when {
            awayTeam.team.shortname.equals(normalizedFavoriteTeam, ignoreCase = true) ->
                awayTeam.gameResult

            homeTeam.team.shortname.equals(normalizedFavoriteTeam, ignoreCase = true) ->
                homeTeam.gameResult

            else -> null
        }
    }

    return when {
        awayTeam.isMyTeam -> awayTeam.gameResult
        homeTeam.isMyTeam -> homeTeam.gameResult
        else -> null
    }
}

private fun GameResult.shareStyle(): AttendanceShareStyle =
    when (this) {
        GameResult.WIN ->
            AttendanceShareStyle(
                background = Res.drawable.img_ticket_bg_win,
                stamp = Res.drawable.img_ticket_win,
                mainColor = Primary600,
                quote = Res.string.attendance_share_quote_win,
            )

        GameResult.LOSE ->
            AttendanceShareStyle(
                background = Res.drawable.img_ticket_bg_lose,
                stamp = Res.drawable.img_ticket_lose,
                mainColor = Red,
                quote = Res.string.attendance_share_quote_lose,
            )

        GameResult.DRAW ->
            AttendanceShareStyle(
                background = Res.drawable.img_ticket_bg_draw,
                stamp = Res.drawable.img_ticket_draw,
                mainColor = Gray600,
                quote = Res.string.attendance_share_quote_draw,
            )
    }

private fun neutralShareStyle(): AttendanceShareStyle =
    AttendanceShareStyle(
        background = Res.drawable.img_ticket_bg_win,
        stamp = Res.drawable.img_ticket_normal,
        mainColor = Primary600,
        quote = Res.string.attendance_share_quote_neutral,
    )

private fun List<AttendanceHistoryItem>.calculateShareStats(
    currentItem: AttendanceHistoryItem,
    recordStats: AttendanceShareRecordStats,
): AttendanceShareStats {
    val sortedItems = sortedBy { it.dateTime }
    val currentIndex = sortedItems.indexOfFirst { it.id == currentItem.id }
    val targetItems =
        if (currentIndex >= 0) {
            sortedItems.take(currentIndex + 1)
        } else {
            sortedItems
        }

    return AttendanceShareStats(
        totalCount = targetItems.size,
        winCount = recordStats.winCount,
        drawCount = recordStats.drawCount,
        loseCount = recordStats.loseCount,
        winRate = recordStats.winRate,
    )
}

internal fun resolveShareYearHistorySort(): String = AttendanceHistorySort.OLDEST.name

private fun AttendanceHistoryItem.validateShareData(favoriteTeamCode: String?) {
    val issues =
        buildList {
            if (stadiumName.isBlank()) add("stadiumName is blank")
            if (awayTeam.name.isBlank()) add("awayTeam name is blank")
            if (homeTeam.name.isBlank()) add("homeTeam name is blank")
            if (awayTeam.score.isBlank()) add("awayTeam score is blank")
            if (homeTeam.score.isBlank()) add("homeTeam score is blank")
            if (awayTeam.type != TeamType.AWAY) add("awayTeam type is ${awayTeam.type}")
            if (homeTeam.type != TeamType.HOME) add("homeTeam type is ${homeTeam.type}")
            if (awayTeam.team == homeTeam.team) add("awayTeam and homeTeam are the same")
        }

    if (issues.isNotEmpty()) {
        shareLogger.w {
            "공유 경기 정보 검증 실패(checkInId=$id): ${issues.joinToString()}"
        }
    }

    val normalizedFavoriteTeam = favoriteTeamCode?.trim().orEmpty()
    if (
        normalizedFavoriteTeam.isNotBlank() &&
        !awayTeam.team.shortname.equals(normalizedFavoriteTeam, ignoreCase = true) &&
        !homeTeam.team.shortname.equals(normalizedFavoriteTeam, ignoreCase = true)
    ) {
        shareLogger.d {
            "중립경기 공유(checkInId=$id): 응원팀=$normalizedFavoriteTeam, 경기=${awayTeam.team.shortname}/${homeTeam.team.shortname}"
        }
    }
}

internal fun DayOfWeek.toKoreanResource(): StringResource =
    when (this) {
        DayOfWeek.MONDAY -> Res.string.day_monday
        DayOfWeek.TUESDAY -> Res.string.day_tuesday
        DayOfWeek.WEDNESDAY -> Res.string.day_wednesday
        DayOfWeek.THURSDAY -> Res.string.day_thursday
        DayOfWeek.FRIDAY -> Res.string.day_friday
        DayOfWeek.SATURDAY -> Res.string.day_saturday
        DayOfWeek.SUNDAY -> Res.string.day_sunday
    }

internal fun Int.twoDigits(): String = toString().padStart(2, '0')
