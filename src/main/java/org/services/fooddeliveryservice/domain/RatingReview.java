package org.services.fooddeliveryservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "order_id"))
public class RatingReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    private FoodOrder order;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private AppUser customer;

    private int rating;

    @Column(length = 1000)
    private String review;

    protected RatingReview() {
    }

    public RatingReview(FoodOrder order, AppUser customer, int rating, String review) {
        this.order = order;
        this.customer = customer;
        this.rating = rating;
        this.review = review;
    }

    public Long getId() {
        return id;
    }

    public int getRating() {
        return rating;
    }

    public String getReview() {
        return review;
    }
}
