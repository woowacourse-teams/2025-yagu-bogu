package com.yagubogu.auth.controller.v1;

import com.yagubogu.auth.dto.LoginParam;
import com.yagubogu.auth.dto.LogoutParam;
import com.yagubogu.auth.dto.TokenParam;
import com.yagubogu.auth.dto.v1.LoginResponse;
import com.yagubogu.auth.dto.v1.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Auth", description = "인증 및 토큰 관련 API")
@RequestMapping("/auth")
public interface AuthControllerInterface {

    @Operation(summary = "로그인", description = "OAuth 인증 정보를 통해 로그인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공")
    })
    @PostMapping("/login")
    ResponseEntity<LoginResponse> login(
            @RequestBody LoginParam request
    );

    @Operation(summary = "토큰 재발급", description = "Refresh Token을 사용해 Access Token을 재발급합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
            @ApiResponse(responseCode = "401", description = "Refresh Token이 존재하지 않거나 유효하지 않음")
    })
    @PostMapping("/refresh")
    ResponseEntity<TokenResponse> refresh(
            @RequestBody TokenParam request
    );

    @Operation(summary = "로그아웃", description = "Refresh Token을 무효화하여 로그아웃합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "Refresh Token이 존재하지 않거나 유효하지 않음")
    })
    @PostMapping("/logout")
    ResponseEntity<TokenResponse> logout(
            @RequestBody LogoutParam request
    );
}

