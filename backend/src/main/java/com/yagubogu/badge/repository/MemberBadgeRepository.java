package com.yagubogu.badge.repository;

import com.yagubogu.badge.domain.Badge;
import com.yagubogu.badge.domain.MemberBadge;
import com.yagubogu.member.domain.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberBadgeRepository extends JpaRepository<MemberBadge, Long> {

    boolean existsByMemberAndBadge(Member member, Badge badge);

    @Query("""
                SELECT mb
                FROM MemberBadge mb
                WHERE mb.member = :member
                  AND mb.badge IN :badges
                  AND mb.progress >= mb.badge.threshold
            """)
    List<MemberBadge> findAcquiredBadges(@Param("member") Member member, @Param("badges") List<Badge> badges);


    Optional<MemberBadge> findByMemberAndBadge(Member member, Badge badge);
}
