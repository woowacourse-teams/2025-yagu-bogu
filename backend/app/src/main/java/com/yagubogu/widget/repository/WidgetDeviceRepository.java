package com.yagubogu.widget.repository;

import com.yagubogu.team.domain.Team;
import com.yagubogu.widget.domain.WidgetDevice;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WidgetDeviceRepository extends JpaRepository<WidgetDevice, String> {

    Optional<WidgetDevice> findByDeviceIdAndMemberId(String deviceId, Long memberId);

    List<WidgetDevice> findAllByMemberId(Long memberId);

    /**
     * 특정 경기의 홈팀 또는 원정팀을 응원하는 회원의 디바이스 목록을 조회합니다.
     * - 탈퇴하지 않은 회원(deletedAt IS NULL)만 포함
     * - widget_settings.enabled = false 로 명시적으로 끈 회원은 제외
     */
    @Query("""
            SELECT wd FROM WidgetDevice wd
            JOIN wd.member m
            WHERE (m.team = :homeTeam OR m.team = :awayTeam)
              AND m.deletedAt IS NULL
              AND NOT EXISTS (
                  SELECT ws FROM WidgetSettings ws
                  WHERE ws.memberId = m.id AND ws.enabled = false
              )
            """)
    List<WidgetDevice> findDevicesForGame(@Param("homeTeam") Team homeTeam,
                                         @Param("awayTeam") Team awayTeam);
}
