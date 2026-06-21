package org.services.fooddeliveryservice.controller;

import jakarta.validation.Valid;
import org.services.fooddeliveryservice.dto.Requests.CityRequest;
import org.services.fooddeliveryservice.dto.Requests.RestaurantRequest;
import org.services.fooddeliveryservice.dto.Requests.UserRequest;
import org.services.fooddeliveryservice.dto.Responses.IdResponse;
import org.services.fooddeliveryservice.dto.Responses.RestaurantResponse;
import org.services.fooddeliveryservice.dto.Responses.UserResponse;
import org.services.fooddeliveryservice.service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
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

    @PostMapping("/restaurants")
    @ResponseStatus(HttpStatus.CREATED)
    public RestaurantResponse createRestaurant(@Valid @RequestBody RestaurantRequest request) {
        return adminService.createRestaurant(request);
    }
}
