package com.yagubogu.member.controller;

import com.yagubogu.auth.dto.MemberClaims;
import com.yagubogu.member.dto.MemberFavoriteResponse;
import com.yagubogu.member.dto.MemberNicknameRequest;
import com.yagubogu.member.dto.MemberNicknameResponse;
import com.yagubogu.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/members")
@RestController
public class MemberController {

    private final MemberService memberService;

    @PatchMapping("/me/{memberId}/nickname")
    public ResponseEntity<MemberNicknameResponse> patchNickname(
            @PathVariable final long memberId, // 나중에 수정
            @RequestBody final MemberNicknameRequest request
    ) {
        MemberNicknameResponse response = memberService.patchNickname(memberId, request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> removeMember(
            final MemberClaims memberClaims
    ) {
        memberService.removeMember(memberClaims.id());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me/{memberId}/nickname")
    public ResponseEntity<MemberNicknameResponse> findNickname(
            @PathVariable final long memberId // 나중에 수정
    ) {
        MemberNicknameResponse response = memberService.findNickname(memberId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{memberId}/favorites")
    public ResponseEntity<MemberFavoriteResponse> findFavorites(
            @PathVariable final long memberId
    ) {
        MemberFavoriteResponse response = memberService.findFavorite(memberId);

        return ResponseEntity.ok(response);
    }
}
