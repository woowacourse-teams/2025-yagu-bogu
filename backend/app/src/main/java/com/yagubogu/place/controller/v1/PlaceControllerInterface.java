package com.yagubogu.place.controller.v1;

import com.yagubogu.place.domain.PlaceCategory;
import com.yagubogu.place.dto.v1.PlaceDetailResponse;
import com.yagubogu.place.dto.v1.PlacesResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Place", description = "구장 주변 장소(맛집/카페/숙소/관광지/공연) API")
@RequestMapping("/places")
public interface PlaceControllerInterface {

    @Operation(summary = "구장 주변 장소 목록 조회",
               description = "지정한 구장 ID와 카테고리 기준으로 반경 내 장소 목록을 반환합니다. 데이터는 매일 새벽 3시에 갱신됩니다.")
    @GetMapping
    ResponseEntity<PlacesResponse> getPlacesByStadium(
            @Parameter(description = "구장 ID", required = true)
            @RequestParam Long stadiumId,
            @Parameter(description = "장소 카테고리", required = true)
            @RequestParam PlaceCategory category
    );

    @Operation(summary = "장소 상세 정보 조회",
               description = "장소 ID로 상세 정보를 조회합니다. 상세 정보는 배치 동기화 시 최초 1회 수집되어 DB에 저장됩니다.")
    @GetMapping("/{id}")
    ResponseEntity<PlaceDetailResponse> getPlaceDetail(
            @Parameter(description = "장소 ID", required = true)
            @PathVariable Long id
    );
}
