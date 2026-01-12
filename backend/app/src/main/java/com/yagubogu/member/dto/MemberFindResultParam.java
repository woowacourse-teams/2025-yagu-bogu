package com.yagubogu.member.dto;

import com.yagubogu.member.domain.Member;

public record MemberFindResultParam(
        Member member,
        boolean isNew
) {
}
