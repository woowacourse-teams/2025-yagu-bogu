package com.yagubogu.member.controller;

import com.yagubogu.auth.annotation.RequireRole;
import com.yagubogu.auth.dto.MemberClaims;
import com.yagubogu.auth.service.AuthService;
import com.yagubogu.badge.dto.BadgeListResponse;
import com.yagubogu.member.dto.MemberFavoriteRequest;
import com.yagubogu.member.dto.MemberFavoriteResponse;
import com.yagubogu.member.dto.MemberInfoResponse;
import com.yagubogu.member.dto.MemberNicknameRequest;
import com.yagubogu.member.dto.MemberNicknameResponse;
import com.yagubogu.member.dto.MemberRepresentativeBadgeResponse;
import com.yagubogu.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequireRole
@RestController
public class MemberController implements MemberControllerInterface {

    private final MemberService memberService;
    private final AuthService authService;

    public ResponseEntity<MemberNicknameResponse> patchNickname(
            @RequestBody final MemberNicknameRequest request,
            final MemberClaims memberClaims
    ) {
        MemberNicknameResponse response = memberService.patchNickname(memberClaims.id(), request);

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Void> removeMember(
            final MemberClaims memberClaims
    ) {
        Long memberId = memberClaims.id();
        memberService.removeMember(memberId);
        authService.removeAllRefreshTokens(memberId);

        return ResponseEntity.noContent().build();
    }

    public ResponseEntity<MemberInfoResponse> findMember(
            final MemberClaims memberClaims
    ) {
        MemberInfoResponse response = memberService.findMember(memberClaims.id());

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<MemberNicknameResponse> findNickname(
            final MemberClaims memberClaims
    ) {
        MemberNicknameResponse response = memberService.findNickname(memberClaims.id());

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<MemberFavoriteResponse> findFavorites(
            final MemberClaims memberClaims
    ) {
        MemberFavoriteResponse response = memberService.findFavorite(memberClaims.id());

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<MemberFavoriteResponse> patchFavorites(
            final MemberClaims memberClaims,
            @RequestBody final MemberFavoriteRequest memberFavoriteRequest
    ) {
        MemberFavoriteResponse response = memberService.updateFavorite(memberClaims.id(), memberFavoriteRequest);

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<BadgeListResponse> findBadges(final MemberClaims memberClaims) {
        BadgeListResponse response = memberService.findBadges(memberClaims.id());

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<MemberRepresentativeBadgeResponse> patchRepresentativeBadge(
            final MemberClaims memberClaims,
            @PathVariable final long badgeId
    ) {
        MemberRepresentativeBadgeResponse response = memberService.patchRepresentativeBadge(memberClaims.id(), badgeId);

        return ResponseEntity.ok(response);
    }
}
