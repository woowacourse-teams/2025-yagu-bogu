package com.yagubogu.checkin.event;

import com.yagubogu.badge.BadgeEvent;
import com.yagubogu.badge.domain.Policy;
import com.yagubogu.member.domain.Member;

public record StadiumVisitEvent(
        Member member,
        Long stadiumId
) implements BadgeEvent {

    @Override
    public Member member() {
        return member;
    }

    @Override
    public Policy policy() {
        return Policy.GRAND_SLAM;
    }
}
