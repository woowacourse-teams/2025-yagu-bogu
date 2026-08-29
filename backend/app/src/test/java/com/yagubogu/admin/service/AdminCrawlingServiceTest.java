package com.yagubogu.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.yagubogu.admin.client.CrawlingAdminClient;
import com.yagubogu.admin.dto.AdminCrawlingGamesRequest;
import com.yagubogu.admin.dto.AdminCrawlingGamesResponse;
import com.yagubogu.admin.dto.CrawlingGameDateResponse;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AdminCrawlingServiceTest {

    private final CrawlingAdminClient crawlingAdminClient = mock(CrawlingAdminClient.class);
    private final AdminCrawlingService adminCrawlingService = new AdminCrawlingService(crawlingAdminClient);

    @DisplayName("Admin 경기 크롤링 응답에 날짜별 강제 ETL 처리 건수를 합산한다")
    @Test
    void aggregateTransformedCount() {
        LocalDate firstDate = LocalDate.of(2026, 8, 12);
        LocalDate secondDate = LocalDate.of(2026, 8, 13);
        AdminCrawlingGamesRequest request = new AdminCrawlingGamesRequest(
                firstDate, secondDate, 0L, 0L
        );
        when(crawlingAdminClient.fetchGames(firstDate)).thenReturn(new CrawlingGameDateResponse(
                5, 5, 0, 5, 5, List.of("20260812HHOB0"), List.of()
        ));
        when(crawlingAdminClient.fetchGames(secondDate)).thenReturn(new CrawlingGameDateResponse(
                5, 5, 0, 5, 5, List.of("20260813HHOB0"), List.of()
        ));

        AdminCrawlingGamesResponse response = adminCrawlingService.crawlGames(request);

        assertThat(response.requested()).isEqualTo(10);
        assertThat(response.saved()).isZero();
        assertThat(response.skipped()).isEqualTo(10);
        assertThat(response.transformed()).isEqualTo(10);
        assertThat(response.savedGameCodes()).containsExactly("20260812HHOB0", "20260813HHOB0");
    }
}
