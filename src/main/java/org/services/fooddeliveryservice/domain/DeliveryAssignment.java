package org.services.fooddeliveryservice.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "order_id"))
public class DeliveryAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    private FoodOrder order;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private DeliveryPartner deliveryPartner;

    private Instant assignedAt = Instant.now();

    protected DeliveryAssignment() {
    }

    public DeliveryAssignment(FoodOrder order, DeliveryPartner deliveryPartner) {
        this.order = order;
        this.deliveryPartner = deliveryPartner;
    }

    public Long getId() {
        return id;
    }

    public FoodOrder getOrder() {
        return order;
    }

    public DeliveryPartner getDeliveryPartner() {
        return deliveryPartner;
    }
}
