package com.yagubogu.team.repository;

import com.yagubogu.team.domain.Team;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    Optional<Team> findByTeamCode(String teamCode);

    @Query("select t from Team t where t.id <> :teamId")
    List<Team> findOpponentsExcluding(@Param("teamId") Long teamId);
}
