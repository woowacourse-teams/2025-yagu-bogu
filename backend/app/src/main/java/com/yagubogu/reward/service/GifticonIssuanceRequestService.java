package com.yagubogu.reward.service;

import com.yagubogu.global.exception.BadGatewayException;
import com.yagubogu.global.exception.ConflictException;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.reward.client.GiftOrderClient;
import com.yagubogu.reward.client.GiftOrderRequest;
import com.yagubogu.reward.client.GiftOrderResult;
import com.yagubogu.reward.client.KakaoGiftRequestRejectedException;
import com.yagubogu.reward.client.KakaoGiftRequestUncertainException;
import com.yagubogu.reward.domain.GifticonIssuance;
import com.yagubogu.reward.domain.InvalidGifticonIssuanceStateException;
import com.yagubogu.reward.domain.RecipientPhoneNumber;
import com.yagubogu.reward.dto.v1.GifticonIssuanceResponse;
import com.yagubogu.reward.repository.GifticonIssuanceRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 기프티콘 발급 요청과 상태 변경을 조율한다.
 *
 * <p>외부 호출은 DB 트랜잭션 밖에서 수행한다.
 */
@RequiredArgsConstructor
@Service
public class GifticonIssuanceRequestService {

    private final GifticonIssuanceRepository gifticonIssuanceRepository;
    private final GiftOrderClient giftOrderClient;
    private final TransactionTemplate transactionTemplate;
    private final Clock clock;

    /**
     * 전화번호를 저장해 발급 요청을 선점하고 외부 발급 응답을 상태에 반영한다.
     */
    public GifticonIssuanceResponse requestIssuance(
            final long memberId,
            final long gifticonIssuanceId,
            final String recipientPhoneNumber
    ) {
        GiftOrderRequest request = prepareRequest(memberId, gifticonIssuanceId, recipientPhoneNumber);
        try {
            GiftOrderResult result = giftOrderClient.requestOrder(request);
            return markRequestAccepted(memberId, gifticonIssuanceId, result);
        } catch (KakaoGiftRequestRejectedException exception) {
            markRequestRetryable(memberId, gifticonIssuanceId);
            throw new BadGatewayException("Kakao rejected the gifticon issuance request");
        } catch (KakaoGiftRequestUncertainException exception) {
            throw new BadGatewayException("Kakao gifticon issuance result is uncertain");
        }
    }

    /**
     * 전화번호 저장과 발급 요청 선점을 한 트랜잭션으로 처리한다.
     */
    private GiftOrderRequest prepareRequest(
            final long memberId,
            final long gifticonIssuanceId,
            final String recipientPhoneNumber
    ) {
        try {
            return transactionTemplate.execute(status -> {
                GifticonIssuance issuance = findOwnedIssuance(gifticonIssuanceId, memberId);
                try {
                    issuance.prepareRequest(
                            new RecipientPhoneNumber(recipientPhoneNumber),
                            LocalDateTime.now(clock)
                    );
                } catch (InvalidGifticonIssuanceStateException exception) {
                    throw new ConflictException(exception.getMessage());
                }
                return new GiftOrderRequest(
                        issuance.getExternalOrderId(),
                        issuance.getRecipientPhoneNumber()
                );
            });
        } catch (ObjectOptimisticLockingFailureException exception) {
            throw new ConflictException("Gifticon issuance request is already in progress");
        }
    }

    /**
     * 외부 제공자가 접수한 요청의 추적 번호와 상태를 저장한다.
     */
    private GifticonIssuanceResponse markRequestAccepted(
            final long memberId,
            final long gifticonIssuanceId,
            final GiftOrderResult result
    ) {
        return transactionTemplate.execute(status -> {
            GifticonIssuance issuance = findOwnedIssuance(gifticonIssuanceId, memberId);
            issuance.markRequestAccepted(result.reserveTraceId(), LocalDateTime.now(clock));
            return GifticonIssuanceResponse.from(issuance);
        });
    }

    /**
     * 외부 제공자가 거절한 요청을 재시도 가능한 상태로 변경한다.
     */
    private void markRequestRetryable(final long memberId, final long gifticonIssuanceId) {
        transactionTemplate.executeWithoutResult(status -> {
            GifticonIssuance issuance = findOwnedIssuance(gifticonIssuanceId, memberId);
            issuance.markRequestRetryable(LocalDateTime.now(clock));
        });
    }

    /**
     * 회원이 소유한 발급 건을 조회한다.
     */
    private GifticonIssuance findOwnedIssuance(final long gifticonIssuanceId, final long memberId) {
        return gifticonIssuanceRepository.findByIdAndMemberId(gifticonIssuanceId, memberId)
                .orElseThrow(() -> new NotFoundException("Gifticon issuance is not found"));
    }
}
