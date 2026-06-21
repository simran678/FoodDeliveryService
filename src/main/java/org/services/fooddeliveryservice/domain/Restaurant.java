package org.services.fooddeliveryservice.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private City city;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private AppUser owner;

    protected Restaurant() {
    }

    public Restaurant(String name, City city, AppUser owner) {
        this.name = name;
        this.city = city;
        this.owner = owner;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public City getCity() {
        return city;
    }

    public AppUser getOwner() {
        return owner;
    }
}
