package com.yagubogu.stadium.controller.v1;

import com.yagubogu.stadium.dto.v1.StadiumsResponse;
import com.yagubogu.stadium.dto.v1.StadiumsWithGamesResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Stadium", description = "구장 관련 API")
@RequestMapping("/stadiums")
public interface StadiumControllerInterface {

    @Operation(summary = "전체 구장 목록 조회", description = "등록된 모든 구장 목록을 조회합니다. 제 2구장을 제외한 등록된 모든 구장 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "구장 목록 조회 성공")
    @GetMapping
    ResponseEntity<StadiumsResponse> findAllMainStadiums();

    @Operation(summary = "구장별 경기 목록 조회", description = "해당 날짜에 구장에서 진행하는 경기 목록들을 조회합니다. "
            + "경기가 없는 구장은 반환되지 않습니다.")
    @ApiResponse(responseCode = "200", description = "구장 목록 조회 성공")
    @GetMapping("/games")
    ResponseEntity<StadiumsWithGamesResponse> findStadiumsWithGame(@RequestParam LocalDate date);
}
