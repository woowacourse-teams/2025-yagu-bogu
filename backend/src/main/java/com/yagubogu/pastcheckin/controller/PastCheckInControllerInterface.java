package com.yagubogu.pastcheckin.controller;

import com.yagubogu.auth.dto.MemberClaims;
import com.yagubogu.pastcheckin.dto.CreatePastCheckInRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "과거 직관 인증")
@RequestMapping("/api/past-check-ins")
public interface PastCheckInControllerInterface {

    @Operation(summary = "과거 직관 인증 생성", description = "과거 경기에 대한 직관 인증을 생성합니다. 위치 인증이 필요하지 않으며, 승리 요정 랭킹에는 포함되지 않습니다.")
    @PostMapping
    ResponseEntity<Void> createPastCheckIn(
            @Parameter(hidden = true) final MemberClaims memberClaims,
            @Valid @RequestBody final CreatePastCheckInRequest request
    );
}
