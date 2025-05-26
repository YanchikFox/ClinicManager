package com.clinicmanager.controller;

import com.clinicmanager.service.AccountManager;
import com.clinicmanager.service.NotificationManager;
import com.clinicmanager.model.actors.Doctor;
import com.clinicmanager.model.actors.Account;
import com.clinicmanager.repository.RepositoryManager;

public class DoctorControlPanel extends BaseControlPanel {
    private Doctor doctor;

    public DoctorControlPanel(String token, AccountManager accountManager, NotificationManager notificationManager) {
        super(token, accountManager, notificationManager );
        // Получаем аккаунт по токену
        Account acc = accountManager.getAccountByToken(token);
        // Получаем доктора по ownerId
        RepositoryManager repos = com.clinicmanager.gui.AppContext.getRepositories();
        this.doctor = repos.doctors.findById(acc.ownerId());
    }

    @Override
    public Object currentPerson() {
        return doctor;
    }

    // ...другие методы
}

