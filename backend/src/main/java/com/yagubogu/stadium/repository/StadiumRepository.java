package com.yagubogu.stadium.repository;

import com.yagubogu.stadium.domain.Stadium;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StadiumRepository extends JpaRepository<Stadium, Long> {

    @Query("SELECT s FROM Stadium s WHERE s.location = :name OR s.shortName = :name OR s.fullName = :name")
    Optional<Stadium> findStadiumByName(@Param("name") String name);

}
