package com.yagubogu.widget.repository;

import com.yagubogu.widget.domain.WidgetSettings;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WidgetSettingsRepository extends JpaRepository<WidgetSettings, Long> {

    Optional<WidgetSettings> findByMemberId(Long memberId);
}
