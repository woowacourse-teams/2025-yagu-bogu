package com.yagubogu.stadium.dto;

import com.yagubogu.stadium.domain.Stadium;

public record StadiumParam(
        Long id,
        String fullName,
        String shortName,
        String location,
        Double latitude,
        Double longitude

) {

    public static StadiumParam from(final Stadium stadium) {
        return new StadiumParam(
                stadium.getId(),
                stadium.getFullName(),
                stadium.getShortName(),
                stadium.getLocation(),
                stadium.getLatitude(),
                stadium.getLongitude()
        );
    }
}
