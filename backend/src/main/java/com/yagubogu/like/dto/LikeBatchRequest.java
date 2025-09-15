package com.yagubogu.like.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record LikeBatchRequest(
        @NotBlank String clientInstanceId,
        @Min(0) Long windowStartEpochSec,
        List<Entry> entries
) {
    public record Entry(Long teamId, Integer delta) {
    }
}
