package org.services.fooddeliveryservice.repository;

import java.util.Optional;
import org.services.fooddeliveryservice.domain.AppUser;
import org.services.fooddeliveryservice.domain.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);

    boolean existsByRole(UserRole role);
}
