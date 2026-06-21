package org.services.fooddeliveryservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import org.services.fooddeliveryservice.domain.PaymentStatus;

public final class Requests {
    private Requests() {
    }

    public record CityRequest(@NotBlank String name) {
    }

    public record UserRequest(@NotBlank String name, @NotBlank String username, @NotBlank String password) {
    }

    public record RestaurantRequest(@NotBlank String name, @NotNull Long cityId, @NotNull Long ownerId) {
    }

    public record MenuItemRequest(
            @NotBlank String name,
            @NotNull @DecimalMin("0.01") BigDecimal price,
            @Min(0) int stock) {
    }

    public record MenuItemUpdateRequest(
            String name,
            @DecimalMin("0.01") BigDecimal price,
            @Min(0) Integer stock,
            Boolean active) {
    }

    public record PlaceOrderRequest(
            @NotNull Long restaurantId,
            @NotEmpty List<@Valid OrderLineRequest> items,
            @NotNull PaymentStatus paymentStatus) {
    }

    public record OrderLineRequest(@NotNull Long menuItemId, @Min(1) int quantity) {
    }

    public record RatingReviewRequest(
            @Min(1) @Max(5) int rating,
            @Size(max = 1000) String review) {
    }
}
