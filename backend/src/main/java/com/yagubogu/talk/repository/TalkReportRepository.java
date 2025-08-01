package com.yagubogu.talk.repository;

import com.yagubogu.talk.domain.TalkReport;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TalkReportRepository extends JpaRepository<TalkReport, Long> {

    boolean existsByTalkIdAndReporterId(long talkId, long reporterId);

    @Query("""
             SELECT tr.talk.id 
             FROM TalkReport tr 
             WHERE tr.reporter.id = :memberId AND tr.talk.id IN :talkIds
            """)
    List<Long> findTalkIdsByMemberIdAndTalkIds(long memberId, List<Long> talkIds);
}
