package com.yagubogu.reward.controller.v1;

import com.yagubogu.auth.annotation.RequireRole;
import com.yagubogu.auth.dto.MemberClaims;
import com.yagubogu.reward.dto.v1.GifticonIssuanceResponse;
import com.yagubogu.reward.dto.v1.GifticonIssuancesResponse;
import com.yagubogu.reward.dto.v1.GifticonRecipientRequest;
import com.yagubogu.reward.service.GifticonIssuanceRequestService;
import com.yagubogu.reward.service.GifticonRecipientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequireRole
@RestController
public class GifticonController implements GifticonControllerInterface {

    private final GifticonRecipientService gifticonRecipientService;
    private final GifticonIssuanceRequestService gifticonIssuanceRequestService;

    @Override
    public ResponseEntity<GifticonIssuancesResponse> findGifticons(final MemberClaims memberClaims) {
        return ResponseEntity.ok(gifticonRecipientService.findGifticons(memberClaims.id()));
    }

    @Override
    public ResponseEntity<GifticonIssuanceResponse> registerRecipient(
            final MemberClaims memberClaims,
            final long gifticonIssuanceId,
            final GifticonRecipientRequest request
    ) {
        return ResponseEntity.ok(
                gifticonIssuanceRequestService.requestIssuance(
                        memberClaims.id(),
                        gifticonIssuanceId,
                        request.phoneNumber()
                ));
    }
}
