package org.services.fooddeliveryservice.repository;

import java.util.List;
import java.util.Optional;
import org.services.fooddeliveryservice.domain.DeliveryPartner;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryPartnerRepository extends JpaRepository<DeliveryPartner, Long> {
    @EntityGraph(attributePaths = "user")
    List<DeliveryPartner> findByAvailableTrue();

    Optional<DeliveryPartner> findByUserId(Long userId);
}
