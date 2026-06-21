package org.services.fooddeliveryservice.config;

import java.math.BigDecimal;
import org.services.fooddeliveryservice.domain.AppUser;
import org.services.fooddeliveryservice.domain.City;
import org.services.fooddeliveryservice.domain.DeliveryPartner;
import org.services.fooddeliveryservice.domain.MenuItem;
import org.services.fooddeliveryservice.domain.Restaurant;
import org.services.fooddeliveryservice.domain.UserRole;
import org.services.fooddeliveryservice.repository.AppUserRepository;
import org.services.fooddeliveryservice.repository.CityRepository;
import org.services.fooddeliveryservice.repository.DeliveryPartnerRepository;
import org.services.fooddeliveryservice.repository.MenuItemRepository;
import org.services.fooddeliveryservice.repository.RestaurantRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {
    @Bean
    @Profile("!test")
    CommandLineRunner seedData(AppUserRepository users, CityRepository cities, RestaurantRepository restaurants,
                               MenuItemRepository menuItems, DeliveryPartnerRepository partners,
                               PasswordEncoder encoder) {
        return args -> {
            if (users.existsByRole(UserRole.ADMIN)) {
                return;
            }
            AppUser admin = users.save(new AppUser("Admin", "admin", encoder.encode("admin"), UserRole.ADMIN));
            AppUser owner = users.save(new AppUser("Owner One", "owner", encoder.encode("owner"), UserRole.RESTAURANT_OWNER));
            AppUser ownerTwo = users.save(new AppUser("Owner Two", "owner2", encoder.encode("owner2"), UserRole.RESTAURANT_OWNER));
            AppUser customer = users.save(new AppUser("Customer One", "customer", encoder.encode("customer"), UserRole.CUSTOMER));
            AppUser customerTwo = users.save(new AppUser("Customer Two", "customer2", encoder.encode("customer2"), UserRole.CUSTOMER));
            AppUser partnerUser = users.save(new AppUser("Partner One", "partner", encoder.encode("partner"), UserRole.DELIVERY_PARTNER));
            AppUser partnerTwoUser = users.save(new AppUser("Partner Two", "partner2", encoder.encode("partner2"), UserRole.DELIVERY_PARTNER));
            AppUser partnerThreeUser = users.save(new AppUser("Partner Three", "partner3", encoder.encode("partner3"), UserRole.DELIVERY_PARTNER));
            partners.save(new DeliveryPartner(partnerUser));
            partners.save(new DeliveryPartner(partnerTwoUser));
            partners.save(new DeliveryPartner(partnerThreeUser));

            City bengaluru = cities.save(new City("Bengaluru"));
            City mumbai = cities.save(new City("Mumbai"));
            Restaurant seedKitchen = restaurants.save(new Restaurant("Seed Kitchen", bengaluru, owner));
            Restaurant coastalCart = restaurants.save(new Restaurant("Coastal Cart", mumbai, ownerTwo));
            menuItems.save(new MenuItem(seedKitchen, "Paneer Roll", BigDecimal.valueOf(149), 20));
            menuItems.save(new MenuItem(seedKitchen, "Veg Biryani", BigDecimal.valueOf(199), 15));
            menuItems.save(new MenuItem(seedKitchen, "Low Stock Thali", BigDecimal.valueOf(249), 1));
            menuItems.save(new MenuItem(coastalCart, "Fish Curry Rice", BigDecimal.valueOf(299), 10));
            menuItems.save(new MenuItem(coastalCart, "Sol Kadhi", BigDecimal.valueOf(99), 25));
            admin.getId();
            customer.getId();
            customerTwo.getId();
        };
    }
}
