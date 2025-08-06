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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/auth")
@RestController
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody final LoginRequest request
    ) {
        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }

    @RequireRole
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @RequestBody final TokenRequest request
    ) {
        TokenResponse response = authService.refreshToken(request.refreshToken());

        return ResponseEntity.ok(response);
    }

    @RequireRole
    @PostMapping("/logout")
    public ResponseEntity<TokenResponse> logout(
            @RequestBody final LogoutRequest request
    ) {
        authService.logout(request);

        return ResponseEntity.noContent().build();
    }
}
