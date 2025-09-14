package com.yagubogu.support.badge;

import com.yagubogu.badge.domain.Badge;
import com.yagubogu.badge.domain.MemberBadge;
import com.yagubogu.member.domain.Member;

public class MemberBadgeBuilder {

    private Badge badge;
    private Member member;
    private double progress = 100.0;
    private Boolean representative = null;

    public MemberBadgeBuilder badge(final Badge badge) {
        this.badge = badge;

        return this;
    }

    public MemberBadgeBuilder member(final Member member) {
        this.member = member;

        return this;
    }

    public MemberBadge build() {
        return new MemberBadge(badge, member, progress, representative);
    }
}
