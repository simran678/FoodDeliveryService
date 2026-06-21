package org.services.fooddeliveryservice.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.services.fooddeliveryservice.domain.AppUser;
import org.services.fooddeliveryservice.domain.City;
import org.services.fooddeliveryservice.domain.MenuItem;
import org.services.fooddeliveryservice.domain.Restaurant;
import org.services.fooddeliveryservice.domain.UserRole;
import org.services.fooddeliveryservice.exception.ApiException;
import org.services.fooddeliveryservice.repository.AppUserRepository;
import org.services.fooddeliveryservice.repository.CityRepository;
import org.services.fooddeliveryservice.repository.DeliveryAssignmentRepository;
import org.services.fooddeliveryservice.repository.DeliveryPartnerRepository;
import org.services.fooddeliveryservice.repository.FoodOrderRepository;
import org.services.fooddeliveryservice.repository.MenuItemRepository;
import org.services.fooddeliveryservice.repository.NotificationRepository;
import org.services.fooddeliveryservice.repository.RatingReviewRepository;
import org.services.fooddeliveryservice.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class RestaurantServiceIntegrationTest {
    @Autowired
    RestaurantService restaurantService;
    @Autowired
    AppUserRepository users;
    @Autowired
    CityRepository cities;
    @Autowired
    RestaurantRepository restaurants;
    @Autowired
    FoodOrderRepository orders;
    @Autowired
    DeliveryPartnerRepository deliveryPartners;
    @Autowired
    DeliveryAssignmentRepository deliveryAssignments;
    @Autowired
    RatingReviewRepository ratings;
    @Autowired
    NotificationRepository notifications;
    @Autowired
    MenuItemRepository menuItems;
    @Autowired
    PasswordEncoder encoder;

    @BeforeEach
    void setUp() {
        notifications.deleteAll();
        ratings.deleteAll();
        deliveryAssignments.deleteAll();
        orders.deleteAll();
        deliveryPartners.deleteAll();
        menuItems.deleteAll();
        restaurants.deleteAll();
        cities.deleteAll();
        users.deleteAll();

        AppUser owner = users.save(new AppUser("Owner", "restaurant-owner-test", encoder.encode("pw"), UserRole.RESTAURANT_OWNER));
        City city = cities.save(new City("Browse City"));
        Restaurant restaurant = restaurants.save(new Restaurant("Browse Restaurant", city, owner));
        menuItems.save(new MenuItem(restaurant, "Browse Item", BigDecimal.valueOf(99), 3));
    }

    @Test
    void listMenuRejectsMissingRestaurant() {
        assertThatThrownBy(() -> restaurantService.listMenu(9999L))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Restaurant not found");
    }

    @Test
    void listRestaurantsRejectsMissingCity() {
        assertThatThrownBy(() -> restaurantService.listRestaurants(9999L))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("City not found");
    }
}
