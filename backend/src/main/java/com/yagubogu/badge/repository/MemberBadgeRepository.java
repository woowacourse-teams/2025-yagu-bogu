package com.yagubogu.badge.repository;

import com.yagubogu.badge.domain.Badge;
import com.yagubogu.badge.domain.MemberBadge;
import com.yagubogu.badge.dto.BadgeCountResponse;
import com.yagubogu.member.domain.Member;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberBadgeRepository extends JpaRepository<MemberBadge, Long> {

    boolean existsByMemberAndBadge(Member member, Badge badge);

    @Query("""
            SELECT ROUND(100.0 * COUNT(mb) / COUNT(m), 1)
            FROM Member m
            LEFT JOIN MemberBadge mb
            ON mb.member.id = m.id AND mb.badge = :badge
            """)
    double calculateAchievedRate(@Param("badge") Badge badge);

    @Query("""
                SELECT new com.yagubogu.badge.dto.BadgeCountResponse(mb.badge.id, COUNT(mb))
                FROM MemberBadge mb
                GROUP BY mb.badge.id
            """)
    List<BadgeCountResponse> countByBadge();
}
