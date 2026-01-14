package com.yagubogu.auth.dto;

import com.yagubogu.member.domain.Member;
import com.yagubogu.member.domain.Role;

public record MemberClaims(Long id, Role role) {

    public static MemberClaims from(final Member member) {
        return new MemberClaims(member.getId(), member.getRole());
    }
}
