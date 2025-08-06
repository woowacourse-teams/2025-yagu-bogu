package com.yagubogu.member.controller;

import com.yagubogu.auth.annotation.RequireRole;
import com.yagubogu.auth.dto.MemberClaims;
import com.yagubogu.member.dto.MemberFavoriteResponse;
import com.yagubogu.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequireRole
@RequestMapping("/api/members")
@RestController
public class MemberController {

    private final MemberService memberService;

    @DeleteMapping("/me")
    public ResponseEntity<Void> removeMember(
            final MemberClaims memberClaims
    ) {
        memberService.removeMember(memberClaims.id());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/favorites")
    public ResponseEntity<MemberFavoriteResponse> findFavorites(
            final MemberClaims memberClaims
    ) {
        MemberFavoriteResponse response = memberService.findFavorite(memberClaims.id());

        return ResponseEntity.ok(response);
    }
}
