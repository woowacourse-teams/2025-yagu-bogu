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
import com.yagubogu.place.dto.v1.detail.AttractionDetail;
import com.yagubogu.place.dto.v1.detail.FoodDetail;
import com.yagubogu.place.dto.v1.detail.LodgingDetail;
import com.yagubogu.place.dto.v1.detail.PerformanceDetail;
import com.yagubogu.place.dto.v1.detail.PlaceDetail;
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

        JsonNode root = readDetailInfo(place.getDetailInfo());
        if (root == null) {
            return PlaceDetailResponse.from(place, null, null, null);
        }

        JsonNode common = root.path("common");
        String overview = nullableText(common, "overview");
        String homepage = nullableText(common, "homepage");
        PlaceDetail detail = readDetail(place.getCategory(), root.path("intro"));

        return PlaceDetailResponse.from(place, overview, homepage, detail);
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

    private PlaceDetail readDetail(PlaceCategory category, JsonNode intro) {
        if (intro.isMissingNode()) {
            return null;
        }
        Class<? extends PlaceDetail> type = switch (category) {
            case ATTRACTION -> AttractionDetail.class;
            case PERFORMANCE -> PerformanceDetail.class;
            case LODGING -> LodgingDetail.class;
            case RESTAURANT, CAFE -> FoodDetail.class;
        };
        try {
            return objectMapper.treeToValue(intro, type);
        } catch (JsonProcessingException e) {
            log.warn("[PlaceQuery] Failed to map detail intro for category={}: {}", category, e.getMessage());
            return null;
        }
    }

    private String nullableText(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (value.isMissingNode() || value.isNull()) {
            return null;
        }
        String text = value.asText().trim();
        return text.isEmpty() ? null : text;
    }
}
