package org.services.fooddeliveryservice.service;

import org.services.fooddeliveryservice.domain.AppUser;
import org.services.fooddeliveryservice.domain.FoodOrder;
import org.services.fooddeliveryservice.domain.OrderStatus;
import org.services.fooddeliveryservice.domain.RatingReview;
import org.services.fooddeliveryservice.dto.Requests.RatingReviewRequest;
import org.services.fooddeliveryservice.dto.Responses.IdResponse;
import org.services.fooddeliveryservice.exception.ApiException;
import org.services.fooddeliveryservice.repository.FoodOrderRepository;
import org.services.fooddeliveryservice.repository.RatingReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RatingReviewService {
    private final FoodOrderRepository orderRepository;
    private final RatingReviewRepository ratingReviewRepository;

    public RatingReviewService(FoodOrderRepository orderRepository, RatingReviewRepository ratingReviewRepository) {
        this.orderRepository = orderRepository;
        this.ratingReviewRepository = ratingReviewRepository;
    }

    @Transactional
    public IdResponse rate(Long orderId, RatingReviewRequest request, AppUser customer) {
        FoodOrder order = orderRepository.findById(orderId).orElseThrow(() -> ApiException.notFound("Order not found"));
        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw ApiException.forbidden("Customer cannot rate another customer's order");
        }
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw ApiException.badRequest("Rating is allowed only after delivery", "RATING_NOT_ALLOWED");
        }
        if (ratingReviewRepository.existsByOrderId(orderId)) {
            throw ApiException.conflict("Order already rated", "DUPLICATE_RATING");
        }
        RatingReview saved = ratingReviewRepository.save(new RatingReview(order, customer, request.rating(), request.review()));
        return new IdResponse(saved.getId());
    }
}
