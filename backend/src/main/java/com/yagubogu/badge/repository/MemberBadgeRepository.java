package com.yagubogu.badge.repository;

import com.yagubogu.badge.domain.Badge;
import com.yagubogu.badge.domain.MemberBadge;
import com.yagubogu.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberBadgeRepository extends JpaRepository<MemberBadge, Long> {
    boolean existsByMemberAndBadge(Member member, Badge badge);
}
