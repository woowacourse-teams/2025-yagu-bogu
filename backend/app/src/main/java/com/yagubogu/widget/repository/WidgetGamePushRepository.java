package com.yagubogu.widget.repository;

import com.yagubogu.widget.domain.WidgetGamePush;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WidgetGamePushRepository extends JpaRepository<WidgetGamePush, Long> {
}
