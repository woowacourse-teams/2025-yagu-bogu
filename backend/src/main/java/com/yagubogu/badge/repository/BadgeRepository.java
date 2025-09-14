package com.yagubogu.badge.repository;

import com.yagubogu.badge.domain.Badge;
import com.yagubogu.badge.domain.Policy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BadgeRepository extends JpaRepository<Badge, Long> {
    Badge findByType(Policy type);
}
