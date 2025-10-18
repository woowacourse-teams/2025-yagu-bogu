package com.yagubogu.member.dto;

import com.yagubogu.member.domain.Nickname;

public record MemberNicknameResponse(
        String nickname
) {

    public static MemberNicknameResponse from(final Nickname nickname) {
        return new MemberNicknameResponse(nickname.getValue());
    }
}
