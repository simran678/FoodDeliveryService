package org.services.fooddeliveryservice.repository;

import java.util.List;
import org.services.fooddeliveryservice.domain.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    List<Restaurant> findByCityId(Long cityId);
}
