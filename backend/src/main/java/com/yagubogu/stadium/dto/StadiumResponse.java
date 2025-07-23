package com.yagubogu.stadium.dto;

import com.yagubogu.stadium.domain.Stadium;

public record StadiumResponse(
        Long id,
        String fullName
) {

    public static StadiumResponse from(final Stadium stadium) {
        return new StadiumResponse(stadium.getId(), stadium.getFullName());
    }
}
