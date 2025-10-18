package com.yagubogu.stadium.repository;

import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.domain.StadiumLevel;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StadiumRepository extends JpaRepository<Stadium, Long> {

    Optional<Stadium> findByShortName(String shortName);

    Optional<Stadium> findByLocation(String location);

    List<Stadium> findAllByLevel(StadiumLevel level);
}
