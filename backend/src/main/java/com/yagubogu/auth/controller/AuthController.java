package com.yagubogu.auth.controller;

import com.yagubogu.auth.annotation.RequireRole;
import com.yagubogu.auth.dto.LoginRequest;
import com.yagubogu.auth.dto.LoginResponse;
import com.yagubogu.auth.dto.LogoutRequest;
import com.yagubogu.auth.dto.TokenRequest;
import com.yagubogu.auth.dto.TokenResponse;
import com.yagubogu.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class AuthController implements AuthControllerInterface {

    private final AuthService authService;

    public ResponseEntity<LoginResponse> login(
            @RequestBody final LoginRequest request
    ) {
        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<TokenResponse> refresh(
            @RequestBody final TokenRequest request
    ) {
        TokenResponse response = authService.refreshToken(request.refreshToken());

        return ResponseEntity.ok(response);
    }

    @RequireRole
    public ResponseEntity<TokenResponse> logout(
            @RequestBody final LogoutRequest request
    ) {
        authService.logout(request);

        return ResponseEntity.noContent().build();
    }
}
