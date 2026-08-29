package com.yagubogu.place.repository;

import com.yagubogu.place.domain.Place;
import com.yagubogu.place.domain.PlaceCategory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {

    List<Place> findAllByStadiumIdAndCategory(Long stadiumId, PlaceCategory category);
}
