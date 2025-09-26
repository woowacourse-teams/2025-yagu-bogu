package com.yagubogu.auth.event;

import com.yagubogu.badge.BadgeEvent;
import com.yagubogu.badge.domain.Policy;
import com.yagubogu.member.domain.Member;

public record SignUpEvent(Member member) implements BadgeEvent {

    @Override
    public Policy policy() {
        return Policy.SIGN_UP;
    }
}
