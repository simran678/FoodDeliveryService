package org.services.fooddeliveryservice.repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.services.fooddeliveryservice.domain.FoodOrder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface FoodOrderRepository extends JpaRepository<FoodOrder, Long> {
    @EntityGraph(attributePaths = {
            "customer",
            "restaurant",
            "payment",
            "deliveryAssignment",
            "deliveryAssignment.deliveryPartner",
            "items",
            "items.menuItem"
    })
    Optional<FoodOrder> findByIdAndCustomerId(Long id, Long customerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from FoodOrder o where o.id = :id")
    Optional<FoodOrder> findByIdForUpdate(Long id);
}
