package com.yagubogu.game.dto;

public record SeasonStatusResponse(
        boolean isOpened,
        int seasonYear
) {}