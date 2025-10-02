package com.yagubogu.checkin.repository;

import com.yagubogu.checkin.domain.CheckIn;
import com.yagubogu.member.domain.Member;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckInRepository extends JpaRepository<CheckIn, Long>, CustomCheckInRepository {

    boolean existsByMemberAndGameDate(Member member, LocalDate date);

    @Query("""
                SELECT CASE
                         WHEN COUNT(c) = 1 THEN true
                         ELSE false
                       END
                FROM CheckIn c
                WHERE c.member = :member
                  AND c.game.stadium.id = :stadiumId
                  AND c.game.stadium.level = 'MAIN'
            """)
    boolean isFirstMainStadiumVisit(@Param("member") Member member, @Param("stadiumId") Long stadiumId);
}
