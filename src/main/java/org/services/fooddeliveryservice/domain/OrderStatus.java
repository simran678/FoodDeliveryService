package org.services.fooddeliveryservice.domain;

public enum OrderStatus {
    PLACED,
    ACCEPTED,
    REJECTED,
    PREPARING,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED
}
