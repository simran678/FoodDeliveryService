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
@RequestMapping("/api/delivery-partner/orders")
public class DeliveryPartnerOrderController {
    private final OrderService orderService;
    private final CurrentUserService currentUserService;

    public DeliveryPartnerOrderController(OrderService orderService, CurrentUserService currentUserService) {
        this.orderService = orderService;
        this.currentUserService = currentUserService;
    }

    @PatchMapping("/{orderId}/accept")
    public OrderResponse accept(@PathVariable Long orderId) {
        return orderService.acceptDelivery(orderId, currentUserService.currentUser());
    }

    @PatchMapping("/{orderId}/out-for-delivery")
    public OrderResponse outForDelivery(@PathVariable Long orderId) {
        return orderService.partnerTransition(orderId, currentUserService.currentUser(), OrderStatus.OUT_FOR_DELIVERY);
    }

    @PatchMapping("/{orderId}/delivered")
    public OrderResponse delivered(@PathVariable Long orderId) {
        return orderService.partnerTransition(orderId, currentUserService.currentUser(), OrderStatus.DELIVERED);
    }
}
