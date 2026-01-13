package com.yagubogu.auth.controller.v1;

import com.yagubogu.auth.annotation.RequireRole;
import com.yagubogu.auth.dto.LoginParam;
import com.yagubogu.auth.dto.LogoutParam;
import com.yagubogu.auth.dto.TokenParam;
import com.yagubogu.auth.dto.v1.LoginResponse;
import com.yagubogu.auth.dto.v1.TokenResponse;
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
            @RequestBody final LoginParam request
    ) {
        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<TokenResponse> refresh(
            @RequestBody final TokenParam request
    ) {
        TokenResponse response = authService.refreshToken(request.refreshToken());

        return ResponseEntity.ok(response);
    }

    @RequireRole
    public ResponseEntity<TokenResponse> logout(
            @RequestBody final LogoutParam request
    ) {
        authService.logout(request);

        return ResponseEntity.noContent().build();
    }
}
