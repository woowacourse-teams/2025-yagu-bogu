package com.yagubogu.reward.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GifticonIssuanceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 20, 12, 0);

    @DisplayName("당첨 발급 건은 수신자 정보 대기 상태로 생성된다")
    @Test
    void createAwaitingRecipientInfo() {
        GifticonIssuance issuance = new GifticonIssuance(null, null, "order-id", NOW);

        assertThat(issuance.getStatus()).isEqualTo(GifticonIssuanceStatus.AWAITING_RECIPIENT_INFO);
    }

    @DisplayName("전화번호를 등록하면서 발급 요청을 선점한다")
    @Test
    void prepareRequest() {
        GifticonIssuance issuance = new GifticonIssuance(null, null, "order-id", NOW);

        issuance.prepareRequest(new RecipientPhoneNumber("01012345678"), NOW.plusMinutes(1));

        assertThat(issuance.getStatus()).isEqualTo(GifticonIssuanceStatus.REQUEST_IN_PROGRESS);
        assertThat(issuance.getRecipientPhoneNumber().getValue()).isEqualTo("01012345678");
        assertThat(issuance.getRequestStartedAt()).isEqualTo(NOW.plusMinutes(1));
        assertThat(issuance.getReconciliationAttemptCount()).isZero();
        assertThat(issuance.getNextReconciliationAt()).isNull();
        assertThat(issuance.getLastReconciledAt()).isNull();
        assertThat(issuance.getLastReconciliationError()).isNull();
    }

    @DisplayName("수신자 전화번호가 없으면 발급 요청을 준비할 수 없다")
    @Test
    void rejectPreparingRequestWithoutRecipientPhoneNumber() {
        GifticonIssuance issuance = new GifticonIssuance(null, null, "order-id", NOW);

        assertThatThrownBy(() -> issuance.prepareRequest(null, NOW.plusMinutes(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Recipient phone number must not be null");
        assertThat(issuance.getStatus()).isEqualTo(GifticonIssuanceStatus.AWAITING_RECIPIENT_INFO);
        assertThat(issuance.getRecipientPhoneNumber()).isNull();
    }

    @DisplayName("발송 요청을 선점한 뒤에는 다시 준비할 수 없다")
    @Test
    void rejectPreparingRequestAfterRequestStarted() {
        GifticonIssuance issuance = new GifticonIssuance(null, null, "order-id", NOW);
        issuance.prepareRequest(new RecipientPhoneNumber("01012345678"), NOW);

        assertThatThrownBy(() -> issuance.prepareRequest(
                new RecipientPhoneNumber("01087654321"), NOW.plusMinutes(1)))
                .isInstanceOf(InvalidGifticonIssuanceStateException.class);
    }

    @DisplayName("재시도 가능 상태에서는 전화번호를 갱신하고 발급 요청을 다시 선점한다")
    @Test
    void prepareRetryableRequest() {
        GifticonIssuance issuance = new GifticonIssuance(null, null, "order-id", NOW);
        issuance.prepareRequest(new RecipientPhoneNumber("01012345678"), NOW);
        issuance.markRequestRetryable(NOW.plusMinutes(1));

        issuance.prepareRequest(new RecipientPhoneNumber("01087654321"), NOW.plusMinutes(2));

        assertThat(issuance.getStatus()).isEqualTo(GifticonIssuanceStatus.REQUEST_IN_PROGRESS);
        assertThat(issuance.getRecipientPhoneNumber().getValue()).isEqualTo("01087654321");
        assertThat(issuance.getRequestStartedAt()).isEqualTo(NOW.plusMinutes(2));
        assertThat(issuance.getReconciliationAttemptCount()).isZero();
    }

    @DisplayName("최초 주문 결과가 불확실하면 시도 횟수를 늘리지 않고 대사를 예약한다")
    @Test
    void scheduleInitialReconciliation() {
        GifticonIssuance issuance = createRequestInProgress();
        LocalDateTime reconcilesAt = NOW.plusMinutes(2);

        issuance.scheduleInitialReconciliation(NOW.plusMinutes(1), reconcilesAt, "request timeout");

        assertSoftly(softAssertions -> {
            softAssertions.assertThat(issuance.getStatus())
                    .isEqualTo(GifticonIssuanceStatus.REQUEST_IN_PROGRESS);
            softAssertions.assertThat(issuance.getReconciliationAttemptCount()).isZero();
            softAssertions.assertThat(issuance.getNextReconciliationAt()).isEqualTo(reconcilesAt);
            softAssertions.assertThat(issuance.getLastReconciledAt()).isNull();
            softAssertions.assertThat(issuance.getLastReconciliationError()).isEqualTo("request timeout");
        });
    }

    @DisplayName("주문을 찾지 못하면 상태를 유지하고 다음 대사를 예약한다")
    @Test
    void recordReconciliationNotFound() {
        GifticonIssuance issuance = createRequestInProgress();
        LocalDateTime reconciledAt = NOW.plusMinutes(2);
        LocalDateTime nextReconciliationAt = NOW.plusMinutes(7);

        issuance.recordReconciliationNotFound(reconciledAt, nextReconciliationAt);

        assertSoftly(softAssertions -> {
            softAssertions.assertThat(issuance.getStatus())
                    .isEqualTo(GifticonIssuanceStatus.REQUEST_IN_PROGRESS);
            softAssertions.assertThat(issuance.getReconciliationAttemptCount()).isEqualTo(1);
            softAssertions.assertThat(issuance.getLastReconciledAt()).isEqualTo(reconciledAt);
            softAssertions.assertThat(issuance.getNextReconciliationAt()).isEqualTo(nextReconciliationAt);
            softAssertions.assertThat(issuance.getLastReconciliationError()).isEqualTo("Gift order not found");
        });
    }

    @DisplayName("대사 결과가 불확실하면 오류를 제한 길이로 기록하고 다음 대사를 예약한다")
    @Test
    void recordReconciliationUncertain() {
        GifticonIssuance issuance = createRequestInProgress();
        String longError = "e".repeat(1_001);

        issuance.recordReconciliationUncertain(
                NOW.plusMinutes(2),
                NOW.plusMinutes(7),
                longError
        );

        assertSoftly(softAssertions -> {
            softAssertions.assertThat(issuance.getStatus())
                    .isEqualTo(GifticonIssuanceStatus.REQUEST_IN_PROGRESS);
            softAssertions.assertThat(issuance.getReconciliationAttemptCount()).isEqualTo(1);
            softAssertions.assertThat(issuance.getLastReconciledAt()).isEqualTo(NOW.plusMinutes(2));
            softAssertions.assertThat(issuance.getNextReconciliationAt()).isEqualTo(NOW.plusMinutes(7));
            softAssertions.assertThat(issuance.getLastReconciliationError()).hasSize(1_000);
        });
    }

    @DisplayName("대사에서 주문을 찾으면 접수 상태와 추적 번호를 복구한다")
    @Test
    void recoverRequestAccepted() {
        GifticonIssuance issuance = createRequestInProgress();
        issuance.scheduleInitialReconciliation(NOW.plusMinutes(1), NOW.plusMinutes(2), "request timeout");

        issuance.recoverRequestAccepted(123L, NOW.plusMinutes(2));

        assertSoftly(softAssertions -> {
            softAssertions.assertThat(issuance.getStatus())
                    .isEqualTo(GifticonIssuanceStatus.REQUEST_ACCEPTED);
            softAssertions.assertThat(issuance.getReserveTraceId()).isEqualTo(123L);
            softAssertions.assertThat(issuance.getReconciliationAttemptCount()).isEqualTo(1);
            softAssertions.assertThat(issuance.getLastReconciledAt()).isEqualTo(NOW.plusMinutes(2));
            softAssertions.assertThat(issuance.getNextReconciliationAt()).isNull();
            softAssertions.assertThat(issuance.getLastReconciliationError()).isNull();
        });
    }

    @DisplayName("대사에서 주문 생성 실패를 확인하면 재요청 가능 상태로 전환한다")
    @Test
    void markCreationFailedRetryable() {
        GifticonIssuance issuance = createRequestInProgress();

        issuance.markCreationFailedRetryable(NOW.plusMinutes(2));

        assertSoftly(softAssertions -> {
            softAssertions.assertThat(issuance.getStatus())
                    .isEqualTo(GifticonIssuanceStatus.REQUEST_RETRYABLE);
            softAssertions.assertThat(issuance.getReconciliationAttemptCount()).isEqualTo(1);
            softAssertions.assertThat(issuance.getLastReconciledAt()).isEqualTo(NOW.plusMinutes(2));
            softAssertions.assertThat(issuance.getNextReconciliationAt()).isNull();
            softAssertions.assertThat(issuance.getLastReconciliationError()).isNull();
        });
    }

    @DisplayName("추적 번호는 양수여야 한다")
    @Test
    void rejectNonPositiveReserveTraceId() {
        GifticonIssuance issuance = createRequestInProgress();

        assertThatThrownBy(() -> issuance.recoverRequestAccepted(0, NOW.plusMinutes(2)))
                .isInstanceOf(InvalidGifticonReserveTraceIdException.class)
                .hasMessageContaining("must be positive");
        assertThat(issuance.getStatus()).isEqualTo(GifticonIssuanceStatus.REQUEST_IN_PROGRESS);
    }

    @DisplayName("요청 진행 중이 아니면 대사 결과를 반영할 수 없다")
    @Test
    void rejectReconciliationOutsideRequestInProgress() {
        GifticonIssuance issuance = new GifticonIssuance(null, null, "order-id", NOW);

        assertSoftly(softAssertions -> {
            softAssertions.assertThatThrownBy(() -> issuance.scheduleInitialReconciliation(
                            NOW, NOW.plusMinutes(1), "timeout"))
                    .isInstanceOf(InvalidGifticonIssuanceStateException.class);
            softAssertions.assertThatThrownBy(() -> issuance.recordReconciliationNotFound(
                            NOW, NOW.plusMinutes(1)))
                    .isInstanceOf(InvalidGifticonIssuanceStateException.class);
            softAssertions.assertThatThrownBy(() -> issuance.recordReconciliationUncertain(
                            NOW, NOW.plusMinutes(1), "timeout"))
                    .isInstanceOf(InvalidGifticonIssuanceStateException.class);
            softAssertions.assertThatThrownBy(() -> issuance.recoverRequestAccepted(123L, NOW))
                    .isInstanceOf(InvalidGifticonIssuanceStateException.class);
            softAssertions.assertThatThrownBy(() -> issuance.markCreationFailedRetryable(NOW))
                    .isInstanceOf(InvalidGifticonIssuanceStateException.class);
        });
    }

    private GifticonIssuance createRequestInProgress() {
        GifticonIssuance issuance = new GifticonIssuance(null, null, "order-id", NOW);
        issuance.prepareRequest(new RecipientPhoneNumber("01012345678"), NOW.plusMinutes(1));
        return issuance;
    }
}
