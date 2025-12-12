package com.yagubogu.ui.attendance.component

import com.yagubogu.domain.model.GameResult
import com.yagubogu.domain.model.Team
import com.yagubogu.ui.attendance.model.AttendanceHistoryItem
import com.yagubogu.ui.attendance.model.GameScoreBoard
import com.yagubogu.ui.attendance.model.GameTeam
import java.time.LocalDate

val ATTENDANCE_HISTORY_ITEM_PLAYED =
    AttendanceHistoryItem.Played(
        summary =
            AttendanceHistoryItem.Summary(
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
                attendanceDate = LocalDate.now(),
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
