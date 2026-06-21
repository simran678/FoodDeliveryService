package org.services.fooddeliveryservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.services.fooddeliveryservice.domain.AppUser;
import org.services.fooddeliveryservice.domain.City;
import org.services.fooddeliveryservice.domain.DeliveryPartner;
import org.services.fooddeliveryservice.domain.MenuItem;
import org.services.fooddeliveryservice.domain.OrderStatus;
import org.services.fooddeliveryservice.domain.PaymentStatus;
import org.services.fooddeliveryservice.domain.Restaurant;
import org.services.fooddeliveryservice.domain.UserRole;
import org.services.fooddeliveryservice.dto.Requests.OrderLineRequest;
import org.services.fooddeliveryservice.dto.Requests.PlaceOrderRequest;
import org.services.fooddeliveryservice.dto.Requests.RatingReviewRequest;
import org.services.fooddeliveryservice.dto.Responses.OrderResponse;
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
class OrderServiceIntegrationTest {
    @Autowired
    OrderService orderService;
    @Autowired
    RatingReviewService ratingReviewService;
    @Autowired
    AppUserRepository users;
    @Autowired
    CityRepository cities;
    @Autowired
    RestaurantRepository restaurants;
    @Autowired
    MenuItemRepository menuItems;
    @Autowired
    DeliveryPartnerRepository deliveryPartners;
    @Autowired
    DeliveryAssignmentRepository deliveryAssignments;
    @Autowired
    RatingReviewRepository ratings;
    @Autowired
    NotificationRepository notifications;
    @Autowired
    FoodOrderRepository orders;
    @Autowired
    PasswordEncoder encoder;

    AppUser owner;
    AppUser customer;
    AppUser otherCustomer;
    AppUser partnerUser;
    AppUser otherPartnerUser;
    Restaurant restaurant;
    MenuItem burger;
    MenuItem pizza;

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

        owner = users.save(new AppUser("Owner", "owner-test", encoder.encode("pw"), UserRole.RESTAURANT_OWNER));
        customer = users.save(new AppUser("Customer", "customer-test", encoder.encode("pw"), UserRole.CUSTOMER));
        otherCustomer = users.save(new AppUser("Other Customer", "other-customer-test", encoder.encode("pw"), UserRole.CUSTOMER));
        partnerUser = users.save(new AppUser("Partner", "partner-test", encoder.encode("pw"), UserRole.DELIVERY_PARTNER));
        otherPartnerUser = users.save(new AppUser("Other Partner", "other-partner-test", encoder.encode("pw"), UserRole.DELIVERY_PARTNER));
        deliveryPartners.save(new DeliveryPartner(partnerUser));
        deliveryPartners.save(new DeliveryPartner(otherPartnerUser));
        City city = cities.save(new City("Test City"));
        restaurant = restaurants.save(new Restaurant("Test Restaurant", city, owner));
        burger = menuItems.save(new MenuItem(restaurant, "Burger", BigDecimal.valueOf(100), 5));
        pizza = menuItems.save(new MenuItem(restaurant, "Pizza", BigDecimal.valueOf(200), 1));
    }

    @Test
    void successfulOrderPlacementDeductsStockAndPersistsPayment() {
        OrderResponse response = placeOrder(burger.getId(), 2);

        assertThat(response.status()).isEqualTo(OrderStatus.PLACED);
        assertThat(response.totalAmount()).isEqualByComparingTo("200");
        assertThat(response.paymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(menuItems.findById(burger.getId()).orElseThrow().getStock()).isEqualTo(3);
        assertThat(response.items()).hasSize(1);
        assertThat(orders.findById(response.id())).isPresent();
    }

    @Test
    void insufficientStockRejectsOrderAndDoesNotDeductStock() {
        assertThatThrownBy(() -> placeOrder(pizza.getId(), 2))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Insufficient stock");

        assertThat(menuItems.findById(pizza.getId()).orElseThrow().getStock()).isEqualTo(1);
        assertThat(orders.findAll()).isEmpty();
    }

    @Test
    void invalidOrderTransitionIsRejected() {
        OrderResponse response = placeOrder(burger.getId(), 1);

        assertThatThrownBy(() -> orderService.partnerTransition(response.id(), partnerUser, OrderStatus.DELIVERED))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Order is not assigned");
    }

    @Test
    void restaurantOwnerCanAcceptAndRejectPlacedOrders() {
        OrderResponse accepted = orderService.ownerTransition(placeOrder(burger.getId(), 1).id(), owner, OrderStatus.ACCEPTED);
        OrderResponse rejected = orderService.ownerTransition(placeOrder(burger.getId(), 1).id(), owner, OrderStatus.REJECTED);

        assertThat(accepted.status()).isEqualTo(OrderStatus.ACCEPTED);
        assertThat(rejected.status()).isEqualTo(OrderStatus.REJECTED);
    }

    @Test
    void onlyOneDeliveryPartnerCanAcceptOrder() {
        Long orderId = makePreparingOrder();

        OrderResponse assigned = orderService.acceptDelivery(orderId, partnerUser);
        assertThat(assigned.deliveryPartnerId()).isNotNull();
        assertThatThrownBy(() -> orderService.acceptDelivery(orderId, otherPartnerUser))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Order already assigned");
    }

    @Test
    void ratingAllowedOnlyAfterDeliveryAndByOwningCustomer() {
        Long orderId = makePreparingOrder();

        assertThatThrownBy(() -> ratingReviewService.rate(orderId, new RatingReviewRequest(5, "Too soon"), customer))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Rating is allowed only after delivery");

        orderService.acceptDelivery(orderId, partnerUser);
        orderService.partnerTransition(orderId, partnerUser, OrderStatus.OUT_FOR_DELIVERY);
        orderService.partnerTransition(orderId, partnerUser, OrderStatus.DELIVERED);

        assertThatThrownBy(() -> ratingReviewService.rate(orderId, new RatingReviewRequest(5, "Not mine"), otherCustomer))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Customer cannot rate");
        assertThat(ratingReviewService.rate(orderId, new RatingReviewRequest(5, "Great"), customer).id()).isNotNull();
    }

    private OrderResponse placeOrder(Long menuItemId, int quantity) {
        return orderService.placeOrder(new PlaceOrderRequest(
                restaurant.getId(),
                List.of(new OrderLineRequest(menuItemId, quantity)),
                PaymentStatus.SUCCESS), customer);
    }

    private Long makePreparingOrder() {
        Long orderId = placeOrder(burger.getId(), 1).id();
        orderService.ownerTransition(orderId, owner, OrderStatus.ACCEPTED);
        orderService.ownerTransition(orderId, owner, OrderStatus.PREPARING);
        return orderId;
    }
}
