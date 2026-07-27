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

@RequiredArgsConstructor
@Service
public class GifticonIssuanceRequestService {

    private final GifticonIssuanceRepository gifticonIssuanceRepository;
    private final GiftOrderClient giftOrderClient;
    private final TransactionTemplate transactionTemplate;
    private final Clock clock;

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

    private void markRequestRetryable(final long memberId, final long gifticonIssuanceId) {
        transactionTemplate.executeWithoutResult(status -> {
            GifticonIssuance issuance = findOwnedIssuance(gifticonIssuanceId, memberId);
            issuance.markRequestRetryable(LocalDateTime.now(clock));
        });
    }

    private GifticonIssuance findOwnedIssuance(final long gifticonIssuanceId, final long memberId) {
        return gifticonIssuanceRepository.findByIdAndMemberId(gifticonIssuanceId, memberId)
                .orElseThrow(() -> new NotFoundException("Gifticon issuance is not found"));
    }
}
