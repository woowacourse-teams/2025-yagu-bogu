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
                        COALESCE(currentUserMb.progress, 0),
                        COALESCE(currentUserMb.isAchieved, false),
                        currentUserMb.achievedAt,
                        COUNT(achievedMb.id),
                        b.threshold,
                        b.badgeImageUrl
                    )
                    FROM Badge b
                    LEFT JOIN MemberBadge currentUserMb
                        ON b.id = currentUserMb.badge.id AND currentUserMb.member.id = :memberId
                    LEFT JOIN MemberBadge achievedMb
                        ON b.id = achievedMb.badge.id AND achievedMb.isAchieved = true
                    GROUP BY b.id, currentUserMb.progress, currentUserMb.isAchieved, currentUserMb.achievedAt
            """)
    List<BadgeRawResponse> findAllBadgesWithAchievedCount(@Param("memberId") Long memberId);

    @Query("""
                SELECT b
                FROM Badge b
                LEFT JOIN MemberBadge mb
                  ON mb.badge = b
                 AND mb.member = :member
                WHERE b.policy = :policy
                  AND (mb.id IS NULL OR mb.isAchieved = false)
            """)
    List<Badge> findNotAchievedBadges(Member member, Policy policy);
}
