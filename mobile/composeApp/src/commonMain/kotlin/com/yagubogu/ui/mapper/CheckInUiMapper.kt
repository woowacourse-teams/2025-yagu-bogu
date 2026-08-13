package com.yagubogu.ui.mapper

import com.yagubogu.data.dto.response.checkin.CheckInGameDto
import com.yagubogu.data.dto.response.checkin.CheckInGameTeamDto
import com.yagubogu.data.dto.response.checkin.CheckInImageDto
import com.yagubogu.data.dto.response.checkin.CheckInReviewResponse
import com.yagubogu.data.dto.response.checkin.FanRateByGameDto
import com.yagubogu.data.dto.response.checkin.HitterRecordDto
import com.yagubogu.data.dto.response.checkin.PitcherRecordDto
import com.yagubogu.data.dto.response.checkin.ScoreBoardDto
import com.yagubogu.data.dto.response.checkin.StadiumCheckInCountDto
import com.yagubogu.data.dto.response.checkin.TeamFanRateDto
import com.yagubogu.domain.model.GameResult
import com.yagubogu.domain.model.Team
import com.yagubogu.ui.attendance.detail.model.DiaryImageItem
import com.yagubogu.ui.attendance.detail.model.PitcherResult
import com.yagubogu.ui.attendance.detail.model.PlayerRecordUiModel
import com.yagubogu.ui.attendance.model.AttendanceHistoryItem
import com.yagubogu.ui.attendance.model.GameState
import com.yagubogu.ui.attendance.model.TeamType
import com.yagubogu.ui.home.model.StadiumFanRateItem
import com.yagubogu.ui.home.model.TeamFanRate
import com.yagubogu.ui.stats.detail.model.StadiumVisitCount
import kotlinx.datetime.LocalDateTime
import kotlin.math.roundToInt

fun FanRateByGameDto.toUiModel(): StadiumFanRateItem =
    StadiumFanRateItem(
        gameId = gameId,
        awayTeamFanRate = awayTeam.toUiModel(),
        homeTeamFanRate = homeTeam.toUiModel(),
    )

fun TeamFanRateDto.toUiModel(): TeamFanRate =
    TeamFanRate(
        team = Team.getByCode(code),
        teamName = name,
        fanRate = fanRate,
    )

fun CheckInGameDto.toUiModel(): AttendanceHistoryItem =
    AttendanceHistoryItem(
        id = checkInId,
        gameState = GameState.from(gameState),
        dateTime = LocalDateTime(attendanceDate, startAt),
        stadiumName = stadiumFullName,
        awayTeam = awayTeam.toUiModel(homeTeam, TeamType.AWAY),
        homeTeam = homeTeam.toUiModel(awayTeam, TeamType.HOME),
        awayTeamScoreBoard = awayScoreBoard.toUiModel(),
        homeTeamScoreBoard = homeScoreBoard.toUiModel(),
    )

fun CheckInGameTeamDto.toUiModel(
    opponent: CheckInGameTeamDto,
    type: TeamType,
): AttendanceHistoryItem.GameTeam =
    AttendanceHistoryItem.GameTeam(
        team = Team.getByCode(code),
        name = name,
        score = score?.toString() ?: "-",
        isMyTeam = isMyTeam,
        gameResult =
            if (score == null || opponent.score == null) {
                GameResult.DRAW
            } else {
                GameResult.from(score, opponent.score)
            },
        type = type,
    )

fun ScoreBoardDto.toUiModel(): AttendanceHistoryItem.GameScoreBoard =
    AttendanceHistoryItem.GameScoreBoard(
        runs = runs,
        hits = hits,
        errors = errors,
        basesOnBalls = basesOnBalls,
        scores = inningScores,
    )

fun CheckInReviewResponse.toUiModel(): PlayerRecordUiModel =
    PlayerRecordUiModel(
        awayTeamHitters = awayHitters.map { it.toUiModel() },
        awayTeamPitchers = awayPitchers.map { it.toUiModel() },
        homeTeamHitters = homeHitters.map { it.toUiModel() },
        homeTeamPitchers = homePitchers.map { it.toUiModel() },
    )

fun HitterRecordDto.toUiModel(): PlayerRecordUiModel.HitterRecord =
    PlayerRecordUiModel.HitterRecord(
        battingOrder = battingOrder,
        position = position,
        playerName = playerName,
        atBats = atBats,
        hits = hits,
        rbi = rbi,
        runs = runs,
    )

fun PitcherRecordDto.toUiModel(): PlayerRecordUiModel.PitcherRecord =
    PlayerRecordUiModel.PitcherRecord(
        playerName = playerName,
        result = result.toPitcherResult(),
        innings = innings.toInningString(),
        pitchCount = pitchCount,
        hitsAllowed = hitsAllowed,
        walksAndHbp = walksAndHbp,
        strikeouts = strikeouts,
        runsAllowed = runsAllowed,
        earnedRuns = earnedRuns,
    )

fun StadiumCheckInCountDto.toUiModel(): StadiumVisitCount =
    StadiumVisitCount(
        location = location,
        visitCounts = checkInCounts,
    )

fun CheckInImageDto.toUiModel(): DiaryImageItem =
    DiaryImageItem(
        id = imageId,
        uri = imageUrl,
    )

/**
 * 소수점으로 표현된 이닝(Double)을 야구 표기법(⅓, ⅔) 문자열로 변환합니다.
 * 예: 5.0 -> "5", 5.1 -> "5 ⅓", 5.2 -> "5 ⅔", 0.1 -> "0 ⅓"
 */
fun Double.toInningString(): String {
    val fullInnings: Int = this.toInt()
    val outs: Int = (this * 10).roundToInt() % 10

    val fraction: String =
        when (outs) {
            1 -> "⅓"
            2 -> "⅔"
            else -> ""
        }

    return when {
        fraction.isEmpty() -> fullInnings.toString() // (예: "5")
        else -> "$fullInnings $fraction" // (예: "5 ⅓")
    }
}

/**
 * 문자열의 첫 글자를 확인하여 PitcherResult Enum으로 변환합니다.
 * 예: "승리" -> WIN, "패" -> LOSE, "세이브" -> SAVE, "홀드" -> HOLD
 * 해당하는 문자가 없거나 빈 문자열이면 null을 반환합니다.
 */
fun String?.toPitcherResult(): PitcherResult? =
    when (this?.firstOrNull()) {
        '승' -> PitcherResult.WIN
        '패' -> PitcherResult.LOSE
        '홀' -> PitcherResult.HOLD
        '세' -> PitcherResult.SAVE
        else -> null
    }
