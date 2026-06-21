package org.services.fooddeliveryservice.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.services.fooddeliveryservice.dto.Requests.CityRequest;
import org.services.fooddeliveryservice.dto.Requests.RestaurantRequest;
import org.services.fooddeliveryservice.dto.Requests.UserRequest;
import org.services.fooddeliveryservice.dto.Responses.DeliveryPartnerResponse;
import org.services.fooddeliveryservice.dto.Responses.IdResponse;
import org.services.fooddeliveryservice.dto.Responses.OrderResponse;
import org.services.fooddeliveryservice.dto.Responses.RestaurantResponse;
import org.services.fooddeliveryservice.dto.Responses.UserResponse;
import org.services.fooddeliveryservice.service.AdminService;
import org.services.fooddeliveryservice.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService adminService;
    private final OrderService orderService;

    public AdminController(AdminService adminService, OrderService orderService) {
        this.adminService = adminService;
        this.orderService = orderService;
    }

    @PostMapping("/cities")
    @ResponseStatus(HttpStatus.CREATED)
    public IdResponse createCity(@Valid @RequestBody CityRequest request) {
        return adminService.createCity(request);
    }

    @PostMapping("/restaurant-owners")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createRestaurantOwner(@Valid @RequestBody UserRequest request) {
        return adminService.createRestaurantOwner(request);
    }

    @PostMapping("/delivery-partners")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createDeliveryPartner(@Valid @RequestBody UserRequest request) {
        return adminService.createDeliveryPartner(request);
    }

    @GetMapping("/delivery-partners/available")
    public List<DeliveryPartnerResponse> availableDeliveryPartners() {
        return adminService.availableDeliveryPartners();
    }

    @PatchMapping("/orders/{orderId}/delivery-partners/{partnerId}/assign")
    public OrderResponse assignDeliveryPartner(@PathVariable Long orderId, @PathVariable Long partnerId) {
        return orderService.assignDeliveryPartner(orderId, partnerId);
    }

    @PostMapping("/restaurants")
    @ResponseStatus(HttpStatus.CREATED)
    public RestaurantResponse createRestaurant(@Valid @RequestBody RestaurantRequest request) {
        return adminService.createRestaurant(request);
    }
}
