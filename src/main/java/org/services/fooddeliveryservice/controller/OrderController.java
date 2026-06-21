package org.services.fooddeliveryservice.controller;

import jakarta.validation.Valid;
import org.services.fooddeliveryservice.dto.Requests.PlaceOrderRequest;
import org.services.fooddeliveryservice.dto.Requests.RatingReviewRequest;
import org.services.fooddeliveryservice.dto.Responses.IdResponse;
import org.services.fooddeliveryservice.dto.Responses.OrderResponse;
import org.services.fooddeliveryservice.service.CurrentUserService;
import org.services.fooddeliveryservice.service.OrderService;
import org.services.fooddeliveryservice.service.RatingReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;
    private final RatingReviewService ratingReviewService;
    private final CurrentUserService currentUserService;

    public OrderController(OrderService orderService, RatingReviewService ratingReviewService, CurrentUserService currentUserService) {
        this.orderService = orderService;
        this.ratingReviewService = ratingReviewService;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
        return orderService.placeOrder(request, currentUserService.currentUser());
    }

    @GetMapping("/{orderId}")
    public OrderResponse track(@PathVariable Long orderId) {
        return orderService.customerOrder(orderId, currentUserService.currentUser());
    }

    @PostMapping("/{orderId}/ratings")
    @ResponseStatus(HttpStatus.CREATED)
    public IdResponse rate(@PathVariable Long orderId, @Valid @RequestBody RatingReviewRequest request) {
        return ratingReviewService.rate(orderId, request, currentUserService.currentUser());
    }
}
