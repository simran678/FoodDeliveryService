package org.services.fooddeliveryservice.repository;

import org.services.fooddeliveryservice.domain.City;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CityRepository extends JpaRepository<City, Long> {
}
