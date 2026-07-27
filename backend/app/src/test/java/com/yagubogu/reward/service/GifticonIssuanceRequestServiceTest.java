package com.yagubogu.reward.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.yagubogu.global.exception.BadGatewayException;
import com.yagubogu.global.exception.ConflictException;
import com.yagubogu.reward.client.GiftOrderClient;
import com.yagubogu.reward.client.GiftOrderRequest;
import com.yagubogu.reward.client.GiftOrderResult;
import com.yagubogu.reward.client.KakaoGiftRequestRejectedException;
import com.yagubogu.reward.client.KakaoGiftRequestUncertainException;
import com.yagubogu.reward.domain.GifticonIssuance;
import com.yagubogu.reward.domain.GifticonIssuanceStatus;
import com.yagubogu.reward.domain.RecipientPhoneNumber;
import com.yagubogu.reward.domain.WeeklyTopScore;
import com.yagubogu.reward.dto.v1.GifticonIssuanceResponse;
import com.yagubogu.reward.repository.GifticonIssuanceRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

@ExtendWith(MockitoExtension.class)
class GifticonIssuanceRequestServiceTest {

    @Mock
    private GifticonIssuanceRepository gifticonIssuanceRepository;

    @Mock
    private GiftOrderClient giftOrderClient;

    @Mock
    private TransactionTemplate transactionTemplate;

    private GifticonIssuanceRequestService service;
    private GifticonIssuance issuance;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-07-20T03:00:00Z"), ZoneOffset.UTC);
        service = new GifticonIssuanceRequestService(
                gifticonIssuanceRepository,
                giftOrderClient,
                transactionTemplate,
                clock
        );
        WeeklyTopScore weeklyTopScore = new WeeklyTopScore(
                LocalDate.of(2026, 7, 13),
                2,
                LocalDateTime.of(2026, 7, 20, 0, 0)
        );
        issuance = new GifticonIssuance(weeklyTopScore, null, "order-id", LocalDateTime.MIN);
        when(gifticonIssuanceRepository.findByIdAndMemberId(1L, 2L)).thenReturn(Optional.of(issuance));
        executeTransactionCallbacksImmediately();
    }

    @DisplayName("카카오가 발급 요청을 접수하면 예약 거래 번호를 저장한다")
    @Test
    void markRequestAccepted() {
        when(giftOrderClient.requestOrder(new GiftOrderRequest("order-id", "01012345678")))
                .thenReturn(new GiftOrderResult(123L));

        GifticonIssuanceResponse response = service.requestIssuance(2L, 1L, "01012345678");

        assertThat(response.status()).isEqualTo(GifticonIssuanceStatus.REQUEST_ACCEPTED);
        assertThat(issuance.getReserveTraceId()).isEqualTo(123L);
    }

    @DisplayName("카카오가 요청을 거절하면 재시도 가능 상태로 전환한다")
    @Test
    void markRequestRetryableWhenRejected() {
        when(giftOrderClient.requestOrder(any()))
                .thenThrow(new KakaoGiftRequestRejectedException("rejected"));

        assertThatThrownBy(() -> service.requestIssuance(2L, 1L, "01012345678"))
                .isInstanceOf(BadGatewayException.class);
        assertThat(issuance.getStatus()).isEqualTo(GifticonIssuanceStatus.REQUEST_RETRYABLE);
    }

    @DisplayName("카카오 접수 여부를 알 수 없으면 요청 중 상태를 유지한다")
    @Test
    void keepRequestingWhenResultIsUncertain() {
        when(giftOrderClient.requestOrder(any()))
                .thenThrow(new KakaoGiftRequestUncertainException("uncertain", null));

        assertThatThrownBy(() -> service.requestIssuance(2L, 1L, "01012345678"))
                .isInstanceOf(BadGatewayException.class);
        assertThat(issuance.getStatus()).isEqualTo(GifticonIssuanceStatus.REQUEST_IN_PROGRESS);
        assertThat(issuance.getRecipientPhoneNumber().getValue()).isEqualTo("01012345678");
    }

    @DisplayName("이미 발급 요청 중인 건은 다시 선점할 수 없다")
    @Test
    void rejectRequestAlreadyInProgress() {
        issuance.prepareRequest(new RecipientPhoneNumber("01012345678"), LocalDateTime.MIN);

        assertThatThrownBy(() -> service.requestIssuance(2L, 1L, "01012345678"))
                .isInstanceOf(ConflictException.class);
        verifyNoInteractions(giftOrderClient);
    }

    @SuppressWarnings("unchecked")
    private void executeTransactionCallbacksImmediately() {
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<Object> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });
        lenient().doAnswer(invocation -> {
            Consumer<TransactionStatus> callback = invocation.getArgument(0);
            callback.accept(null);
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());
    }
}
