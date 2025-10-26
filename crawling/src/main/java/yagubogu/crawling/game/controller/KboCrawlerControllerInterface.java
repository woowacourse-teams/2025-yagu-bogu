package yagubogu.crawling.game.controller;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import yagubogu.crawling.game.dto.GamesFromGameCenter;
import yagubogu.crawling.game.dto.ScoreboardResponse;

@Tag(name = "KboCrawler", description = "KBO 크롤링 관련 API")
@RequestMapping("/api/kbo")
public interface KboCrawlerControllerInterface {

    @Operation(summary = "특정 날짜 범위의 스코어보드 크롤링", description = "KBO 공식 사이트에서 지정한 날짜 범위의 스코어보드를 가져옵니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "경기 조회 성공")
    })
    @PostMapping("/scoreboards/range")
    ResponseEntity<List<ScoreboardResponse>> fetchScoreboardRange(
            @RequestParam @DateTimeFormat(iso = DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DATE) LocalDate endDate
    );

    @Operation(summary = "특정 날짜 범위의 게임 센터 크롤링", description = "KBO 공식 사이트에서 지정한 날짜의 게임 센터를 가져옵니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "경기 조회 성공")
    })
    @PostMapping("/game-center")
    ResponseEntity<GamesFromGameCenter> fetchGameCenter(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date);
}
