package org.services.fooddeliveryservice.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "food_orders")
public class FoodOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private AppUser customer;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Restaurant restaurant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PLACED;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Payment payment;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private DeliveryAssignment deliveryAssignment;

    private Instant createdAt = Instant.now();

    @Version
    private long version;

    protected FoodOrder() {
    }

    public FoodOrder(AppUser customer, Restaurant restaurant) {
        this.customer = customer;
        this.restaurant = restaurant;
    }

    public Long getId() {
        return id;
    }

    public AppUser getCustomer() {
        return customer;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public Payment getPayment() {
        return payment;
    }

    public DeliveryAssignment getDeliveryAssignment() {
        return deliveryAssignment;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void addItem(MenuItem menuItem, int quantity) {
        OrderItem item = new OrderItem(this, menuItem, menuItem.getName(), quantity, menuItem.getPrice());
        items.add(item);
        totalAmount = totalAmount.add(menuItem.getPrice().multiply(BigDecimal.valueOf(quantity)));
    }

    public void attachPayment(PaymentStatus status) {
        this.payment = new Payment(this, status, totalAmount);
    }

    public void attachAssignment(DeliveryAssignment assignment) {
        this.deliveryAssignment = assignment;
    }

    public void transitionTo(OrderStatus next) {
        boolean valid = switch (status) {
            case PLACED -> next == OrderStatus.ACCEPTED || next == OrderStatus.REJECTED || next == OrderStatus.CANCELLED;
            case ACCEPTED -> next == OrderStatus.PREPARING || next == OrderStatus.CANCELLED;
            case PREPARING -> next == OrderStatus.OUT_FOR_DELIVERY;
            case OUT_FOR_DELIVERY -> next == OrderStatus.DELIVERED;
            default -> false;
        };
        if (!valid) {
            throw new IllegalStateException("Invalid order status transition");
        }
        this.status = next;
    }
}
