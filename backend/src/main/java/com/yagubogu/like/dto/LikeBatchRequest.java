package com.yagubogu.like.dto;

import jakarta.validation.constraints.Min;

public record LikeBatchRequest(
        @Min(0) Long windowStartEpochSec,
        LikeDelta likeDelta
) {
    public record LikeDelta(Long teamId, Long delta) {
    }
}
