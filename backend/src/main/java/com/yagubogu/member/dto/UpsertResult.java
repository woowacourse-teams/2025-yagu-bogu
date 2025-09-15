package com.yagubogu.member.dto;

import com.yagubogu.member.domain.Member;

public record UpsertResult(
        Member member,
        boolean isNew
) {
}
