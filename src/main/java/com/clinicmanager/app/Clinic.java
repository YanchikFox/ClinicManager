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

    public Clinic(AccountManager accountManager) {
        this.accountManager = accountManager;
    }

    public BaseControlPanel login(String email, String password) {
        String token = accountManager.login(email, password);
        Account acc = accountManager.getAccountByToken(token);

        return switch (acc.role()) {
            case DOCTOR -> new DoctorControlPanel(token, accountManager);
            case PATIENT -> new PatientControlPanel(token, accountManager);
        };
    }
}
