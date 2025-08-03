package com.yagubogu.auth.dto;

import com.yagubogu.member.domain.Member;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        boolean isNew,
        MemberResponse user
) {
    public record MemberResponse(
            long id,
            String nickname,
            String profileImageUrl
    ) {
        public static MemberResponse from(final Member member) {
            return new MemberResponse(member.getId(), member.getNickname(), member.getImage());
        }
    }
}
