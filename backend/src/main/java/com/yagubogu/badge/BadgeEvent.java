package com.yagubogu.badge;

import com.yagubogu.badge.domain.Policy;
import com.yagubogu.member.domain.Member;

public interface BadgeEvent {

    Member member();

    Policy policy();
}
