package org.services.fooddeliveryservice.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
class OrderLifecycleUnitTest {
    @Test
    void deliveredOrderCannotMoveBackToPreparing() {
        FoodOrder order = new FoodOrder();
        order.transitionTo(OrderStatus.ACCEPTED);
        order.transitionTo(OrderStatus.PREPARING);
        order.transitionTo(OrderStatus.OUT_FOR_DELIVERY);
        order.transitionTo(OrderStatus.DELIVERED);

        assertThatThrownBy(() -> order.transitionTo(OrderStatus.PREPARING))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid order status transition");
    }
}
