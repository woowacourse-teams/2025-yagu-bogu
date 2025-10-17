package com.yagubogu.talk.dto.event;

import com.yagubogu.badge.BadgeEvent;
import com.yagubogu.badge.domain.Policy;
import com.yagubogu.member.domain.Member;

public record TalkEvent(Member member) implements BadgeEvent {

    @Override
    public Policy policy() {
        return Policy.CHAT;
    }
}
