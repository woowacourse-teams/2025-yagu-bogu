package com.yagubogu.member.controller.v1;

import com.yagubogu.auth.annotation.RequireRole;
import com.yagubogu.auth.dto.MemberClaims;
import com.yagubogu.auth.service.AuthService;
import com.yagubogu.badge.dto.BadgeListResponse;
import com.yagubogu.member.dto.MemberProfileResponse;
import com.yagubogu.member.dto.v1.MemberFavoriteRequest;
import com.yagubogu.member.dto.v1.MemberFavoriteResponse;
import com.yagubogu.member.dto.v1.MemberInfoResponse;
import com.yagubogu.member.dto.v1.MemberNicknameRequest;
import com.yagubogu.member.dto.v1.MemberNicknameResponse;
import com.yagubogu.member.dto.v1.MemberRepresentativeBadgeResponse;
import com.yagubogu.member.dto.v1.PreSignedUrlCompleteRequest;
import com.yagubogu.member.dto.v1.PreSignedUrlCompleteResponse;
import com.yagubogu.member.dto.v1.PreSignedUrlStartRequest;
import com.yagubogu.member.dto.v1.PresignedUrlStartResponse;
import com.yagubogu.member.service.MemberService;
import com.yagubogu.member.service.ProfileImageService;
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
    private final ProfileImageService profileImageService;

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

    public ResponseEntity<PresignedUrlStartResponse> generatePresignedUrl(
            @RequestBody final PreSignedUrlStartRequest request
    ) {
        PresignedUrlStartResponse response = profileImageService.issuePreSignedUrl(request);

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<PreSignedUrlCompleteResponse> updateProfileImage(
            final MemberClaims memberClaims,
            @RequestBody final PreSignedUrlCompleteRequest request
    ) {
        PreSignedUrlCompleteResponse response = profileImageService.completeUpload(memberClaims.id(), request);

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

    public ResponseEntity<MemberProfileResponse> findProfileInformation(
            final MemberClaims memberClaims,
            @PathVariable final Long memberId
    ) {
        MemberProfileResponse response = memberService.findProfileInformation(memberClaims.id(), memberId);

        return ResponseEntity.ok(response);
    }
}
