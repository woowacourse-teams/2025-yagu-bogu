package com.yagubogu.ui.attendance.component

import com.yagubogu.domain.model.GameResult
import com.yagubogu.domain.model.Team
import com.yagubogu.ui.attendance.model.AttendanceHistoryItem
import com.yagubogu.ui.attendance.model.GameState
import com.yagubogu.ui.attendance.model.PastGameUiModel
import com.yagubogu.ui.attendance.model.TeamType
import com.yagubogu.ui.util.minusDays
import com.yagubogu.ui.util.now
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

val ATTENDANCE_HISTORY_ITEM_PLAYED =
    AttendanceHistoryItem(
        id = 0L,
        gameState = GameState.COMPLETED,
        dateTime = LocalDateTime(LocalDate.now(), LocalTime.now()),
        stadiumName = "잠실 야구장",
        awayTeam =
            AttendanceHistoryItem.GameTeam(
                team = Team.HT,
                name = "KIA",
                score = "10",
                isMyTeam = true,
                gameResult = GameResult.WIN,
                type = TeamType.AWAY,
            ),
        homeTeam =
            AttendanceHistoryItem.GameTeam(
                team = Team.OB,
                name = "두산",
                score = "4",
                isMyTeam = false,
                gameResult = GameResult.LOSE,
                type = TeamType.HOME,
            ),
        awayTeamScoreBoard =
            AttendanceHistoryItem.GameScoreBoard(
                runs = 10,
                hits = 13,
                errors = 0,
                basesOnBalls = 5,
                scores = listOf("0", "0", "1", "0", "1", "4", "1", "1", "2", "-", "-"),
            ),
        homeTeamScoreBoard =
            AttendanceHistoryItem.GameScoreBoard(
                runs = 4,
                hits = 9,
                errors = 2,
                basesOnBalls = 3,
                scores = listOf("0", "0", "2", "0", "1", "0", "0", "1", "0", "-", "-"),
            ),
    )

val ATTENDANCE_HISTORY_ITEM_CANCELED =
    AttendanceHistoryItem(
        id = 1L,
        gameState = GameState.CANCELED,
        dateTime = LocalDateTime(LocalDate.now().minusDays(2), LocalTime.now()),
        stadiumName = "잠실 야구장",
        awayTeam =
            AttendanceHistoryItem.GameTeam(
                team = Team.HT,
                name = "KIA",
                score = "-",
                isMyTeam = true,
                gameResult = GameResult.DRAW,
                type = TeamType.AWAY,
            ),
        homeTeam =
            AttendanceHistoryItem.GameTeam(
                team = Team.OB,
                name = "두산",
                score = "-",
                isMyTeam = false,
                gameResult = GameResult.DRAW,
                type = TeamType.HOME,
            ),
        awayTeamScoreBoard =
            AttendanceHistoryItem.GameScoreBoard(
                runs = 0,
                hits = 0,
                errors = 0,
                basesOnBalls = 0,
                scores = listOf("-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-"),
            ),
        homeTeamScoreBoard =
            AttendanceHistoryItem.GameScoreBoard(
                runs = 0,
                hits = 0,
                errors = 0,
                basesOnBalls = 0,
                scores = listOf("-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-"),
            ),
    )

val ATTENDANCE_HISTORY_ITEMS =
    listOf(
        ATTENDANCE_HISTORY_ITEM_PLAYED,
        ATTENDANCE_HISTORY_ITEM_CANCELED,
        ATTENDANCE_HISTORY_ITEM_PLAYED.copy(
            id = 2L,
            dateTime = LocalDateTime(LocalDate.now().minusDays(15), LocalTime.now()),
        ),
        ATTENDANCE_HISTORY_ITEM_PLAYED.copy(
            id = 3L,
            dateTime = LocalDateTime(LocalDate.now().minusDays(5), LocalTime.now()),
        ),
    )

val GAME_DATES =
    setOf(
        LocalDate.now(),
        LocalDate.now().minusDays(1),
        LocalDate.now().minusDays(2),
        LocalDate.now().minusDays(3),
        LocalDate.now().minusDays(4),
        LocalDate.now().minusDays(5),
    )

val PAST_GAME_UI_MODEL =
    PastGameUiModel(
        gameId = 0L,
        date = LocalDate.now(),
        startAt = LocalTime.now(),
        stadiumName = "광주 KIA 챔피언스필드",
        awayTeam = Team.LT,
        awayTeamName = "롯데",
        homeTeam = Team.HT,
        homeTeamName = "KIA",
    )

val PAST_GAME_UI_MODELS =
    listOf(
        PAST_GAME_UI_MODEL,
        PAST_GAME_UI_MODEL.copy(gameId = 1L),
        PAST_GAME_UI_MODEL.copy(gameId = 2L),
        PAST_GAME_UI_MODEL.copy(gameId = 3L),
    )
