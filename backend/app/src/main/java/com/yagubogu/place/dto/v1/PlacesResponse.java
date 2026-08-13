package com.yagubogu.place.dto.v1;

import com.yagubogu.place.domain.PlaceCategory;
import java.util.List;

public record PlacesResponse(
        Long stadiumId,
        PlaceCategory category,
        List<PlaceResponse> places
) {}
