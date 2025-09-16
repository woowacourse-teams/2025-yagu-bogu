package com.yagubogu.stadium.repository;

import com.yagubogu.stadium.domain.Stadium;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface StadiumRepository extends JpaRepository<Stadium, Long> {

    Optional<Stadium> findByShortName(String shortName);

    @Query("""
            select s
            from Game g
            join g.stadium s
            where g.date = :date
            """)
    List<Stadium> findStadiumsByGameDate(LocalDate date);
}
