package com.yagubogu.internal.controller;

import com.yagubogu.auth.dto.v1.TokenResponse;
import com.yagubogu.internal.service.InternalAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Profile({"local", "dev", "k6"})
@RequiredArgsConstructor
@RestController
public class InternalAuthController implements InternalAuthControllerInterface {

    private final InternalAuthService internalAuthService;

    public ResponseEntity<TokenResponse> issueToken(@RequestParam final long memberId) {
        TokenResponse response = internalAuthService.issueAccessToken(memberId);

        return ResponseEntity.ok(response);
    }
}
