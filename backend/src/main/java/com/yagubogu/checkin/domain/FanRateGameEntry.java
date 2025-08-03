package com.yagubogu.checkin.domain;

import com.yagubogu.checkin.dto.FanRateByGameResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class FanRateGameEntry implements Comparable<FanRateGameEntry> {

    private final long totalCheckInCounts;
    private final FanRateByGameResponse response;

    @Override
    public int compareTo(FanRateGameEntry other) {
        return Long.compare(other.totalCheckInCounts, this.totalCheckInCounts);
    }
}