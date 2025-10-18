package com.yagubogu.checkin.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class FanRateGameParam implements Comparable<FanRateGameParam> {

    private final long totalCheckInCounts;
    private final FanRateByGameParam response;

    @Override
    public int compareTo(final FanRateGameParam other) {
        return Long.compare(other.totalCheckInCounts, this.totalCheckInCounts);
    }
}
