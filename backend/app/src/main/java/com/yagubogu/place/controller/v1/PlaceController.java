package com.yagubogu.place.controller.v1;

import com.yagubogu.place.domain.PlaceCategory;
import com.yagubogu.place.dto.v1.PlaceDetailResponse;
import com.yagubogu.place.dto.v1.PlacesResponse;
import com.yagubogu.place.service.PlaceQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PlaceController implements PlaceControllerInterface {

    private final PlaceQueryService placeQueryService;

    @Override
    public ResponseEntity<PlacesResponse> getPlacesByStadium(Long stadiumId, PlaceCategory category) {
        return ResponseEntity.ok(placeQueryService.findByStadiumAndCategory(stadiumId, category));
    }

    @Override
    public ResponseEntity<PlaceDetailResponse> getPlaceDetail(Long id) {
        return ResponseEntity.ok(placeQueryService.findDetailById(id));
    }
}
