package com.yagubogu.badge.repository;

import com.yagubogu.badge.domain.BadgeUpdateQueue;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BadgeUpdateCounterRepository extends CrudRepository<BadgeUpdateQueue, Long> {
}
