package com.clinicmanager.app;

import com.clinicmanager.controller.DoctorControlPanel;
import com.clinicmanager.controller.PatientControlPanel;
import com.clinicmanager.exception.AuthenticationException;
import com.clinicmanager.model.actors.Account;
import com.clinicmanager.model.enums.Role;
import com.clinicmanager.repository.DatabaseManager;
import com.clinicmanager.service.AccountManager;
import com.clinicmanager.service.NotificationManager;
import com.clinicmanager.model.actors.Account;
import com.clinicmanager.controller.BaseControlPanel;
import com.clinicmanager.model.actors.Account;

public class Clinic {
    AccountManager accountManager;
    NotificationManager notificationManager;
    DatabaseManager dbManager;

    BaseControlPanel login(String email, String password) {
        Account account = accountManager.login(email, password);
        if (account == null) {
            throw new AuthenticationException("Invalid credentials");
        }

        String token = accountManager.generateAccessToken(account);

        // Выбор панели в зависимости от роли
        if (account.role() == Role.DOCTOR) {
            return new DoctorControlPanel(account.ownerId(), token, accountManager);
        } else if (account.role() == Role.PATIENT) {
            return new PatientControlPanel(account.ownerId(), token, accountManager);
        }

        throw new IllegalStateException("Unsupported role");
    }
}
