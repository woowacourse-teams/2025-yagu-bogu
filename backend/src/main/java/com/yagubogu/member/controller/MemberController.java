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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/members")
@RestController
public class MemberController {

    private final MemberService memberService;

    @PatchMapping("/me/nickname")
    public ResponseEntity<MemberNicknameResponse> patchNickname(
            @RequestBody final MemberNicknameRequest request,
            final MemberClaims memberClaims
    ) {
        MemberNicknameResponse response = memberService.patchNickname(memberClaims.id(), request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> removeMember(
            final MemberClaims memberClaims
    ) {
        memberService.removeMember(memberClaims.id());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me/nickname")
    public ResponseEntity<MemberNicknameResponse> findNickname(
            final MemberClaims memberClaims
    ) {
        MemberNicknameResponse response = memberService.findNickname(memberClaims.id());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/favorites")
    public ResponseEntity<MemberFavoriteResponse> findFavorites(
            final MemberClaims memberClaims
    ) {
        MemberFavoriteResponse response = memberService.findFavorite(memberClaims.id());

        return ResponseEntity.ok(response);
    }
}
