package com.yagubogu.restaurant.controller.v1;

import com.yagubogu.restaurant.dto.v1.RestaurantsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Restaurant", description = "구장 주변 맛집 API")
@RequestMapping("/restaurants")
public interface RestaurantControllerInterface {

    @Operation(summary = "구장 주변 맛집 목록 조회",
               description = "지정한 구장 ID 기준으로 반경 내 음식점 목록을 반환합니다. 데이터는 매일 새벽 3시에 갱신됩니다.")
    @GetMapping
    ResponseEntity<RestaurantsResponse> getRestaurantsByStadium(
            @Parameter(description = "구장 ID", required = true)
            @RequestParam Long stadiumId
    );
}
