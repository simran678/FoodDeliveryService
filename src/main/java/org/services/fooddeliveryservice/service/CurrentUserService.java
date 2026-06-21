package org.services.fooddeliveryservice.service;

import org.services.fooddeliveryservice.domain.AppUser;
import org.services.fooddeliveryservice.exception.UnauthorizedResourceAccessException;
import org.services.fooddeliveryservice.repository.AppUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {
    private final AppUserRepository userRepository;

    public CurrentUserService(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AppUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedResourceAccessException("Authentication required");
        }
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new UnauthorizedResourceAccessException("Authenticated user is not registered"));
    }
}
