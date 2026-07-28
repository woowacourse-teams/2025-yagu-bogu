package com.yagubogu.reward.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.global.config.JpaAuditingConfig;
import com.yagubogu.member.domain.Member;
import com.yagubogu.reward.domain.GifticonIssuance;
import com.yagubogu.reward.domain.RecipientPhoneNumber;
import com.yagubogu.reward.domain.WeeklyTopScore;
import com.yagubogu.reward.dto.GifticonReconciliationTarget;
import com.yagubogu.support.base.ServiceUsingMysqlTestBase;
import com.yagubogu.support.member.MemberFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

@Import({AuthTestConfig.class, JpaAuditingConfig.class})
class GifticonIssuanceRepositoryUsingMysqlTest extends ServiceUsingMysqlTestBase {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 28, 12, 0);

    @Autowired
    private GifticonIssuanceRepository gifticonIssuanceRepository;

    @Autowired
    private WeeklyTopScoreRepository weeklyTopScoreRepository;

    @Autowired
    private MemberFactory memberFactory;

    @DisplayName("대사 시각이 지난 요청만 시각과 식별자 순으로 제한해 조회한다")
    @Test
    void findDueReconciliationTargets() {
        WeeklyTopScore weeklyTopScore = weeklyTopScoreRepository.save(
                new WeeklyTopScore(LocalDate.of(2026, 7, 20), 1, NOW.minusDays(1))
        );
        GifticonIssuance second = saveScheduledIssuance(
                weeklyTopScore, "order-second", NOW.minusMinutes(1)
        );
        GifticonIssuance first = saveScheduledIssuance(
                weeklyTopScore, "order-first", NOW.minusMinutes(2)
        );
        GifticonIssuance sameTimeAsFirst = saveScheduledIssuance(
                weeklyTopScore, "order-same-time", NOW.minusMinutes(2)
        );
        saveScheduledIssuance(weeklyTopScore, "order-future", NOW.plusMinutes(1));

        List<GifticonReconciliationTarget> targets =
                gifticonIssuanceRepository.findDueReconciliationTargets(
                        NOW,
                        PageRequest.of(0, 3)
                );

        assertThat(targets)
                .extracting(GifticonReconciliationTarget::id)
                .containsExactly(first.getId(), sameTimeAsFirst.getId(), second.getId());
        assertThat(targets)
                .extracting(GifticonReconciliationTarget::externalOrderId)
                .doesNotContain("order-future");
    }

    private GifticonIssuance saveScheduledIssuance(
            final WeeklyTopScore weeklyTopScore,
            final String externalOrderId,
            final LocalDateTime nextReconciliationAt
    ) {
        Member member = memberFactory.save(builder -> {
        });
        GifticonIssuance issuance = new GifticonIssuance(weeklyTopScore, member, externalOrderId, NOW.minusHours(1));
        issuance.prepareRequest(new RecipientPhoneNumber("01012345678"), NOW.minusMinutes(10));
        issuance.scheduleInitialReconciliation(NOW.minusMinutes(9), nextReconciliationAt, "request timeout");
        return gifticonIssuanceRepository.save(issuance);
    }
}
