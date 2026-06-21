package org.services.fooddeliveryservice.service;

import java.util.List;
import org.services.fooddeliveryservice.domain.AppUser;
import org.services.fooddeliveryservice.domain.MenuItem;
import org.services.fooddeliveryservice.domain.Restaurant;
import org.services.fooddeliveryservice.dto.Requests.MenuItemRequest;
import org.services.fooddeliveryservice.dto.Requests.MenuItemUpdateRequest;
import org.services.fooddeliveryservice.dto.Responses.MenuItemResponse;
import org.services.fooddeliveryservice.dto.Responses.RestaurantResponse;
import org.services.fooddeliveryservice.exception.ResourceNotFoundException;
import org.services.fooddeliveryservice.exception.UnauthorizedResourceAccessException;
import org.services.fooddeliveryservice.repository.CityRepository;
import org.services.fooddeliveryservice.repository.MenuItemRepository;
import org.services.fooddeliveryservice.repository.RestaurantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RestaurantService {
    private final CityRepository cityRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;

    public RestaurantService(CityRepository cityRepository, RestaurantRepository restaurantRepository, MenuItemRepository menuItemRepository) {
        this.cityRepository = cityRepository;
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponse> listRestaurants(Long cityId) {
        if (cityId != null && !cityRepository.existsById(cityId)) {
            throw new ResourceNotFoundException("City not found");
        }
        List<Restaurant> restaurants = cityId == null
                ? restaurantRepository.findAll()
                : restaurantRepository.findByCityId(cityId);
        return restaurants.stream().map(RestaurantResponse::from).toList();
    }

    public List<MenuItemResponse> listMenu(Long restaurantId) {
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new ResourceNotFoundException("Restaurant not found");
        }
        return menuItemRepository.findByRestaurantIdAndActiveTrue(restaurantId).stream().map(MenuItemResponse::from).toList();
    }

    @Transactional
    public MenuItemResponse addMenuItem(Long restaurantId, MenuItemRequest request, AppUser owner) {
        Restaurant restaurant = ownedRestaurant(restaurantId, owner);
        return MenuItemResponse.from(menuItemRepository.save(new MenuItem(restaurant, request.name(), request.price(), request.stock())));
    }

    @Transactional
    public MenuItemResponse updateMenuItem(Long restaurantId, Long menuItemId, MenuItemUpdateRequest request, AppUser owner) {
        ownedRestaurant(restaurantId, owner);
        MenuItem item = menuItemRepository.findByIdAndRestaurantId(menuItemId, restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));
        item.update(request.name(), request.price(), request.stock(), request.active());
        return MenuItemResponse.from(item);
    }

    private Restaurant ownedRestaurant(Long restaurantId, AppUser owner) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
        if (!restaurant.getOwner().getId().equals(owner.getId())) {
            throw new UnauthorizedResourceAccessException("Restaurant owner cannot modify another restaurant");
        }
        return restaurant;
    }
}
