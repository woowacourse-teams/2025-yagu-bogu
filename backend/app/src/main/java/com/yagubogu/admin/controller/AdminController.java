package com.yagubogu.admin.controller;

import com.yagubogu.admin.dto.AdminCrawlingGamesRequest;
import com.yagubogu.admin.dto.AdminCrawlingGamesResponse;
import com.yagubogu.admin.service.AdminCrawlingService;
import com.yagubogu.auth.annotation.RequireRole;
import com.yagubogu.member.domain.Role;
import com.yagubogu.place.dto.PlaceSyncResult;
import com.yagubogu.place.service.PlaceSyncService;
import com.yagubogu.stat.service.LocationCheckInRankingSyncService;
import com.yagubogu.stat.service.StatSyncService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequireRole(Role.ADMIN)
@RequestMapping("/admin")
@RestController
public class AdminController {

    private final StatSyncService statSyncService;
    private final LocationCheckInRankingSyncService locationCheckInRankingSyncService;
    private final AdminCrawlingService adminCrawlingService;
    private final PlaceSyncService placeSyncService;

    @PostMapping("/victory-fairy-rankings/sync")
    public ResponseEntity<Void> syncVictoryRankings() {
        int year = LocalDate.now().getYear();
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);
        for (LocalDate date = startDate; date.isBefore(endDate); date = date.plusDays(1)) {
            statSyncService.updateRankings(date);
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/location-check-in-rankings/sync")
    public ResponseEntity<Integer> syncLocationCheckInRankings() {
        int syncedCount = locationCheckInRankingSyncService.rebuildAll();

        return ResponseEntity.ok(syncedCount);
    }

    /**
     * 구장 주변 장소를 즉시 재동기화한다. 매일 새벽 3시 스케줄러를 기다릴 수 없을 때(서비스키 교체 직후 등) 사용한다.
     *
     * <p>구장 x 카테고리 조합을 순회하며 외부 API를 호출하므로 응답까지 수 분 이상 걸릴 수 있다.
     * 프록시 타임아웃으로 응답을 못 받아도 서버 측 동기화는 계속 진행되며, 진행 상황은 {@code [PlaceSync]} 로그로 확인한다.
     * 이미 동기화가 진행 중이면 409를 반환한다 — 중복 실행은 서비스키의 일일 트래픽 한도를 그대로 소모한다.</p>
     */
    @PostMapping("/places/sync")
    public ResponseEntity<PlaceSyncResult> syncPlaces() {
        return placeSyncService.syncAll()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.CONFLICT).build());
    }

    @PostMapping("/crawling/games")
    public ResponseEntity<AdminCrawlingGamesResponse> crawlGames(
            @Valid @RequestBody final AdminCrawlingGamesRequest request
    ) {
        AdminCrawlingGamesResponse response = adminCrawlingService.crawlGames(request);

        return ResponseEntity.ok(response);
    }
}
