package org.services.fooddeliveryservice.repository;

import java.util.Optional;
import org.services.fooddeliveryservice.domain.DeliveryAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryAssignmentRepository extends JpaRepository<DeliveryAssignment, Long> {
    boolean existsByOrderId(Long orderId);

    Optional<DeliveryAssignment> findByOrderId(Long orderId);
}
