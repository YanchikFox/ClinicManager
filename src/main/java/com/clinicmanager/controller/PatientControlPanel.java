package com.clinicmanager.controller;

import com.clinicmanager.service.AccountManager;
import com.clinicmanager.model.actors.Patient;
import com.clinicmanager.model.actors.Account;
import com.clinicmanager.repository.RepositoryManager;

public class PatientControlPanel extends BaseControlPanel {
    private final Patient patient;

    public PatientControlPanel(String token, AccountManager accountManager) {
        super(token, accountManager);
        Account acc = accountManager.getAccountByToken(token);
        RepositoryManager repos = com.clinicmanager.gui.AppContext.getRepositories();
        this.patient = repos.patients.findById(acc.ownerId());
    }

    @Override
    public Object currentPerson() {
        requireValidToken();
        requirePatientRole();
        return patient;
    }

    private void requirePatientRole() {
        if (!(accountManager.getAccountByToken(token).role().name().equals("PATIENT"))) {
            throw new com.clinicmanager.exception.InvalidTokenException("Access denied: not a patient");
        }
    }
}
