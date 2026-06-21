package org.services.fooddeliveryservice.service;

import org.services.fooddeliveryservice.domain.AppUser;
import org.services.fooddeliveryservice.domain.Notification;
import org.services.fooddeliveryservice.repository.NotificationRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(AppUser recipient, String message) {
        try {
            notificationRepository.save(new Notification(recipient, message));
        } catch (RuntimeException ignored) {
            // Notification failures must not affect order/payment/status transactions.
        }
    }
}
