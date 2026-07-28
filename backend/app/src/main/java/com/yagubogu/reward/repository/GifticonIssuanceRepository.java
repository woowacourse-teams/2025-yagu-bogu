package com.yagubogu.reward.repository;

import com.yagubogu.reward.domain.GifticonIssuance;
import com.yagubogu.reward.domain.WeeklyTopScore;
import com.yagubogu.reward.dto.GifticonReconciliationTarget;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GifticonIssuanceRepository extends JpaRepository<GifticonIssuance, Long> {

    List<GifticonIssuance> findAllByWeeklyTopScore(WeeklyTopScore weeklyTopScore);

    List<GifticonIssuance> findAllByMemberIdOrderByCreatedAtDesc(Long memberId);

    Optional<GifticonIssuance> findByIdAndMemberId(Long id, Long memberId);

    /**
     * 대사 시각이 지난 요청 진행 중 발급 건을 순서대로 조회한다.
     */
    @Query("""
            SELECT new com.yagubogu.reward.dto.GifticonReconciliationTarget(
                issuance.id,
                issuance.externalOrderId,
                issuance.requestStartedAt,
                issuance.reconciliationAttemptCount
            )
            FROM GifticonIssuance issuance
            WHERE issuance.status =
                com.yagubogu.reward.domain.GifticonIssuanceStatus.REQUEST_IN_PROGRESS
                AND issuance.nextReconciliationAt IS NOT NULL
                AND issuance.nextReconciliationAt <= :now
            ORDER BY issuance.status ASC, issuance.nextReconciliationAt ASC, issuance.id ASC
            """)
    List<GifticonReconciliationTarget> findDueReconciliationTargets(
            @Param("now") LocalDateTime now,
            Pageable pageable
    );
}
