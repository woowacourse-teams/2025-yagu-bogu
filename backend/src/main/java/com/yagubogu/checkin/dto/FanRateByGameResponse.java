package com.yagubogu.checkin.dto;

public record FanRateByGameResponse(
        TeamFanRateResponse homeTeam,
        TeamFanRateResponse awayTeam
) {
}
