package org.services.fooddeliveryservice.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.services.fooddeliveryservice.dto.Requests.MenuItemRequest;
import org.services.fooddeliveryservice.dto.Requests.MenuItemUpdateRequest;
import org.services.fooddeliveryservice.dto.Responses.MenuItemResponse;
import org.services.fooddeliveryservice.dto.Responses.RestaurantResponse;
import org.services.fooddeliveryservice.service.CurrentUserService;
import org.services.fooddeliveryservice.service.RestaurantService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {
    private final RestaurantService restaurantService;
    private final CurrentUserService currentUserService;

    public RestaurantController(RestaurantService restaurantService, CurrentUserService currentUserService) {
        this.restaurantService = restaurantService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public List<RestaurantResponse> restaurants(@RequestParam(required = false) Long cityId) {
        return restaurantService.listRestaurants(cityId);
    }

    @GetMapping("/{restaurantId}/menu-items")
    public List<MenuItemResponse> menu(@PathVariable Long restaurantId) {
        return restaurantService.listMenu(restaurantId);
    }

    @PostMapping("/{restaurantId}/menu-items")
    @ResponseStatus(HttpStatus.CREATED)
    public MenuItemResponse addMenuItem(@PathVariable Long restaurantId, @Valid @RequestBody MenuItemRequest request) {
        return restaurantService.addMenuItem(restaurantId, request, currentUserService.currentUser());
    }

    @PatchMapping("/{restaurantId}/menu-items/{menuItemId}")
    public MenuItemResponse updateMenuItem(@PathVariable Long restaurantId, @PathVariable Long menuItemId,
                                           @Valid @RequestBody MenuItemUpdateRequest request) {
        return restaurantService.updateMenuItem(restaurantId, menuItemId, request, currentUserService.currentUser());
    }
}
