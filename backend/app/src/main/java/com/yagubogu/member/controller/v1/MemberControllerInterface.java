package com.yagubogu.member.controller.v1;

import com.yagubogu.auth.dto.MemberClaims;
import com.yagubogu.badge.dto.BadgeListResponse;
import com.yagubogu.member.dto.v1.MemberFavoriteRequest;
import com.yagubogu.member.dto.v1.MemberFavoriteResponse;
import com.yagubogu.member.dto.v1.MemberInfoResponse;
import com.yagubogu.member.dto.v1.MemberNicknameRequest;
import com.yagubogu.member.dto.v1.MemberNicknameResponse;
import com.yagubogu.member.dto.v1.MemberProfileResponse;
import com.yagubogu.member.dto.v1.MemberRepresentativeBadgeResponse;
import com.yagubogu.member.dto.v1.PreSignedUrlCompleteRequest;
import com.yagubogu.member.dto.v1.PreSignedUrlCompleteResponse;
import com.yagubogu.member.dto.v1.PreSignedUrlStartRequest;
import com.yagubogu.member.dto.v1.PresignedUrlStartResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Member", description = "회원 관련 API")
@RequestMapping("/members")
public interface MemberControllerInterface {

    @Operation(summary = "닉네임 수정", description = "현재 로그인된 회원의 닉네임을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "닉네임 수정 성공"),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
    })
    @PatchMapping("/me/nickname")
    ResponseEntity<MemberNicknameResponse> patchNickname(
            @RequestBody MemberNicknameRequest request,
            @Parameter(hidden = true) MemberClaims memberClaims
    );

    @Operation(summary = "회원 탈퇴", description = "현재 로그인된 회원의 계정을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "회원 삭제 성공")
    })
    @DeleteMapping("/me")
    ResponseEntity<Void> removeMember(@Parameter(hidden = true) MemberClaims memberClaims);

    @Operation(summary = "회원 정보 조회", description = "현재 로그인된 회원의 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원 정보 조회 성공"),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
    })
    @GetMapping("/me")
    ResponseEntity<MemberInfoResponse> findMember(@Parameter(hidden = true) MemberClaims memberClaims);

    @Operation(summary = "닉네임 조회", description = "현재 로그인된 회원의 닉네임을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "닉네임 조회 성공"),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
    })
    @GetMapping("/me/nickname")
    ResponseEntity<MemberNicknameResponse> findNickname(@Parameter(hidden = true) MemberClaims memberClaims);

    @Operation(summary = "응원팀 조회", description = "현재 로그인된 회원의 응원팀 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "응원팀 조회 성공"),
            @ApiResponse(responseCode = "404", description = "회원 또는 팀을 찾을 수 없음")
    })
    @GetMapping("/favorites")
    ResponseEntity<MemberFavoriteResponse> findFavorites(@Parameter(hidden = true) MemberClaims memberClaims);

    @Operation(summary = "응원팀 수정", description = "현재 로그인된 회원의 응원팀 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "응원팀 수정 성공"),
            @ApiResponse(responseCode = "404", description = "회원 또는 팀을 찾을 수 없음")
    })
    @PatchMapping("/favorites")
    ResponseEntity<MemberFavoriteResponse> patchFavorites(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @RequestBody MemberFavoriteRequest request
    );

    @Operation(summary = "pre-signed url 조회", description = "프로필 사진 저장을 위한 url 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "pre-signed url 조회 성공"),
            @ApiResponse(responseCode = "413", description = "contentLength가 기준을 초과함")
    })
    @PostMapping("/me/profile-image/pre-signed")
    ResponseEntity<PresignedUrlStartResponse> generatePresignedUrl(
            @RequestBody PreSignedUrlStartRequest preSignedUrlStartRequest
    );

    @Operation(summary = "회원 프로필 이미지 수정", description = "프로필 사진 저장을 완료하고 회원의 프로필 사진으로 저장합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원 프로필 이미지 수정성공"),
            @ApiResponse(responseCode = "404", description = "key로 s3에서 이미지를 찾을 수 없음")
    })
    @PostMapping("/me/profile-image/update")
    ResponseEntity<PreSignedUrlCompleteResponse> updateProfileImage(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @RequestBody PreSignedUrlCompleteRequest preSignedUrlCompleteRequest
    );

    @Operation(summary = "뱃지 조회", description = "모든 뱃지와 현재 로그인된 회원이 보유한 뱃지를 보여준다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "뱃지 조회 성공"),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
    })
    @GetMapping("/me/badges")
    ResponseEntity<BadgeListResponse> findBadges(@Parameter(hidden = true) MemberClaims memberClaims);

    @Operation(summary = "대표 뱃지 수정", description = "현재 로그인된 회원의 대표 뱃지를 수정한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "대표 뱃지 수정 성공")
    })
    @PatchMapping("/me/badges/{badgeId}/representative")
    ResponseEntity<MemberRepresentativeBadgeResponse> patchRepresentativeBadge(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @PathVariable final long badgeId
    );

    @Operation(summary = "프로필 조회", description = "memberId에 해당하는 멤버 프로필 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "프로필 조회 성공"),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
    })
    @GetMapping("/{memberId}")
    ResponseEntity<MemberProfileResponse> findMemberProfile(
            @PathVariable Long memberId
    );
}
