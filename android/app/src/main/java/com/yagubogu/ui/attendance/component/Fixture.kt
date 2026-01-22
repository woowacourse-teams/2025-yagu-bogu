package com.yagubogu.ui.attendance.component

import com.yagubogu.domain.model.GameResult
import com.yagubogu.domain.model.Team
import com.yagubogu.ui.attendance.model.AttendanceHistoryItem
import com.yagubogu.ui.attendance.model.GameScoreBoard
import com.yagubogu.ui.attendance.model.GameTeam
import com.yagubogu.ui.attendance.model.PastGameUiModel
import java.time.LocalDate
import java.time.LocalTime

val ATTENDANCE_HISTORY_ITEM_PLAYED =
    AttendanceHistoryItem.Played(
        summary =
            AttendanceHistoryItem.Summary(
                id = 0L,
                attendanceDate = LocalDate.now(),
                stadiumName = "잠실 야구장",
                awayTeam =
                    GameTeam(
                        team = Team.HT,
                        name = "KIA",
                        score = "10",
                        isMyTeam = true,
                        gameResult = GameResult.WIN,
                    ),
                homeTeam =
                    GameTeam(
                        team = Team.OB,
                        name = "두산",
                        score = "4",
                        isMyTeam = false,
                        gameResult = GameResult.LOSE,
                    ),
            ),
        awayTeamPitcher = "이의리",
        homeTeamPitcher = "김택연",
        awayTeamScoreBoard =
            GameScoreBoard(
                runs = 10,
                hits = 13,
                errors = 0,
                basesOnBalls = 5,
                scores = listOf("0", "0", "1", "0", "1", "4", "1", "1", "2", "-", "-"),
            ),
        homeTeamScoreBoard =
            GameScoreBoard(
                runs = 4,
                hits = 9,
                errors = 2,
                basesOnBalls = 3,
                scores = listOf("0", "0", "2", "0", "1", "0", "0", "1", "0", "-", "-"),
            ),
    )

val ATTENDANCE_HISTORY_ITEM_CANCELED =
    AttendanceHistoryItem.Canceled(
        summary =
            AttendanceHistoryItem.Summary(
                id = 1L,
                attendanceDate = LocalDate.now().minusDays(2L),
                stadiumName = "잠실 야구장",
                awayTeam =
                    GameTeam(
                        team = Team.HT,
                        name = "KIA",
                        score = "-",
                        isMyTeam = true,
                        gameResult = GameResult.DRAW,
                    ),
                homeTeam =
                    GameTeam(
                        team = Team.OB,
                        name = "두산",
                        score = "-",
                        isMyTeam = false,
                        gameResult = GameResult.DRAW,
                    ),
            ),
    )

val ATTENDANCE_HISTORY_ITEMS =
    listOf(
        ATTENDANCE_HISTORY_ITEM_PLAYED,
        ATTENDANCE_HISTORY_ITEM_CANCELED,
        ATTENDANCE_HISTORY_ITEM_PLAYED.copy(
            summary =
                ATTENDANCE_HISTORY_ITEM_PLAYED.summary.copy(
                    id = 2L,
                    attendanceDate = LocalDate.now().minusDays(15L),
                ),
        ),
        ATTENDANCE_HISTORY_ITEM_PLAYED.copy(
            summary =
                ATTENDANCE_HISTORY_ITEM_PLAYED.summary.copy(
                    id = 3L,
                    attendanceDate = LocalDate.now().minusDays(5L),
                ),
        ),
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
