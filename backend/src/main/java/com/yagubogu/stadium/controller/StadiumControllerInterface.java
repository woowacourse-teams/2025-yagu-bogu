package com.yagubogu.stadium.controller;

import com.yagubogu.stadium.dto.StadiumsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Stadium", description = "구장 관련 API")
@RequestMapping("/api/stadiums")
public interface StadiumControllerInterface {

    @Operation(summary = "전체 구장 목록 조회", description = "등록된 모든 구장 목록을 조회합니다. 제 2구장을 제외한 등록된 모든 구장 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "구장 목록 조회 성공")
    @GetMapping
    ResponseEntity<StadiumsResponse> findAllMainStadiums();
}
