package com.clinicmanager.service;

import com.clinicmanager.model.entities.Notification;

import java.util.List;

public interface NotificationService {
    Notification createNotification(int personId, String message);

    void markAsRead(Notification n);

    List<Notification> getAllNotificationsByPersonId(int personId);

    List<Notification> getUnreadNotificationsByPersonId(int personId);
}
