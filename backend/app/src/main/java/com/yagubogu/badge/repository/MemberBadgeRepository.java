package com.yagubogu.badge.repository;

import com.yagubogu.badge.domain.Badge;
import com.yagubogu.badge.domain.MemberBadge;
import com.yagubogu.member.domain.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberBadgeRepository extends JpaRepository<MemberBadge, Long> {

    Optional<MemberBadge> findByMemberAndBadge(Member member, Badge badge);

    boolean existsByMemberAndBadgeAndIsAchievedTrue(Member member, Badge badge);
}
