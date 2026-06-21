package org.services.fooddeliveryservice.repository;

import org.services.fooddeliveryservice.domain.RatingReview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RatingReviewRepository extends JpaRepository<RatingReview, Long> {
    boolean existsByOrderId(Long orderId);
}
