package com.yagubogu.stat.dto;

public record InsertDto(
        Long memberId,
        Integer year,
        Double score,
        Integer winCount,
        Integer checkInCount
) {
}
