package com.yagubogu.member.controller;

import com.yagubogu.auth.dto.MemberClaims;
import com.yagubogu.member.dto.MemberFavoriteResponse;
import com.yagubogu.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
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

    @GetMapping("/{memberId}/favorites")
    public ResponseEntity<MemberFavoriteResponse> findFavorites(
            @PathVariable final long memberId
    ) {
        MemberFavoriteResponse response = memberService.findFavorite(memberId);

        return ResponseEntity.ok(response);
    }
}
