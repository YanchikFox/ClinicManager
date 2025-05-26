package com.clinicmanager.service;

import com.clinicmanager.model.entitys.Notification;
import com.clinicmanager.repository.NotificationRepository;

import java.time.LocalDateTime;
import java.util.List;

public class NotificationManager {
    private final NotificationRepository repo;

    public NotificationManager(NotificationRepository repo) {
        this.repo = repo;
    }

    public Notification createNotification(int personId, String message) {
        Notification n = new Notification(
                0, // autoincrement id
                personId,
                message,
                LocalDateTime.now(),
                false
        );
        repo.save(n);
        return n;
    }

    public void markAsRead(Notification n) {
        n.markAsRead();
        repo.update(n);
    }

    public List<Notification> getAllNotificationsByPersonId(int personId) {
        return repo.findByPersonId(personId);
    }

    public List<Notification> getUnreadNotificationsByPersonId(int personId) {
        return repo.findUnreadByPersonId(personId);
    }
}
