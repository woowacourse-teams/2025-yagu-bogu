package com.yagubogu.reward.domain;

import com.yagubogu.member.domain.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 당첨자의 기프티콘 발급 상태와 외부 주문 대사 이력을 관리한다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "gifticon_issuances")
@Entity
public class GifticonIssuance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "weekly_top_score_id", nullable = false)
    private WeeklyTopScore weeklyTopScore;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "external_order_id", nullable = false, length = 70)
    private String externalOrderId;

    @Embedded
    private RecipientPhoneNumber recipientPhoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private GifticonIssuanceStatus status;

    @Column(name = "reserve_trace_id")
    private Long reserveTraceId;

    @Column(name = "request_started_at")
    private LocalDateTime requestStartedAt;

    @Column(name = "reconciliation_attempt_count", nullable = false)
    private int reconciliationAttemptCount;

    @Column(name = "next_reconciliation_at")
    private LocalDateTime nextReconciliationAt;

    @Column(name = "last_reconciled_at")
    private LocalDateTime lastReconciledAt;

    @Column(name = "last_reconciliation_error", length = 1000)
    private String lastReconciliationError;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public GifticonIssuance(final WeeklyTopScore weeklyTopScore, final Member member, final String externalOrderId,
                             final LocalDateTime now) {
        this.weeklyTopScore = weeklyTopScore;
        this.member = member;
        this.externalOrderId = externalOrderId;
        this.status = GifticonIssuanceStatus.AWAITING_RECIPIENT_INFO;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void prepareRequest(final RecipientPhoneNumber recipientPhoneNumber, final LocalDateTime now) {
        if (recipientPhoneNumber == null) {
            throw new IllegalArgumentException("Recipient phone number must not be null");
        }
        if (status != GifticonIssuanceStatus.AWAITING_RECIPIENT_INFO
                && status != GifticonIssuanceStatus.REQUEST_RETRYABLE) {
            throw new InvalidGifticonIssuanceStateException("prepare request", status);
        }
        this.recipientPhoneNumber = recipientPhoneNumber;
        this.status = GifticonIssuanceStatus.REQUEST_IN_PROGRESS;
        this.requestStartedAt = now;
        this.reconciliationAttemptCount = 0;
        this.nextReconciliationAt = null;
        this.lastReconciledAt = null;
        this.lastReconciliationError = null;
        this.updatedAt = now;
    }

    public void markRequestAccepted(final long reserveTraceId, final LocalDateTime now) {
        validateStatus(GifticonIssuanceStatus.REQUEST_IN_PROGRESS);
        validateReserveTraceId(reserveTraceId);
        this.reserveTraceId = reserveTraceId;
        this.status = GifticonIssuanceStatus.REQUEST_ACCEPTED;
        clearReconciliationSchedule();
        this.updatedAt = now;
    }

    public void markRequestRetryable(final LocalDateTime now) {
        validateStatus(GifticonIssuanceStatus.REQUEST_IN_PROGRESS);
        this.status = GifticonIssuanceStatus.REQUEST_RETRYABLE;
        clearReconciliationSchedule();
        this.updatedAt = now;
    }

    /**
     * 최초 주문 결과가 불확실할 때 첫 대사 시각을 예약한다.
     */
    public void scheduleInitialReconciliation(
            final LocalDateTime now,
            final LocalDateTime nextReconciliationAt,
            final String error
    ) {
        validateStatus(GifticonIssuanceStatus.REQUEST_IN_PROGRESS);
        this.nextReconciliationAt = nextReconciliationAt;
        this.lastReconciliationError = summarize(error);
        this.updatedAt = now;
    }

    /**
     * 조회된 주문이 없으면 현재 상태를 유지하고 다음 대사를 예약한다.
     */
    public void recordReconciliationNotFound(
            final LocalDateTime now,
            final LocalDateTime nextReconciliationAt
    ) {
        recordUncertainReconciliation(now, nextReconciliationAt, "Gift order not found");
    }

    /**
     * 주문 결과를 판단할 수 없으면 오류를 기록하고 다음 대사를 예약한다.
     */
    public void recordReconciliationUncertain(
            final LocalDateTime now,
            final LocalDateTime nextReconciliationAt,
            final String error
    ) {
        recordUncertainReconciliation(now, nextReconciliationAt, error);
    }

    /**
     * 외부 주문이 확인되면 추적 번호를 복구하고 접수 완료 상태로 전환한다.
     */
    public void recoverRequestAccepted(final long reserveTraceId, final LocalDateTime now) {
        validateStatus(GifticonIssuanceStatus.REQUEST_IN_PROGRESS);
        validateReserveTraceId(reserveTraceId);
        this.reserveTraceId = reserveTraceId;
        this.status = GifticonIssuanceStatus.REQUEST_ACCEPTED;
        recordCompletedReconciliation(now);
    }

    /**
     * 외부 주문 생성 실패가 확인되면 다시 요청할 수 있는 상태로 전환한다.
     */
    public void markCreationFailedRetryable(final LocalDateTime now) {
        validateStatus(GifticonIssuanceStatus.REQUEST_IN_PROGRESS);
        this.status = GifticonIssuanceStatus.REQUEST_RETRYABLE;
        recordCompletedReconciliation(now);
    }

    private void recordUncertainReconciliation(
            final LocalDateTime now,
            final LocalDateTime nextReconciliationAt,
            final String error
    ) {
        validateStatus(GifticonIssuanceStatus.REQUEST_IN_PROGRESS);
        this.reconciliationAttemptCount++;
        this.lastReconciledAt = now;
        this.nextReconciliationAt = nextReconciliationAt;
        this.lastReconciliationError = summarize(error);
        this.updatedAt = now;
    }

    private void recordCompletedReconciliation(final LocalDateTime now) {
        this.reconciliationAttemptCount++;
        this.lastReconciledAt = now;
        clearReconciliationSchedule();
        this.updatedAt = now;
    }

    private void clearReconciliationSchedule() {
        this.nextReconciliationAt = null;
        this.lastReconciliationError = null;
    }

    private String summarize(final String error) {
        if (error == null) {
            return null;
        }
        return error.substring(0, Math.min(error.length(), 1000));
    }

    private void validateReserveTraceId(final long reserveTraceId) {
        if (reserveTraceId <= 0) {
            throw new InvalidGifticonReserveTraceIdException(reserveTraceId);
        }
    }

    private void validateStatus(final GifticonIssuanceStatus expected) {
        if (status != expected) {
            throw new InvalidGifticonIssuanceStateException("transition status", expected, status);
        }
    }
}
