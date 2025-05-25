package com.clinicmanager.app;

import com.clinicmanager.controller.BaseControlPanel;
import com.clinicmanager.controller.DoctorControlPanel;
import com.clinicmanager.controller.PatientControlPanel;
import com.clinicmanager.model.actors.Account;
import com.clinicmanager.repository.NotificationRepository;
import com.clinicmanager.repository.RepositoryManager;
import com.clinicmanager.service.AccountManager;
import com.clinicmanager.service.NotificationManager;

import static com.clinicmanager.model.enums.Role.PATIENT;

public class Clinic {
    private final AccountManager accountManager;
    private final NotificationManager notificationManager;

    public Clinic(AccountManager accountManager, NotificationManager notificationManager) {
        this.accountManager = accountManager;
        this.notificationManager = notificationManager;
    }

    public BaseControlPanel login(String email, String password) {
        String token = accountManager.login(email, password);
        Account acc = accountManager.getAccountByToken(token);

        return switch (acc.role()) {
            case DOCTOR -> new DoctorControlPanel(token, accountManager, notificationManager);
            case PATIENT -> new PatientControlPanel(token, accountManager, notificationManager);
        };
    }
}