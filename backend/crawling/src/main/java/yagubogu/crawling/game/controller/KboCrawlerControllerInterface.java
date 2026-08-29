package yagubogu.crawling.game.controller;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import yagubogu.crawling.game.dto.GameDateCrawlRequest;
import yagubogu.crawling.game.dto.GameDateCrawlResponse;
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
    ResponseEntity<Integer> fetchGameCenter(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date);

    @Operation(summary = "특정 날짜 게임 크롤링", description = "지정한 날짜의 스코어보드와 게임센터를 가져온 뒤 Bronze 처리 여부와 관계없이 Silver를 강제 동기화합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "날짜 기반 크롤링 성공")
    })
    @PostMapping("/games/by-date")
    ResponseEntity<GameDateCrawlResponse> fetchGamesByDate(@Valid @RequestBody GameDateCrawlRequest request);

    @Operation(summary = "리뷰 크롤링 수동 실행", description = "특정 경기의 타자/투수 기록을 크롤링합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "크롤링 성공")
    })
    @PostMapping("/review")
    ResponseEntity<Void> fetchReview(@RequestParam String gameCode);

    @Operation(summary = "리뷰 크롤링 재시도 큐 등록", description = "특정 경기의 리뷰 크롤링을 재시도 큐에 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "리뷰 크롤링 재시도 큐 등록 성공")
    })
    @PostMapping("/review/retries")
    ResponseEntity<Void> enqueueReviewRetry(
            @RequestParam String gameCode,
            @RequestParam(defaultValue = "30") long delayMinutes
    );
}
