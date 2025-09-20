package com.yagubogu.badge.repository;

import com.yagubogu.badge.domain.Badge;
import com.yagubogu.badge.domain.Policy;
import com.yagubogu.badge.dto.BadgeRawResponse;
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
                    CASE WHEN mb.id IS NOT NULL THEN true ELSE false END,
                    mb.createdAt,
                    COUNT(CASE WHEN mb2.progress >= b.threshold THEN 1 ELSE NULL END),
                    b.threshold
                )
                FROM Badge b
                LEFT JOIN MemberBadge mb
                    ON b.id = mb.badge.id AND mb.member.id = :memberId
                LEFT JOIN MemberBadge mb2
                    ON b.id = mb2.badge.id
                GROUP BY b.id, b.name, b.description, b.policy, mb.progress, mb.id, mb.createdAt, b.threshold
            """)
    List<BadgeRawResponse> findAllBadgesWithAchievedCount(@Param("memberId") Long memberId);

}
