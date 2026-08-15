package com.yagubogu.widget.repository;

import com.yagubogu.member.domain.Member;
import com.yagubogu.widget.domain.WidgetDevice;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WidgetDeviceRepository extends JpaRepository<WidgetDevice, Long> {

    Optional<WidgetDevice> findByDeviceId(String deviceId);

    Optional<WidgetDevice> findByDeviceIdAndMemberId(String deviceId, Long memberId);

    void deleteAllByMember(Member member);
}
