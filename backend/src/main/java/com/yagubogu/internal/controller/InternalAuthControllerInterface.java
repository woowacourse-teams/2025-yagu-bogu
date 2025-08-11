package com.yagubogu.internal.controller;

import com.yagubogu.auth.dto.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Internal Auth", description = "내부 회원 관련 API")
@RequestMapping("/internal/test-auth")
public interface InternalAuthControllerInterface {

    @Operation(summary = "토큰 발급", description = "해당 회원의 토큰을 발급합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음"),
    })
    @PostMapping("/token")
    ResponseEntity<TokenResponse> issueToken(@RequestParam long memberId);
}
