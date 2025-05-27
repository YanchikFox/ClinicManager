package com.clinicmanager.controller;

import com.clinicmanager.exception.InvalidTokenException;
import com.clinicmanager.model.entities.Notification;
import com.clinicmanager.service.AccountManager;
import com.clinicmanager.service.NotificationManager;

import java.util.List;

public abstract class BaseControlPanel {
    protected final String token;
    protected final AccountManager accountManager;
    protected final NotificationManager notificationManager;

    // Bazowy konstruktor dla wspólnej logiki panelu lekarza i pacjenta
    protected BaseControlPanel(String token, AccountManager accountManager, NotificationManager notificationManager) {
        this.token = token;
        this.accountManager = accountManager;
        this.notificationManager = notificationManager;
    }

    // Sprawdza poprawność tokena – jeśli nieważny, rzuca wyjątek
    protected void requireValidToken() {
        if (!accountManager.validateToken(token)) {
            throw new InvalidTokenException("Access denied: invalid token");
        }
    }

    public void demoAction() {
        requireValidToken();
        System.out.println("Token is valid — access to the function is granted.");
    }

    // Zwraca listę powiadomień użytkownika (na podstawie ID konta z tokena)
    public List<Notification> viewNotifications() {
        requireValidToken();
        int accountId = accountManager.getAccountByToken(token).id();
        return notificationManager.getAllNotificationsByPersonId(accountId);
    }

    // Unieważnia token (np. przy wylogowaniu)
    public void revokeToken() {
        accountManager.revokeToken(token);
    }

    // Każdy panel (pacjenta/lekarza) musi zwrócić aktualnego użytkownika
    public abstract Object currentPerson();
}
