package com.yagubogu.badge.repository;

import com.yagubogu.badge.domain.Badge;
import com.yagubogu.badge.domain.Policy;
import com.yagubogu.badge.dto.BadgeResponse;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BadgeRepository extends JpaRepository<Badge, Long> {
    Badge findByType(Policy type);

    @Query("""
            SELECT new com.yagubogu.badge.dto.BadgeResponse(
                 b.id, b.name, b.description, b.type, b.achievedRate, mb.progress,
                 CASE WHEN mb.id IS NOT NULL THEN true ELSE false END,
                 mb.createdAt
            )
            FROM Badge b
            LEFT JOIN MemberBadge mb
            ON b.id = mb.badge.id AND mb.member.id = :memberId
            """)
    List<BadgeResponse> findAllBadgesWithAcquiredBadgeStatus(@Param("memberId") Long memberId);
}
