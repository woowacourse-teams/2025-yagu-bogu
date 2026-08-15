package com.yagubogu.widget.repository;

import com.yagubogu.widget.domain.WidgetLiveActivity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WidgetLiveActivityRepository extends JpaRepository<WidgetLiveActivity, String> {

    List<WidgetLiveActivity> findAllByDeviceDeviceId(String deviceId);

    /**
     * 특정 경기에 연결된 모든 iOS Live Activity를 조회합니다.
     * 갱신 및 종료 푸시 발송 시 사용됩니다.
     */
    List<WidgetLiveActivity> findAllByGameId(Long gameId);

    /**
     * 특정 기기에 이미 활성화된 Live Activity가 있는지 확인합니다.
     * 더블헤더 안전장치: 이미 켜진 위젯이 있으면 새 START를 보내지 않습니다.
     */
    boolean existsByDeviceDeviceId(String deviceId);
}
