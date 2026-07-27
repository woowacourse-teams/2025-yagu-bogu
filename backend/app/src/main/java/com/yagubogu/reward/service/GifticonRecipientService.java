package com.yagubogu.reward.service;

import com.yagubogu.global.exception.ConflictException;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.reward.domain.GifticonIssuance;
import com.yagubogu.reward.domain.InvalidGifticonIssuanceStateException;
import com.yagubogu.reward.domain.RecipientPhoneNumber;
import com.yagubogu.reward.dto.v1.GifticonIssuancesResponse;
import com.yagubogu.reward.repository.GifticonIssuanceRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class GifticonRecipientService {

    private final GifticonIssuanceRepository gifticonIssuanceRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public GifticonIssuancesResponse findGifticons(final long memberId) {
        return GifticonIssuancesResponse.from(
                gifticonIssuanceRepository.findAllByMemberIdOrderByCreatedAtDesc(memberId));
    }

    @Transactional
    public void registerPhoneNumber(final long memberId, final long gifticonIssuanceId, final String phoneNumber) {
        GifticonIssuance issuance = findOwnedIssuance(gifticonIssuanceId, memberId);
        try {
            issuance.registerRecipientPhoneNumber(
                    new RecipientPhoneNumber(phoneNumber),
                    LocalDateTime.now(clock)
            );
            gifticonIssuanceRepository.flush();
        } catch (InvalidGifticonIssuanceStateException exception) {
            throw new ConflictException(exception.getMessage());
        } catch (ObjectOptimisticLockingFailureException exception) {
            throw new ConflictException("Recipient phone number is already being registered");
        }
    }

    private GifticonIssuance findOwnedIssuance(final long gifticonIssuanceId, final long memberId) {
        return gifticonIssuanceRepository.findByIdAndMemberId(gifticonIssuanceId, memberId)
                .orElseThrow(() -> new NotFoundException("Gifticon issuance is not found"));
    }
}
