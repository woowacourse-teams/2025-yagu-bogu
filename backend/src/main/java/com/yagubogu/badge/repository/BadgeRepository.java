package com.yagubogu.badge.repository;

import com.yagubogu.badge.domain.Badge;
import com.yagubogu.badge.domain.Policy;
import com.yagubogu.badge.dto.BadgeRawResponse;
import com.yagubogu.member.domain.Member;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BadgeRepository extends JpaRepository<Badge, Long> {

    List<Badge> findByPolicy(Policy policy);

    @Query("""
                SELECT new com.yagubogu.badge.dto.BadgeRawResponse(
                    b.id,
                    b.name,
                    b.description,
                    b.policy,
                    COALESCE(mb.progress, 0),
                    COALESCE(mb.achieved, false),
                    mb.achievedAt,
                    COUNT(CASE WHEN mb2.achieved = true THEN 1 ELSE NULL END),
                    b.threshold
                )
                FROM Badge b
                LEFT JOIN MemberBadge mb
                    ON b.id = mb.badge.id AND mb.member.id = :memberId
                LEFT JOIN MemberBadge mb2
                    ON b.id = mb2.badge.id
                GROUP BY b.id, b.name, b.description, b.policy, mb.progress, mb.achieved, mb.achievedAt, b.threshold
            """)
    List<BadgeRawResponse> findAllBadgesWithAchievedCount(@Param("memberId") Long memberId);

    @Query("""
                SELECT b
                FROM Badge b
                LEFT JOIN MemberBadge mb
                  ON mb.badge = b
                 AND mb.member = :member
                WHERE b.policy = :policy
                  AND (mb.id IS NULL OR mb.achieved = false)
            """)
    List<Badge> findNotAchievedBadges(Member member, Policy policy);
}
