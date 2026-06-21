package org.services.fooddeliveryservice.controller;

import org.services.fooddeliveryservice.domain.OrderStatus;
import org.services.fooddeliveryservice.dto.Responses.OrderResponse;
import org.services.fooddeliveryservice.service.CurrentUserService;
import org.services.fooddeliveryservice.service.OrderService;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/restaurant-owner/orders")
public class RestaurantOwnerOrderController {
    private final OrderService orderService;
    private final CurrentUserService currentUserService;

    public RestaurantOwnerOrderController(OrderService orderService, CurrentUserService currentUserService) {
        this.orderService = orderService;
        this.currentUserService = currentUserService;
    }

    @PatchMapping("/{orderId}/accept")
    public OrderResponse accept(@PathVariable Long orderId) {
        return orderService.ownerTransition(orderId, currentUserService.currentUser(), OrderStatus.ACCEPTED);
    }

    @PatchMapping("/{orderId}/reject")
    public OrderResponse reject(@PathVariable Long orderId) {
        return orderService.ownerTransition(orderId, currentUserService.currentUser(), OrderStatus.REJECTED);
    }

    @PatchMapping("/{orderId}/preparing")
    public OrderResponse preparing(@PathVariable Long orderId) {
        return orderService.ownerTransition(orderId, currentUserService.currentUser(), OrderStatus.PREPARING);
    }
}
