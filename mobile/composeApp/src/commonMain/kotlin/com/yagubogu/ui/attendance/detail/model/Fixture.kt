package com.yagubogu.ui.attendance.detail.model

val HITTER_RECORDS: List<PlayerRecordUiModel.HitterRecord> =
    listOf(
        PlayerRecordUiModel.HitterRecord(
            battingOrder = 1,
            position = "지명타자",
            playerName = "고종욱",
            atBats = 4,
            hits = 0,
            rbi = 0,
            runs = 0,
        ),
        PlayerRecordUiModel.HitterRecord(
            battingOrder = 1,
            position = "대타",
            playerName = "박정우",
            atBats = 0,
            hits = 0,
            rbi = 0,
            runs = 1,
        ),
        PlayerRecordUiModel.HitterRecord(
            battingOrder = 2,
            position = "유격수",
            playerName = "박찬호",
            atBats = 4,
            hits = 0,
            rbi = 0,
            runs = 2,
        ),
        PlayerRecordUiModel.HitterRecord(
            battingOrder = 3,
            position = "2루수",
            playerName = "김선빈",
            atBats = 4,
            hits = 3,
            rbi = 1,
            runs = 2,
        ),
        PlayerRecordUiModel.HitterRecord(
            battingOrder = 4,
            position = "지명타자",
            playerName = "나성범",
            atBats = 3,
            hits = 1,
            rbi = 0,
            runs = 1,
        ),
    )

val PITCHER_RECORDS: List<PlayerRecordUiModel.PitcherRecord> =
    listOf(
        PlayerRecordUiModel.PitcherRecord(
            playerName = "양현종",
            result = PitcherResult.WIN,
            innings = "5 ⅓",
            hitsAllowed = 8,
            runsAllowed = 3,
            earnedRuns = 2,
            strikeouts = 4,
            walksAndHbp = 0,
            pitchCount = 86,
        ),
        PlayerRecordUiModel.PitcherRecord(
            playerName = "성영탁",
            result = PitcherResult.HOLD,
            innings = "0 ⅔",
            hitsAllowed = 2,
            runsAllowed = 1,
            earnedRuns = 1,
            strikeouts = 0,
            walksAndHbp = 0,
            pitchCount = 12,
        ),
        PlayerRecordUiModel.PitcherRecord(
            playerName = "전상현",
            result = null,
            innings = "1",
            hitsAllowed = 0,
            runsAllowed = 0,
            earnedRuns = 0,
            strikeouts = 2,
            walksAndHbp = 1,
            pitchCount = 19,
        ),
    )

val PLAYER_RECORD =
    PlayerRecordUiModel(
        awayTeamHitters = HITTER_RECORDS,
        awayTeamPitchers = PITCHER_RECORDS,
        homeTeamHitters = listOf(),
        homeTeamPitchers = listOf(),
    )
