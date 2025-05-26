package com.clinicmanager.controller;

import com.clinicmanager.service.AccountManager;
import com.clinicmanager.service.NotificationManager;
import com.clinicmanager.model.actors.Patient;
import com.clinicmanager.model.actors.Account;
import com.clinicmanager.repository.RepositoryManager;

public class PatientControlPanel extends BaseControlPanel {
    private Patient patient;

    public PatientControlPanel(String token, AccountManager accountManager, NotificationManager notificationManager) {
        super(token, accountManager, notificationManager);
        // Получаем аккаунт по токену
        Account acc = accountManager.getAccountByToken(token);
        // Получаем пациента по ownerId
        RepositoryManager repos = com.clinicmanager.gui.AppContext.getRepositories();
        this.patient = repos.patients.findById(acc.ownerId());
    }

    @Override
    public Object currentPerson() {
        return patient;
    }

    // ...другие методы
}
