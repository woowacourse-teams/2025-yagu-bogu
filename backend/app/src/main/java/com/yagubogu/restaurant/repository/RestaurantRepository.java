package com.yagubogu.restaurant.repository;

import com.yagubogu.restaurant.domain.Restaurant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    List<Restaurant> findAllByStadiumId(Long stadiumId);
}
