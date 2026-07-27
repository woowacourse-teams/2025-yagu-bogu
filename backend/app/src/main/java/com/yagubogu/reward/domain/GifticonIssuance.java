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
 * 당첨자 발급 기록
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

    public void registerRecipientPhoneNumber(final RecipientPhoneNumber recipientPhoneNumber,
                                             final LocalDateTime now) {
        if (status != GifticonIssuanceStatus.AWAITING_RECIPIENT_INFO
                && status != GifticonIssuanceStatus.READY) {
            throw new InvalidGifticonIssuanceStateException("change recipient phone number", status);
        }
        this.recipientPhoneNumber = recipientPhoneNumber;
        this.status = GifticonIssuanceStatus.READY;
        this.updatedAt = now;
    }

    public void markRequested(final long reserveTraceId, final LocalDateTime now) {
        validateStatus(GifticonIssuanceStatus.REQUESTING);
        this.reserveTraceId = reserveTraceId;
        this.status = GifticonIssuanceStatus.REQUESTED;
        this.updatedAt = now;
    }

    public void startRequesting(final LocalDateTime now) {
        validateStatus(GifticonIssuanceStatus.READY);
        this.status = GifticonIssuanceStatus.REQUESTING;
        this.updatedAt = now;
    }

    public void returnToReady(final LocalDateTime now) {
        validateStatus(GifticonIssuanceStatus.REQUESTING);
        this.status = GifticonIssuanceStatus.READY;
        this.updatedAt = now;
    }

    private void validateStatus(final GifticonIssuanceStatus expected) {
        if (status != expected) {
            throw new InvalidGifticonIssuanceStateException("transition status", expected, status);
        }
    }
}
