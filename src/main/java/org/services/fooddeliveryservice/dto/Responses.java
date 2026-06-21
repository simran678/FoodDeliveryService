package org.services.fooddeliveryservice.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.services.fooddeliveryservice.domain.FoodOrder;
import org.services.fooddeliveryservice.domain.MenuItem;
import org.services.fooddeliveryservice.domain.OrderItem;
import org.services.fooddeliveryservice.domain.OrderStatus;
import org.services.fooddeliveryservice.domain.PaymentStatus;
import org.services.fooddeliveryservice.domain.Restaurant;

public final class Responses {
    private Responses() {
    }

    public record IdResponse(Long id) {
    }

    public record UserResponse(Long id, String name, String username, String role) {
    }

    public record RestaurantResponse(Long id, String name, Long cityId, String cityName, Long ownerId) {
        public static RestaurantResponse from(Restaurant restaurant) {
            return new RestaurantResponse(
                    restaurant.getId(),
                    restaurant.getName(),
                    restaurant.getCity().getId(),
                    restaurant.getCity().getName(),
                    restaurant.getOwner().getId());
        }
    }

    public record MenuItemResponse(Long id, String name, BigDecimal price, int stock, boolean active) {
        public static MenuItemResponse from(MenuItem item) {
            return new MenuItemResponse(item.getId(), item.getName(), item.getPrice(), item.getStock(), item.isActive());
        }
    }

    public record OrderItemResponse(Long menuItemId, String name, int quantity, BigDecimal unitPrice) {
        public static OrderItemResponse from(OrderItem item) {
            return new OrderItemResponse(item.getMenuItem().getId(), item.getItemName(), item.getQuantity(), item.getUnitPrice());
        }
    }

    public record OrderResponse(
            Long id,
            Long customerId,
            Long restaurantId,
            OrderStatus status,
            BigDecimal totalAmount,
            PaymentStatus paymentStatus,
            Long deliveryPartnerId,
            Instant createdAt,
            List<OrderItemResponse> items) {
        public static OrderResponse from(FoodOrder order) {
            Long partnerId = order.getDeliveryAssignment() == null
                    ? null
                    : order.getDeliveryAssignment().getDeliveryPartner().getId();
            return new OrderResponse(
                    order.getId(),
                    order.getCustomer().getId(),
                    order.getRestaurant().getId(),
                    order.getStatus(),
                    order.getTotalAmount(),
                    order.getPayment().getStatus(),
                    partnerId,
                    order.getCreatedAt(),
                    order.getItems().stream().map(OrderItemResponse::from).toList());
        }
    }

    public record ErrorResponse(int status, String message, String errorCode) {
    }
}
