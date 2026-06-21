package org.services.fooddeliveryservice.service;

import org.services.fooddeliveryservice.domain.AppUser;
import org.services.fooddeliveryservice.domain.City;
import org.services.fooddeliveryservice.domain.DeliveryPartner;
import org.services.fooddeliveryservice.domain.Restaurant;
import org.services.fooddeliveryservice.domain.UserRole;
import org.services.fooddeliveryservice.dto.Requests.CityRequest;
import org.services.fooddeliveryservice.dto.Requests.RestaurantRequest;
import org.services.fooddeliveryservice.dto.Requests.UserRequest;
import org.services.fooddeliveryservice.dto.Responses.IdResponse;
import org.services.fooddeliveryservice.dto.Responses.RestaurantResponse;
import org.services.fooddeliveryservice.dto.Responses.UserResponse;
import org.services.fooddeliveryservice.exception.InvalidRequestException;
import org.services.fooddeliveryservice.exception.ResourceNotFoundException;
import org.services.fooddeliveryservice.repository.AppUserRepository;
import org.services.fooddeliveryservice.repository.CityRepository;
import org.services.fooddeliveryservice.repository.DeliveryPartnerRepository;
import org.services.fooddeliveryservice.repository.RestaurantRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {
    private final AppUserRepository userRepository;
    private final CityRepository cityRepository;
    private final RestaurantRepository restaurantRepository;
    private final DeliveryPartnerRepository deliveryPartnerRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(AppUserRepository userRepository, CityRepository cityRepository,
                        RestaurantRepository restaurantRepository, DeliveryPartnerRepository deliveryPartnerRepository,
                        PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.cityRepository = cityRepository;
        this.restaurantRepository = restaurantRepository;
        this.deliveryPartnerRepository = deliveryPartnerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public IdResponse createCity(CityRequest request) {
        return new IdResponse(cityRepository.save(new City(request.name())).getId());
    }

    @Transactional
    public UserResponse createRestaurantOwner(UserRequest request) {
        AppUser user = userRepository.save(new AppUser(request.name(), request.username(),
                passwordEncoder.encode(request.password()), UserRole.RESTAURANT_OWNER));
        return new UserResponse(user.getId(), user.getName(), user.getUsername(), user.getRole().name());
    }

    @Transactional
    public UserResponse createDeliveryPartner(UserRequest request) {
        AppUser user = userRepository.save(new AppUser(request.name(), request.username(),
                passwordEncoder.encode(request.password()), UserRole.DELIVERY_PARTNER));
        deliveryPartnerRepository.save(new DeliveryPartner(user));
        return new UserResponse(user.getId(), user.getName(), user.getUsername(), user.getRole().name());
    }

    @Transactional
    public RestaurantResponse createRestaurant(RestaurantRequest request) {
        City city = cityRepository.findById(request.cityId()).orElseThrow(() -> new ResourceNotFoundException("City not found"));
        AppUser owner = userRepository.findById(request.ownerId()).orElseThrow(() -> new ResourceNotFoundException("Owner not found"));
        if (owner.getRole() != UserRole.RESTAURANT_OWNER) {
            throw new InvalidRequestException("Owner must have RESTAURANT_OWNER role", "INVALID_OWNER_ROLE");
        }
        return RestaurantResponse.from(restaurantRepository.save(new Restaurant(request.name(), city, owner)));
    }
}
