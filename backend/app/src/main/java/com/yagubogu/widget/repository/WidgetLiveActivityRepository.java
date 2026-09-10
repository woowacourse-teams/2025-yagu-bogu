package com.yagubogu.widget.repository;

import com.yagubogu.game.domain.Game;
import com.yagubogu.member.domain.Member;
import com.yagubogu.widget.domain.WidgetDevice;
import com.yagubogu.widget.domain.WidgetLiveActivity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WidgetLiveActivityRepository extends JpaRepository<WidgetLiveActivity, Long> {

    Optional<WidgetLiveActivity> findByDeviceAndGame(WidgetDevice device, Game game);

    Optional<WidgetLiveActivity> findByActivityId(String activityId);

    Optional<WidgetLiveActivity> findByActivityIdAndDeviceMemberId(String activityId, Long memberId);

    void deleteAllByDevice(WidgetDevice device);

    void deleteAllByDeviceMember(Member member);
}
