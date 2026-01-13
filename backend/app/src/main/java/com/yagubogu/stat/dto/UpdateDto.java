package com.yagubogu.stat.dto;

public record UpdateDto(
        Long id,
        Double score,
        Integer winCount,
        Integer checkInCount
) {
}
