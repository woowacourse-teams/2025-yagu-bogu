package com.yagubogu.stadium.repository;

import com.yagubogu.stadium.domain.Stadium;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StadiumRepository extends JpaRepository<Stadium, Long> {

    Optional<Stadium> findByShortName(String shortName);
}
