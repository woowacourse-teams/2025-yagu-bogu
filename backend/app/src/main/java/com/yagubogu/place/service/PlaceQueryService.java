package com.yagubogu.place.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.place.domain.Place;
import com.yagubogu.place.domain.PlaceCategory;
import com.yagubogu.place.dto.v1.PlaceDetailResponse;
import com.yagubogu.place.dto.v1.PlaceResponse;
import com.yagubogu.place.dto.v1.PlacesResponse;
import com.yagubogu.place.repository.PlaceRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceQueryService {

    private final PlaceRepository placeRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public PlacesResponse findByStadiumAndCategory(Long stadiumId, PlaceCategory category) {
        List<PlaceResponse> places = placeRepository.findAllByStadiumIdAndCategory(stadiumId, category)
                .stream()
                .map(PlaceResponse::from)
                .toList();

        return new PlacesResponse(stadiumId, category, places);
    }

    @Transactional(readOnly = true)
    public PlaceDetailResponse findDetailById(Long placeId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new NotFoundException("Place is not found"));

        return PlaceDetailResponse.from(place, readDetailInfo(place.getDetailInfo()));
    }

    private JsonNode readDetailInfo(String detailInfo) {
        if (detailInfo == null) {
            return null;
        }
        try {
            return objectMapper.readTree(detailInfo);
        } catch (JsonProcessingException e) {
            log.warn("[PlaceQuery] Failed to parse stored detailInfo: {}", e.getMessage());
            return null;
        }
    }
}
