package com.yagubogu.support.badge;

import com.yagubogu.badge.domain.Badge;
import com.yagubogu.badge.domain.MemberBadge;
import com.yagubogu.member.domain.Member;

public class MemberBadgeBuilder {

    private Badge badge;
    private Member member;
    private int progress = 0;
    private boolean isAchieved = false;

    public MemberBadgeBuilder badge(final Badge badge) {
        this.badge = badge;

        return this;
    }

    public MemberBadgeBuilder member(final Member member) {
        this.member = member;

        return this;
    }

    public MemberBadgeBuilder isAchieved(final boolean isAchieved) {
        this.isAchieved = isAchieved;

        return this;
    }

    public MemberBadge build() {
        MemberBadge mb = new MemberBadge(badge, member);
        if (isAchieved) {
            mb.increaseProgress(badge.getThreshold());
        }
        return mb;
    }
}
