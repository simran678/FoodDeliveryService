package org.services.fooddeliveryservice.repository;

import java.util.Optional;
import org.services.fooddeliveryservice.domain.DeliveryPartner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryPartnerRepository extends JpaRepository<DeliveryPartner, Long> {
    Optional<DeliveryPartner> findByUserId(Long userId);
}
