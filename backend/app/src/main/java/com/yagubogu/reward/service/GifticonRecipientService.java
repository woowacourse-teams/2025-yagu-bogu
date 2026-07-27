package com.yagubogu.reward.service;

import com.yagubogu.reward.dto.v1.GifticonIssuancesResponse;
import com.yagubogu.reward.repository.GifticonIssuanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class GifticonRecipientService {

    private final GifticonIssuanceRepository gifticonIssuanceRepository;

    @Transactional(readOnly = true)
    public GifticonIssuancesResponse findGifticons(final long memberId) {
        return GifticonIssuancesResponse.from(
                gifticonIssuanceRepository.findAllByMemberIdOrderByCreatedAtDesc(memberId));
    }
}
