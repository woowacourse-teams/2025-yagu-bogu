package com.yagubogu.stadium.dto;

import com.yagubogu.stadium.domain.Stadium;

public record StadiumResponse(
        Long id,
        String fullName,
        String shortName,
        String location,
        Double latitude,
        Double longitude

) {

    public static StadiumResponse from(final Stadium stadium) {
        return new StadiumResponse(
                stadium.getId(),
                stadium.getFullName(),
                stadium.getShortName(),
                stadium.getLocation(),
                stadium.getLatitude(),
                stadium.getLongitude()
        );
    }
}
