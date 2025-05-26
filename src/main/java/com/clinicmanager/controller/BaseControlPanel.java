package com.clinicmanager.controller;

import com.clinicmanager.exception.InvalidTokenException;
import com.clinicmanager.model.entities.Notification;
import com.clinicmanager.service.AccountManager;

import java.util.List;

import com.clinicmanager.service.NotificationManager;

public abstract class BaseControlPanel {
    protected final String token;
    protected final AccountManager accountManager;
    protected final NotificationManager notificationManager;
    protected BaseControlPanel(String token, AccountManager accountManager, NotificationManager notificationManager) {
        this.token = token;
        this.accountManager = accountManager;
        this.notificationManager = notificationManager;
    }

    protected void requireValidToken() {
        if (!accountManager.validateToken(token)) {
            throw new InvalidTokenException("Access denied: invalid token");
        }
    }
    public void demoAction() {
        requireValidToken();
        System.out.println("Токен действителен — доступ к функции разрешён.");
    }

    public List<Notification> viewNotifications() {
        requireValidToken();
        int accountId = accountManager.getAccountByToken(token).id();
        return notificationManager.getAllNotificationsByPersonId(accountId);
    }

    public abstract Object currentPerson();
}
