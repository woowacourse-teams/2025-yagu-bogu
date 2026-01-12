package com.yagubogu.game.dto;

import java.util.List;

public record BatchResultParam(int success, List<Integer> failedIndices, long tookMs) {

    public static BatchResultParam empty() {
        return new BatchResultParam(0, List.of(), 0);
    }
}
