package com.yagubogu.checkin.event;

import com.yagubogu.badge.BadgeEvent;
import com.yagubogu.badge.domain.Policy;
import com.yagubogu.member.domain.Member;

public record CheckInEvent(Member member) implements BadgeEvent {

    @Override
    public Policy policy() {
        return Policy.CHECK_IN;
    }
}
