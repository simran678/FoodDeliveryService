package org.services.fooddeliveryservice.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.Instant;

@Entity
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private AppUser recipient;

    private String message;
    private Instant createdAt = Instant.now();

    protected Notification() {
    }

    public Notification(AppUser recipient, String message) {
        this.recipient = recipient;
        this.message = message;
    }
}
