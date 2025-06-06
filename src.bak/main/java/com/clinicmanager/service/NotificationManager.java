package com.clinicmanager.service;

import com.clinicmanager.model.entities.Notification;
import com.clinicmanager.repository.NotificationRepository;

import java.time.LocalDateTime;
import java.util.List;

public class NotificationManager {
    private final NotificationRepository notificationRepository;

    public NotificationManager(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification createNotification(int personId, String message) {
        Notification n = new Notification(
                0, // autoincrement id
                personId,
                message,
                LocalDateTime.now(),
                false);
        notificationRepository.save(n);
        return n;
    }

    public void markAsRead(Notification n) {
        n.markAsRead();
        notificationRepository.update(n);
    }

    public List<Notification> getAllNotificationsByPersonId(int personId) {
        return notificationRepository.findByPersonId(personId);
    }

    public List<Notification> getUnreadNotificationsByPersonId(int personId) {
        return notificationRepository.findUnreadByPersonId(personId);
    }
}
