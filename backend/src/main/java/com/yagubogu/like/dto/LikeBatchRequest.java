package com.yagubogu.like.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record LikeBatchRequest(
        @NotBlank String clientInstanceId,
        @Min(0) Long windowStartEpochSec,
        LikeDelta likeDelta
) {
    public record LikeDelta(Long teamId, Long delta) {
    }
}
