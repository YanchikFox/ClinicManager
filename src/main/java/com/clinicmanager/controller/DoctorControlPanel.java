package com.clinicmanager.controller;

import com.clinicmanager.service.AccountManager;
import com.clinicmanager.model.actors.Doctor;
import com.clinicmanager.model.actors.Account;
import com.clinicmanager.repository.RepositoryManager;

public class DoctorControlPanel extends BaseControlPanel {
    private final Doctor doctor;

    public DoctorControlPanel(String token, AccountManager accountManager) {
        super(token, accountManager);
        Account acc = accountManager.getAccountByToken(token);
        RepositoryManager repos = com.clinicmanager.gui.AppContext.getRepositories();
        this.doctor = repos.doctors.findById(acc.ownerId());
    }

    @Override
    public Object currentPerson() {
        requireValidToken();
        requireDoctorRole();
        return doctor;
    }

    private void requireDoctorRole() {
        if (!(accountManager.getAccountByToken(token).role().name().equals("DOCTOR"))) {
            throw new com.clinicmanager.exception.InvalidTokenException("Access denied: not a doctor");
        }
    }
}

