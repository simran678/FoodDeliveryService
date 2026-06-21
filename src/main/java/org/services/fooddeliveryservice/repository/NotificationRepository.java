package org.services.fooddeliveryservice.repository;

import org.services.fooddeliveryservice.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
