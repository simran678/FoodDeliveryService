package org.services.fooddeliveryservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Version;
import java.math.BigDecimal;

@Entity
public class MenuItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Restaurant restaurant;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int stock;

    private boolean active = true;

    @Version
    private long version;

    protected MenuItem() {
    }

    public MenuItem(Restaurant restaurant, String name, BigDecimal price, int stock) {
        this.restaurant = restaurant;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    public Long getId() {
        return id;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public boolean isActive() {
        return active;
    }

    public void update(String name, BigDecimal price, Integer stock, Boolean active) {
        if (name != null) {
            this.name = name;
        }
        if (price != null) {
            this.price = price;
        }
        if (stock != null) {
            this.stock = stock;
        }
        if (active != null) {
            this.active = active;
        }
    }

    public void deduct(int quantity) {
        if (quantity <= 0 || stock < quantity) {
            throw new IllegalStateException("Insufficient stock");
        }
        stock -= quantity;
    }
}
