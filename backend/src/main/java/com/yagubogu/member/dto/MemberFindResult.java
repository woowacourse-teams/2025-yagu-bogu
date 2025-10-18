package com.yagubogu.member.dto;

import com.yagubogu.member.domain.Member;

public record MemberFindResult(
        Member member,
        boolean isNew
) {
}
