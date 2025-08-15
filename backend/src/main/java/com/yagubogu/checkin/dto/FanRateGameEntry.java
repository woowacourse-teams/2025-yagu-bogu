package com.yagubogu.checkin.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class FanRateGameEntry implements Comparable<FanRateGameEntry> {

    private final long totalCheckInCounts;
    private final FanRateByGameResponse response;

    @Override
    public int compareTo(final FanRateGameEntry other) {
        return Long.compare(other.totalCheckInCounts, this.totalCheckInCounts);
    }
}
