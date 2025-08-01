package com.yagubogu.auth.dto;

import com.yagubogu.member.domain.Member;
import com.yagubogu.member.domain.Role;

public record MemberInfo(Long id, Role role) {

    public static MemberInfo from(final Member member) {
        return new MemberInfo(member.getId(), member.getRole());
    }
}
