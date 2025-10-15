package com.yagubogu.pastcheckin.controller;

import com.yagubogu.auth.annotation.RequireRole;
import com.yagubogu.auth.dto.MemberClaims;
import com.yagubogu.pastcheckin.dto.CreatePastCheckInRequest;
import com.yagubogu.pastcheckin.service.PastCheckInService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequireRole
@RestController
public class PastCheckInController implements PastCheckInControllerInterface {

    private final PastCheckInService pastCheckInService;

    @Override
    public ResponseEntity<Void> createPastCheckIn(
            final MemberClaims memberClaims,
            @RequestBody @Valid final CreatePastCheckInRequest request
    ) {
        pastCheckInService.createPastCheckIn(memberClaims.id(), request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
