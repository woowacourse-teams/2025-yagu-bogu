package com.yagubogu.member.dto;

public record MemberNicknameResponse(
        String nickname
) {
    public static MemberNicknameResponse from(final String nickname) {
        return new MemberNicknameResponse(nickname);
    }
}
